#迭代器
#简单的说，迭代器是逐个往后取出的序列，取出最后一个后迭代器失效（元素取尽了），
#列表、元组、字典、字符串等符合for循环，是因为调用了__iter__方法，本身不属于可迭代对象
#文件对象是迭代器，满足next()方法
iter('序列') #可以用iter函数将序列转化为迭代器

#可迭代对象可变：如果变量名指向同一内存地址，变量在循环中改变，会影响循环本身


#生成器
#生成一个遵循迭代器协议的数据类型，以函数、生成器表达式实现

#生成器表达式：列表解析
a=5
b = "大于10" if a>10 else "小于10"  #三元表达式：如果a>10,则b等于"大于10"，否则等于"小于10"
print(b)
c=[i for i in range(10) if i%2 == 1]  #列表解析：if语句不能带else
c1=(i for i in range(10) if i%2 == 1) #生成器表达式：并非元组

#函数生成器
def fun(x):
    i=0
    while i < x:
        i += 1
        yield i #可以保存函数状态

fun_obj=fun(10)
next(fun_obj)
#通过yield返回值，每一次返回一个，调用一次next返回一次
#yield可以接受值，也可以返回值：
def testfunc():
    for i in range(10):
        a=yield i
        print(a)

test=testfunc()
next(test)
test.send(100)

#yield可以在函数中多次出现
def fun1():
    a=yield     #yield还可以接收值，通过生成器函数的send方法传值
    while a >0:
        yield a
        a -= 1

fun1obj=fun1()
next(fun1obj)
fun1obj.send(10)
#注意：send方法无法启动生成器，需要在next启动后方能触发自动运行



#简单的异步：
import time
def costomers(costomername,cash,price):
    while cash>0:
        goods=yield price
        print("%s:花了%s元购买了%s"%(costomername,price,goods))
        print("%s开始吃%s"%(costomername,goods))
        cash -= price

def producer():
    goods = yield
    while True:
        print("生产厂家：开始生产%s" % goods)
        yield goods


def shop():
    c1=costomers("顾客1",100,10)
    c1.__next__()
    print("欢迎顾客1光临！")
    c2=costomers("顾客2",100,20)
    c2.__next__()
    print("欢迎顾客2光临！")

    order1=producer()
    order1.__next__()
    order1.send("馒头")

    order2=producer()
    order2.__next__()
    order2.send("豆浆")

    flag1,flag2=0,0
    x=1
    while True:
        print("-----------------------第%s次---------------------"%x)
        try:
            income1=c1.send("馒头")
            print("店家：收了顾客一%s元" % income1)
            goods1=next(order1)
            print("店家：向厂家订购了产品%s" % goods1)
        except StopIteration:
            flag1=1
        print("*********************************************")
        try:
            income2=c2.send("豆浆")
            print("店家：收了顾客二%s元" % income2)
            goods2=next(order2)
            print("店家：向厂家订购了产品%s" % goods2)
        except StopIteration:
            flag2=1
        if flag1+flag2==2:break
        time.sleep(2)
        x+=1

if __name__ == '__main__':
    shop()

