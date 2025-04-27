import socketserver
import json
import configparser as cfp


class ftp_server(socketserver.BaseRequestHandler):
    def handle(self):
        print(self.request)

    def user_info(self):
        info=self.request.recv(1024).decode('utf8')
        info=json.loads(info)
        return info

    def user_refuse(self):
        self.request.sent("账号密码错误，请重新输入！".encode('utf8'))


    def user_action(self):
        pass

