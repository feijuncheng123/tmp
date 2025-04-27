import abc  #该模块主要在定义接口规范时使用

#继承：可以通过继承重写内置类型方法（如列表、元组、字典方法）
class level1:
    name='父类'
    def __init__(self,var1,var3):
        print('父类')
        self.var1=var1
        self.var3=var3

    def fun1(self):
        print(self.var1)
        print("父类函数")

    @abc.abstractmethod      #该装饰器表示父类中的这个函数功能未定义，需要在子类中自行定义（强制要求子类定义）
    def fun3(self):
        pass
    #接口继承：接口函数，但功能未定义。必须在子类中定义。凡是继承的子类都必须定义

class level2A(level1):
    #pass
    name = '子类2A'     #子类中的变量名与父类重复，并非覆盖父类属性，而是自定义自己的属性
    def __init__(self,var1,var2,var3):     #子类需要继承父类属性，需要self参数，用于将自身传到到父类属性和函数中
        print("子类2A")
        level1.__init__(self,var1,var3)  #继承父对象的属性方法一（self不能少）。重用父类逻辑
        self.var1=var1   #并非覆盖父类变量（是自定义自己属性）
        self.var2=var2   #私有属性

    def fun3(self):
        level1.fun1(self)     #调用父类的方法



a=level2A('子类调用')
print(level1.__dict__)
print(level2A.__dict__)


class level2B(level1):
    def __init__(self,var1,var3):
        super().__init__(var1,var3)   #调用父类属性方法二（推荐）：无需父类名（self省略)。重用父类逻辑
        #也可以：super(level2B,self).__init__(var1,var3) 增加两个参数
        print('子类2B')

    def fun3(self):
        super().fun1()   #用super代替父类名调用父类函数，无需self

class level3(level2A,level2B):
    #pass
    def __init__(self):
        print('子类3')

#level3继承顺序：py3中深度优先。即：一、level3继承了level2A和level2B，level2A又继承了level1。
#则：先向level2A查找，然后像level1查找，再然后才向level2B查找
#二、level3继承了level2A和level2B，level2A和level2B都继承了level1，则先向level2A查找，然后像level2B查找，在向level1找

#查看类的继承顺序可以用__mro__方法：
print(level3.__mro__)


#多态
#指多种子类可以共用一个方法。主要由继承实现

#封装:
# 简单地说，将实现逻辑封装到变量中.让外部不可见
#_带单下滑杠的变量：为只作为类或者模块代码内部使用的变量。不能作为接口提供给外部访问，或者作为特殊目的使用（私有变量）
#单下滑杠变量：只有当前类和子类能够使用。
#__以双下划綫开头的变量（不含双下滑线结尾）：py会重命名。调用时需要使用_类名__变量名进行访问（开头添加_类名）
#双下滑杠变量：只有类对象自己能访问，子类对象不能访问到这个数据

class encapsulation:
    __age=32
    _sex='men'
    def __init__(self,height):
        self.__height = height
    def get_age(self):    #接口函数或者访问函数
        print(self.__age)   #双下划綫：可以通过内部进行访问。
    def get_sex(self):
        print(self._sex)   #单下划线：也同样内部可以取到

l = encapsulation(30)
print(l._encapsulation__age)   #通过重命名取到
print(l._sex)


