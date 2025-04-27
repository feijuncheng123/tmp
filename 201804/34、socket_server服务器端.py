import socketserver
import subprocess
import struct

class MyServer(socketserver.BaseRequestHandler):
    def handle(self):  #self相当于accept  #必须要定义一个handle
        print(self.request)   #self.request相当于链接conn
        print(self.client_address) #self.client_address相当于addr

        while True:
            try:
                data=self.request.recv(1024)
                x=data.decode('gbk')
                f=subprocess.Popen(x,shell=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,stdin=subprocess.PIPE)
                err=f.stderr.read()
                if not err:
                    seq=f.stdout.read()
                else: seq=err
                length=len(seq)
                l=struct.pack('i',length)
                self.request.sendall(l+seq)
            except Exception:
                continue


if __name__ == "__main__":
    s=socketserver.ThreadingTCPServer(('127.0.0.1',8001),MyServer)
    s.serve_forever()


#socketserver模块：可以实现并发效果

