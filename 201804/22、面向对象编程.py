#属性（特征）
#1、主要为静态

#方法（动作）
#1、主要为动态

#对象
#1、将同一对象的属性和方法进行封装

class math:   #传统类
    '''这是文档'''
    π = 3.1415926   #在类方法里要调用静态属性变量，需要使用self.π。如果直接用π，会到全局里查找，没有就报错
    def __init__(self,x,y,z):   #生成实例对象时，会自动执行该函数进行初始化，接收变量
        print("实例化")        #类变量的作用域类似函数。实例的变量首先在init函数中查找，然后在上一层找
        print(self.π)
        self.x=x               #实例__dict__函数仅返回init函数中的变量名（作用域）
        self.y=y
        self.z=z

    def addfunc(self):
        he=self.x+self.y+self.z
        print(he)

    @property   #将函数转化为静态属性。调用时不用加括号。静态方法可以让实例的属性值随着实例变动。如纳税额随着销售额增长。
    def right(self):
        print("This class's property right belong to FeiJuncheng")
        return (self.x,self.y,self.z)

    @classmethod   #类方法。类似于π。类级别的静态方法。
    def laugh(cls,var):  #类方法中cls类似于其他函数中的self，只不过cls是指类本身，将类本身作为参数传入
        print('哈哈哈哈，这个是类方法，类似上面的π')
        print(var)  #参数需要另行传值
        print(cls.π)   #调用类的静态属性π
    #在类方法中，self关键字被禁用，会被当成普通参数对待（实例调用能运行，但不能调用实例属性）

    @staticmethod   #如果不加staticmethod，直接定义函数，则实例调用会出错（会把self自行传入，但无参数接收）
    def exciting(*args):  #静态方法。和普通函数无任何区别。除了封装在类中，能被调用。所有参数都要单独传值
        print("Very exciting! 这里是静态方法，就是类里面的普通函数！")
        print(args)

#property：可以访问类属性，也可以访问实例属性
#classmethod：只能访问类属性，无法访问实例属性（需要单独传值）
#staticmethod：都不能访问，需要单独传值

emp=math(1,2,3)   #创建实例对象
emp.π
emp.x   #查看类属性
math.π=3.14  #会导致类属性变量被覆盖。后面创建实例都会受影响
emp.addfunc()  #查看类方法

def sub(self):
    cha=self.x-self.y-self.z
    print(cha)

math.e=2.18  #对类增加属性。后面创建的实例都可以调用
math.right  #调用静态属性
math.sub=sub  #也可以增加函数方法
emp.t=123  #对实例增加属性，但只能该实例使用
#实例也可以添加函数方法
del math.sub  #删除属性


print(math.__dict__)  #返回类中的属性值（变量），以字典形式.无需括号
print(math.__name__)  #返回类名
print(math.__doc__)   #返回注释文档。不能被子类继承
print(math.__module__)  #显示当前类的模块（__main__或其他）


class cls2(object):  #新式类
    def __init__(self):
        pass