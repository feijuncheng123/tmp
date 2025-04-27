import threading
import time

lock=threading.Lock() #定义一把锁对象

def func(argv):
    lock.acquire()   #获取一把锁。锁定一个代码块，执行完该代码块才能往下执行（穿行）。（解决同步锁争用）
    print('当前是第%s'%argv)
    time.sleep(3)
    lock.release()  #释放一把锁


if __name__ == '__main__':
    t1=threading.Thread(target=func,args=(1,),name='thread1')
    #创建实例，target参数为要运行的函数名，args为函数需要传入的参数，以可迭代方式传入
    t2 = threading.Thread(target=func, args=(2,))
    t2.setDaemon(True)
    #设定t2为守护线程。即主线程结束，该线程无论是否完成立即也就结束（如是循环立即结束循环退出）。必须设定在start前面
    #主进程结束是需要未设定守护线程的其他线程都完成，才会结束
    t1.start()   #启动线程
    t2.start()
    t1.getName() #返回线程名
    t1.setName('name') #设置线程名
    t2.is_alive() #线程是否活动

    t2.join()  #需要t2结束后，其他线程才能结束（开头不收影响，结尾需要t2结束其他才能结束
    print("this is the main threading")

    threading.active_count()   #返回当前有几个线程在活动。往往用于判断是否只有主线程
    threading.current_thread()  #返回当前的线程变量
    threading.enumerate()  #返回当前正在运行的线程列表


    #主线程是全部进程都结束，才会结束
    #由于python全局锁的存在，多线程仅对io密集型任务有效，对计算密集型任务则无效，甚至效率更低
    #递归锁：定义两个锁，两个函数，分别嵌套锁。多线程运行时可能存在两线程分别抢到两个锁，然后又互相等待对方释放锁，导致锁死
    #同步锁：通过循环创建线程，如果函数的变量是需要传递的，多个线程同时运行，会导致变量获取错误，使计算结果失效

    event=threading.Event()  #同步操作
    event.set()  #为线程设定一个标志位
    event.wait()  #需要等其他线程运行到标志位，该线程才能运行，否则一直等待
    event.clear() #等待后并运行后，需要清空上一次的set状态，同时设定下一个set标志位，并等待
    event.is_set() #判断事件是否被设定标志位了

    sema=threading.Semaphore(3)  #设定最多可同时运行的线程数。默认1个
    sema.acquire()   #设定某一个代码块，最多只能3个线程同时执行
    sema.release()   #释放锁定


import multiprocessing
#多进程
def fun3(name):
    print('fun3:%s'%name)

p=multiprocessing.Process(target=fun3,args=('fei',),name='process1') #创建进程对象
p.start()  #同线程
p.daemon=True   #守护进程。（属性）
p.is_alive()  #判断进程是否在运行中
p.pid()  #当前进程的pid
p.terminate() #立即停止进程，无论是否完成

#进程间通信
q=multiprocessing.Queue()  #进程队列
#子进程需要在参数中传入队列
#主进程与子进程间存在数据复制。需要测试变化

#通过管道通信
from multiprocessing import Pipe
parent_pipe,child_pipe=Pipe()   #双向管道
#创建子进程时，将child_pipe作为参数传入子进程
parent_pipe.recv() #接收信息
parent_pipe.send("变量")  #发送变量


#进程间数据共享
from multiprocessing import Manager
with Manager() as m:
    d=m.dict()   #创建共享字典
    l=m.list()   #共享列表
#然后：将共享变量以参数形成传入子进程.(fun3函数需要修改形参接收）
p1=multiprocessing.Process(target=fun3,args=('fei',d,l,),name='process1')

#进程锁
from multiprocessing import Lock
lock1=Lock()
#同样以参数形式传入子进程
lock1.acquire()  #开启锁
lock1.release()  #释放锁

#进程池
from multiprocessing import Pool
pool=Pool(5)  #定义最大进程数5，不输入默认cpu核数

for i in range(100):
    pool.apply_async(func=fun3,args=('fei',),callback=func)   #最多产生5个进程，退出一个另一个才能开始执行
    #callback为回调函数：执行fun3成功后，立即执行func函数。可省略。func函数必须带一个形参
    #callback函数是在主进程调用的。所带参数用来接收子进程函数的返回值
    pool.apply(func=fun3,args=('fei',))  #串行。无并行效果，一个一个执行了

pool.close()  #必须和join连用，且必须放在join前面，必不可少。否则报错
pool.join()  #和close连用。







