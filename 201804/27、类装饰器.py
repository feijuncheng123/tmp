#描述符类对装饰函数内变量也适用
def deco(*args):  #装饰器原理就是将被装饰对象以参数形式传入装饰函数，处理后在返回相同名字的对象
    def fun(obj):
        obj.name='obj'
        return obj
    return fun

@deco(*args)   #类似于函数装饰器，类装饰器是将类对象作为参数放入装饰器函数中。然后又把返回值赋值给与类名相同的变量
class cls1:
    pass

#描述符、装饰器本质一样
#@classmethod,@property,@staticmethod等方法实质为描述符（或者装饰器）

