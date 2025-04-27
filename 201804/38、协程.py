#方法一：可以通过生成器与迭代器实现协程
#方法二：
from greenlet import greenlet

def fun1():
    print('fun1')
    gf1.switch()  #又切换到fun2函数

def fun2():
    print('fun2')
    gf1.switch()  #切换到fun1函数。并保留当前状态。切换回来时从此处执行

gf1=greenlet(fun1)  #把需要协作的函数分别封装
gf2=greenlet(fun2)

gf2.switch()  #执行fun2函数

#方法三：
import gevent
gevent.joinall([
        gevent.spawn(fun2,'args'),
        gevent.spawn(fun1,'args')
    ])

import asyncore  #注意该模块相关模块

#事件驱动编程
