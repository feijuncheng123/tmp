#描述符：类似于装饰器，只是用于装饰静态属性

class desc:  #删除其中之一都会导致相应功能消失，而且会报错。要满足描述符功能，至少需要get、set功能
    def __init__(self,name):
        self.name=name
    def __set__(self, instance, value):   #self是指被描述类中创建的变量本身。instance是被描述类的实例
        print('set描述符')                 #即self为描述类的实例，也是被描述类的变量。
        instance.__dict__[self.name]=value    #要进行定义，否则设置不成功
    def __get__(self, instance, owner):
        print('get描述符')
        return instance.__dict__[self.name]
    def __delete__(self, instance):
        print('__delete__')
        instance.__dict__.remove[self.name]

class cls1:
    x=desc('x')   #创建描述符。实例对x,y的设置获取删除操作将触发desc类中相关方法的执行
    y=desc('y')   #参数是desc类中init初始化的参数，作为操作实例__dict__使用
    # 描述符只能在类级别上定义，不能放入实例的初始化函数中
    def __init__(self,x,y):
        self.x=x    #使用描述符修饰后，实例的__dict__字典变为空。需要在描述符类中进行操作定义
        self.y=y    #传入参数就会触发__set__方法
    def __getattribute__(self, item):
        print('__getattribute__')
        if hasattr(cls1,item):
            return cls1.__dict__[item]
        else:
            raise AttributeError
    def __getattr__(self, item):
        print('__getattr__')


a=cls1(1,2)
print(a.__dict__)
print(a.x)
a.y=3
del a.x
a.x=1  #再次设置还是会触发描述符

cls1.x=1  #类级别上的操作，会直接覆盖类的x属性值，导致描述符失效。
print(cls1.x)  #在类的__dict__字典，x为实例对象。会打印内存对象
a.x=1 #实例的属性操作。由于在类中x为描述符实例。a.x会在类的__dict__字典中查找，触发描述符运行


#调用优先级：
#通过类名对类属性直接调用。
#__getattribute__
#数据描述符
#实例对属性进行调用（只要属性被数据描述符装饰，就会触发数据描述符先执行）
#没有被描述的实例属性
#__getattr__


