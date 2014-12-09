import time
import BaseHTTPServer
from DataAnalyze import get_data

HOSTNAME = "SDMDomotic.servebeer.com"
PORT_NUMBER = 8080

class DomoticHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()

    def do_GET(s):
        """Response to a GET Request."""
        s.send_response(200)
        s.send_header("Content-type", "application/json")
        s.end_headers()

        s.wfile.write(get_data())

if __name__ == '__main__':

    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class(('', PORT_NUMBER), DomoticHandler)

    print time.asctime(), "Server starts - %s:%s"%(HOSTNAME, PORT_NUMBER)

    try:
            httpd.serve_forever()
    except KeyboardInterrupt:
            pass
    httpd.server_close()
    print "\n", time.asctime(), "Server stops - %s:%s"%(HOSTNAME, PORT_NUMBER)
