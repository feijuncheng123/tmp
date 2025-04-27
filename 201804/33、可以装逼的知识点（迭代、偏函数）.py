from functools import partial

#partial为偏函数。
# partial函数的作用就是：
#将所作用的函数作为partial（）函数的第一个参数，原函数的各个参数依次作为partial（）函数的后续参数。
#原函数有关键字参数的一定要带上关键字，没有的话，按原有参数顺序进行补充。
#例子：
def func(x,y,z=1,w=3):
    return x+y+z+w

new_func=partial(func,1,w=2,z=5)
#将原函数使用partial进行装饰。普通参数按原函数的顺序进行输入，关键字参数需要使用关键字输入，否则按顺序输入
#然后返回新函数，接收没有指定默认值的参数的值，然后计算结果

iter('function','限定位置（结尾可以用None）') #高级用法：
#iter参数一：函数；参数二：停止位
#通过调用next方法，iter会一直执行function函数，直到函数返回限定位置的数据为止
#函数无需为生成器
l=[1,2,3,4,5,6,7,8,9]
def func1():
    return l.pop()

i=iter(func1,3)
next(i)  #每一次next，会执行一次func1函数，直到返回值到达3
