#参数
def fun(*args,**kwargs):   #fun为变量名，可被覆盖
    #*代表列表、元组解析，**代表字典解析
    "the func's definitions"   #可以添加函数注释
    print(args[1],kwargs['name'])
    return "这是一个函数"  #可返回多个值，用逗号分隔，以元组形式返回
#没有return，则默认返回None.

#形参：定义函数时，定义的参数。
#实参：调用函数时，实际输入的参数

def fun1(x,y,z,a=1):  #默认参数需放在正常参数后面
    pass
fun1(1,3,4)  #按参数位置传入
fun1(1,2,z=3) #参数名传入。位置传入的参数必须在按参数名传入参数的左边

def fun3(a,b,c=1,*args,**kwargs):pass #参数顺序

#局部变量与全局变量
#全局变量：在模块第一层定义的变量，作用域为全模块。（无缩进）
#局部变量：在子程序中，如函数中定义的变量，作用域仅该函数。无法在子程序中更改全局变量。（有缩进）

a=1
def fun4(a,b):
    x=[]            #a,b,x均为函数中定义的局部变量，无法在子程序中修改全局变量
    return a,b,c

def fun5(a,b):
    global a         #global声明a是全局变量。nonlocal则指定上一级变量
    x=[]
    return a,b,c

#装饰器基础
def fun6():
    print('name')
    def fun6a():
        print('age')
    return fun6a

f=fun6()  #打印name，同时返回一些函数，将fun6a函数赋值给f
f()  #等于执行fun6a函数。