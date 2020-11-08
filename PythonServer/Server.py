from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse
from shutil import copyfile, rmtree
import os
import socket
import subprocess
import json
import cgi
import time

SERVER_IP = "192.168.0.42"

ports = {}
process_d = {}

class Server(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()

    def do_GET(self):
        query = urlparse(self.path)
        query_components = query.path.split("/")[1:]

        if "getnewaddress" in query_components:
            result = self.create_new_instance()
            if result is None:
                self.send_response(500)
                self.end_headers()
                return

            privkey = subprocess.check_output(
                args=[os.getcwd() + "\\" + str(ports[result]) + "\\bitcoin-cli.exe", "-datadir=data",
                      "-rpcport=" + str(ports[result]), "dumpprivkey", result],
                shell=True)

            self._set_headers()
            self.wfile.write(json.dumps({'pubkey': result, 'privkey': ''.join(e for e in privkey.decode("UTF-8") if e.isalnum())}).encode("UTF-8"))
        elif "gettransaction" in query_components:
            if len(query_components) != 3:
                self.send_response(400)
                self.end_headers()
                return

            result = subprocess.check_output(
                args=[os.getcwd() + "\\" + str(ports[query_components[1]]) + "\\bitcoin-cli.exe", "-datadir=data",
                      "-rpcport=" + str(ports[query_components[1]]), "gettransaction", query_components[2]],
                shell=True)
            result = result.decode("UTF-8")
            self._set_headers()
            self.wfile.write(json.dumps(result).encode("UTF-8"))
        elif "listtransactions" in query_components:
            if len(query_components) != 2:
                self.send_response(400)
                self.end_headers()
                return

            result = subprocess.check_output(
                args=[os.getcwd() + "\\" + str(ports[query_components[1]]) + "\\bitcoin-cli.exe", "-datadir=data",
                      "-rpcport=" + str(ports[query_components[1]]), "listreceivedbyaddress", "0"],
                shell=True)
            result = result.decode("UTF-8")
            print(result)

            self._set_headers()
            self.wfile.write(json.dumps(result).encode("UTF-8"))
        else:
            self.send_response(400)

    def do_POST(self):
        query = urlparse(self.path)
        query_components = query.path.split("/")[1:]

        ctype, pdict = cgi.parse_header(self.headers.get('content-type'))

        # refuse to receive non-json content
        if ctype != 'application/json':
            self.send_response(400)
            self.end_headers()
            return

        length = int(self.headers.get('content-length'))
        payload = json.loads(self.rfile.read(length))

        if "sendtoaddress" in query_components:
            if "address" in payload.keys() and "privkey" in payload.keys() and len(query_components) == 2:
                privkey = subprocess.check_output(
                    args=[os.getcwd() + "\\" + str(ports[query_components[1]]) + "\\bitcoin-cli.exe", "-datadir=data",
                          "-rpcport=" + str(ports[query_components[1]]), "dumpprivkey", query_components[1]],
                    shell=True)
                print(privkey.decode("UTF-8"))
                print(payload["privkey"])
                if ''.join(e for e in privkey.decode("UTF-8") if e.isalnum()) != payload['privkey']:
                    self.send_response(403)
                    self.end_headers()
                    return

                result = subprocess.check_output(
                    args=[os.getcwd() + "\\" + str(ports[query_components[1]]) + "\\bitcoin-cli.exe", "-datadir=data",
                          "-rpcport=" + str(ports[query_components[1]]), "sendfrom", query_components[1], payload["address"], "0", "0"],
                    shell=True)
                self._set_headers()
                self.wfile.write(json.dumps({'txid': ''.join(e for e in result.decode("UTF-8") if e.isalnum())}).encode("UTF-8"))
            else:
                self.send_response(400)
                self.end_headers()
        else:
            self.send_response(400)
            self.end_headers()

    def next_free_port(self, port=1024, max_port=65535):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        while port <= max_port:
            if port in ports.values():
                port += 1
                continue

            try:
                sock.bind(('', port))
                sock.close()

                return port
            except OSError:
                port += 1
        return -1

    def create_new_instance(self):

        port = self.next_free_port()
        if port == -1:
            return None

        self.add_conf_entry(port)

        os.mkdir(os.getcwd() + "\\" + str(port))
        os.mkdir(os.getcwd() + "\\" + str(port) + "\\data")
        copyfile(os.getcwd() + "\\bitcoin-cli.exe", os.getcwd() + "\\" + str(port) + "\\bitcoin-cli.exe")
        copyfile(os.getcwd() + "\\bitcoind.exe", os.getcwd() + "\\" + str(port) + "\\bitcoind.exe")
        copyfile(os.getcwd() + "\\data\\bitcoin.conf", os.getcwd() + "\\" + str(port) + "\\data\\bitcoin.conf")

        pd = subprocess.Popen(args=[os.getcwd() + "\\" + str(port) + "\\bitcoind.exe", "-datadir=" + os.getcwd() + "\\" + str(port) + "\\data", "-rpcport=" + str(port)])
        time.sleep(5)
        result = subprocess.check_output(args=[os.getcwd() + "\\" + str(port) + "\\bitcoin-cli.exe", "-datadir=" + os.getcwd() + "\\" + str(port) + "\\data", "-rpcport=" + str(port), "getnewaddress"])
        result = result.decode("UTF-8").split("\r")[0]
        hashes = subprocess.check_output(args=[os.getcwd() + "\\" + str(port) + "\\bitcoin-cli.exe",
                                      "-datadir=" + os.getcwd() + "\\" + str(port) + "\\data", "-rpcport=" + str(port),
                                      "generatetoaddress", "100", result])

        #pd.terminate()

        #os.rename(os.getcwd() + "\\1", os.getcwd() + "\\" + result)
        process_d[result] = pd
        ports[result] = port

        return result

    def add_conf_entry(self, port):
        with open('data/bitcoin.conf', 'a') as file:
            file.write('\nconnect=' + SERVER_IP + ":" + str(port))

        for pubkey in process_d.keys():
            copyfile(os.getcwd() + "\\data\\bitcoin.conf", os.getcwd() + "\\" + str(ports[pubkey]) + "\\data\\bitcoin.conf")


def run(server_class=HTTPServer, handler_class=Server, port=8008):
    for i in range(1024, 65536):
        if os.path.isdir(os.getcwd() + "\\" + str(i)):
            rmtree(os.getcwd() + "\\" + str(i))
    server_address = (SERVER_IP, port)
    httpd = server_class(server_address, handler_class)

    print('Starting httpd on port %d...' % port)
    httpd.serve_forever()


if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()