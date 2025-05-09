#常用异常
# AttributeError 试图访问一个对象没有的树形，比如foo.x，但是foo没有属性x
# IOError 输入/输出异常；基本上是无法打开文件
# ImportError 无法引入模块或包；基本上是路径问题或名称错误
# IndentationError 语法错误（的子类） ；代码没有正确对齐
# IndexError 下标索引超出序列边界，比如当x只有三个元素，却试图访问x[5]
# KeyError 试图访问字典里不存在的键
# KeyboardInterrupt Ctrl+C被按下
# NameError 使用一个还未被赋予对象的变量
# SyntaxError Python代码非法，代码不能编译(个人认为这是语法错误，写错了）
# TypeError 传入对象类型与要求的不符合
# UnboundLocalError 试图访问一个还未被设置的局部变量，基本上是由于另有一个同名的全局变量，导致你以为正在访问它
# ValueError 传入一个调用者不期望的值，即使值的类型是正确的
#TypeError:int类型不可迭代

for i in 3:
    pass
#ValueError
num=input(">>: ") #输入hello
int(num)

#NameError
aaa

#IndexError
l=['egon','aa']
l[3]

#KeyError
dic={'name':'egon'}
dic['age']

#AttributeError
class Foo:pass
Foo.x

#ZeroDivisionError:无法完成计算
res1=1/0
res2=1+'str'


#结构：尽量少用
try:
    pass
    raise KeyError  #主动触发异常
except KeyError as k:
    print("......")
except AttributeError:
    print('...')
except Exception:
    print("...")
else:     #如果都没有异常，就会执行else
    print('else')
finally:   #无论有误异常，都会执行
    print('...')

#自定义异常
class excep(BaseException):
    def __init__(self,name):
        self.name=name
    def __str__(self):
        return self.name

#断言
assert 1==2  #断言判断，如果错误就发起一个异常，终结程序运行。（大型程序中很常用）
#if判断可以实现相同的功能。但断言更简洁

