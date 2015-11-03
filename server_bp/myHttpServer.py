import time
import cgi
import BaseHTTPServer
from gmail_util import *


HOST_NAME = ''
PORT_NUMBER = 9000
class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def do_GET(s):
        s.send_response(200)
        s.send_header('Content-type', 'text')
        s.end_headers()
        s.wfile.write('this is a test')

    def do_POST(s):
        form = cgi.FieldStorage(
            fp = s.rfile,
            headers = s.headers,
            environ={'REQUEST_METHOD':'POST','CONTENT_TYPE':s.headers['Content-Type']},
        )
        #execute commands
        cmd = form['Command'].value
        print 'recieve'
        responseText = ''
        if cmd=='check-in-box':
            message_num = CheckInBox()
            #process messages
            responseText = 'unread_num:'+str(message_num)+'|'
        elif cmd=='summarize':
            senderlst = SummarizeFrom()
            responseText = 'Senderlst:'
            for sender in senderlst:
                responseText+=sender+'.'
            responseText+='|'
        elif cmd=='read':
            responseText='email not found'
            msglst = GetMsgLst()
            if form['MsgId'].value=='first':
                responseText = 'email-content:'+ReadMsgById(msglst[0]['id'])+'|'
            elif form['MsgId'].value=='name':
                responseText = 'email-content:'+ReadMsgFrom(form['Name'].value)+'|'
                
                
        s.send_response(200)
        s.send_header('Content-type', 'text')
        s.end_headers()
        s.wfile.write(responseText)
        
        


if __name__=='__main__':
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)
        
