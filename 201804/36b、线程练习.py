import threading
import time

class counttask():
    def __init__(self):
        self._run=True

    def terminate(self):
        self._run=False    #运行该函数会将_run节点设为false

    def run(self,n):
        while self._run and n>0:
            print("block-mins",n)
            n-=1
            time.sleep(5)

c=counttask()
t=threading.Thread(target=c.run,args=(10,))
t.start()
time.sleep(6)
c.terminate()
t.join()
