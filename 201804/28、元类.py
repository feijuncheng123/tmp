#一切皆对象：对象的尽头是元类

class cls1:
    pass

#cls1是type类的实例。type类是元类
#创建类实例：
type('cls1',(object,),{'属性字典':'值'})  #第二个参数为父类
#可以通过继承type自定义元类

class mytype(type):   #定义元类
    def __init__(self,*args,**kwargs):  #创建其他类时，等同于创建mytype实例
        self.name=n
        super().__init__(*args,**kwargs)
    def __call__(self, *args, **kwargs):   #创建类时，会触发该函数执行
        pass

class cls2(metaclass=mytype):  #使用自定义元类创建类。相当于mytype('cls2',('父类‘，),{属性字典})
    pass
