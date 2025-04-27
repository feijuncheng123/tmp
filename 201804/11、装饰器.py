#装饰器：在不修改原始函数的基础上，为原函数添加功能。

#高阶函数：
#满足任意一个条件：1、参数为函数名；2、返回值是函数名

import time

#装饰器函数：
def  decorator(function):
    def fun1(x):
        start_time = time.time()
        fun(x)    #原函数执行
        end_time=time.time()
        print(start_time-end_time)
    return fun1  #返回新函数

#原函数:需要添加统计运行时长的功能
@decorator   #用原函数名覆盖新函数名，调用后等于执行了装饰函数内的新函数
def fun(x):
    a=0
    for i in range(x):
        a += i
    return a

#相当于
fun=decorator(fun)
fun()

#带参数的装饰器
def fun1(auth=None):
    def fun2(fun):   #第二层函数接受函数作为变量，即装饰函数，接受原函数进行装饰
        def fun3(*args,**kwargs):    #接受与原函数相同的变量
            pass
            a=fun(*args,**kwargs)
            return a
        return fun3    #返回第三层函数，即装饰后新函数
    return fun2

@fun1(auth="ssl")   #先执行装饰器最外层函数。返回第二次函数进行装饰
def meta_fun(*args,**kwargs):
    pass

meta_fun(1,2,3)
#相当于
meta_fun= fun1(auth="ssl")(meta_fun)(1,2,3)