class cls1:
    def __init__(self):
        pass
    def __getattr__(self, item):
        pass
    def __getattribute__(self, item):
        #实例调用属性时优先触发该方法执行（无论属性是否存在），getattr不执行。定义异常触发后，会跳到getattr执行（不定义不会跳过去）
        print('新方法')
        raise AttributeError('跳到__getattr__执行')  #仅能AttributeError异常，其他异常不行。。。
        return cls1.__getattribute__(self,item)    #返回时需要用基类调用

class cls2(cls1):
    pass

isinstance(123,int)  #判断对象是否是类的实例。子类创建的实例也是父类的实例
issubclass(cls2,cls1)   #判断第一个参数是否为第二个参数的子类

#item：仅通过字典形式操作实例，才会触发item相关函数
class cls2:
    def __init__(self):
        pass

    def __getitem__(self, item):  #可以通过字典形式的键值对取值，会触发该函数
        pass

    def __setitem__(self, key, value):  #实例属性可以通过字典形式的方式赋值。触发该函数执行
        self.__dict__[key]=value

    def __delitem__(self, key):  #通过字典形式删除实例属性，会触发该函数
        self.__dict__.pop(key)

#其他
class cls3:
    def __str__(self):   #创建实例后，打印实例（print)，会调用这个方法，打印该方法返回值
        return '打印该内容'

    def __repr__(self):
        return '输出该内容'   #比__str__更强大。str仅用于print输出，而repr可以在交互解释器中输出实例形式（print优先调str，其次repr）

    def __format__(self, format_spec):  #format函数可以使用实例。默认调用该方法。必须返回字符串.format_spec默认为空
        print('__format__')
        return  "根据format_spe格式返回相应的字符串"

c=cls3()
format(c,'格式')  #对实例根据相应格式显示

class cls4:
    __slots__ = ['key1','key2','...'] #定义slots后，实例将没有__dict__字典（多实例时可节省内存空间）
    #__slots__定义后，将不能添加新属性，必须使用slots中定义的属性

    def __init__(self,key1):
        self.key1=key1
    def __del__(self):
        print('析构函数')   #删除属性时不会触发，但是内存回收空间时会触发（一般是程序完结）

    def __call__(self, *args, **kwargs):  #创建的实例可以调用、调用时触发该函数
        pass

    def __iter__(self):  #可以让实例可迭代，必须返回可迭代类型.还必须有一个next方法
        return self
    def __next__(self):  #和iter结合使用
        if self.key1>=100:
            raise StopIteration('越界了')
        self.key1 += 1
        return self.key1

    def __enter__(self):  #与__exit__连用，构成上下文管理器
        return self    #使用with 创建类实例时会触发enter执行.必须返回值


    def __exit__(self, exc_type, exc_val, exc_tb):#遇到错误也会自动触发该函数
        pass  #with语句块结束时会触发exit执行
        #exc_type指异常类型，exc_val指错误提示，exc_tb指错误跟踪。默认均为None
        #根据类别自行定义清理功能
        return True   #会消除异常提示



print(c.__module__) #实例的来源模块
print(c.__class__) #实例的创建类

