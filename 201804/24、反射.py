class clas:
    var1="第1个属性"
    def __init__(self,name,nomber):
        self.name=name
        self.nomber=nomber

    def fun1(self):
        print("函数1")

    def __getattr__(self, item):
        print("getattr方法")
        return clas.__getattr__(self,item)  #返回时需要用基类调用属性，否则会陷入无穷递归

    def __delattr__(self, item):
        print("删除属性触发")
        self.__dict__.pop(item)   #不能直接删除。否则陷入递归循环

    def __setattr__(self, key, value):
        #self.key=value   #不能如此定义。否则一直触发，会陷入递归循环
        self.__dict__[key]=value   #如此才行（会覆盖系统默认设置）
        print('添加或设置属性时触发')


obj=clas('abc',1)
a=hasattr(clas,'var1')  #clas类中是否可以调用fun1的方法或者属性.传入类名时无法检测实例属性
b=hasattr(obj,'var1')  #检测实例属性,也能检测类属性
print(a,b)

fun=getattr(clas,'fun2','默认返回值')  #取出属性值。如果是函数直接返回可运行的函数名。如果没有则返回默认值，没设定则报错
#默认返回值也可以是函数

setattr(obj,"height",160)  #为对象添加属性，相当于obj.height=160.如果变量已存在，则修改值。
#设定值也可以指定为函数名

delattr(obj,'height')  #删除对象的特定属性

#同样适用于其他对象
#类内置__getattr__方法：设定后，如果实例调用不存在的属性或方法，则会触发__getattr__方法执行（不是报错）
#可以使用__getattr__方法对类功能或函数功能进行扩展。
#类内置__delattr__方法：删除（del）属性时会触发执行
#类内置__setattr__方法：设置值时触发。（实例传入__init__值时都会触发）。（会覆盖系统默认设置，但可以对设置值类型等进行限制）

#类内置方法只有实例调用时才会触发

