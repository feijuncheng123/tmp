import socket
import optparse
import json

class clientHandle:
    def __init__(self):
        op=optparse.OptionParser()
        op.add_option('-h','--host',dest='host')
        op.add_option('-P','--Port',dest='Port')
        op.add_option('-u','--user',dest='user')
        op.add_option('-p','--password',dest='passwd')
        self.options, self.args = op.parse_args()
        info = {}
        info['username'] = self.options.user
        info['passwd'] = self.options.passwd
        self.info = json.dumps(info)

    def info_recv(self):


        return self.options,self.args

    def connect(self):
        self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client.connect((self.optionss.host, int(self.options.Port)))

    def interaction(self):





