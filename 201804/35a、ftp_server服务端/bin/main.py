import setting
from core import ftp_server as fs
import configparser as cfp


class authenticate:
    def __init__(self):
        pass

    def argv_recv(self):
        info={}


    def verify(self):
        argv = fs.userinfo()
        config=cfp.ConfigParser(allow_no_value=True)
        config.read('core/accounts.cfg')
        if argv['username'] in config:
            if argv['passwd']==config[argv['username']]['passwd']:
                self.server_start()
            else: self.argv_recv()
        else:self.argv_recv()

    def server_start(self):
        pass

    def server_close(self):
        pass



if __name__=="__main__":
    pass