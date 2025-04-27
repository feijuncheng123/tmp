import json
#json字符串：多种语言都遵守的一种数据规范。

dic={"k1":"v1","k2":"v2"}  #必须双引号。如果用单引号,json会转化为双引号，如果混用可能导致错误
#json.loads('数据对象',encoding='编码')  #将json字符串转化为字符串、列表、字典等
dic1={'k1':'v1','k2':'v2'}
a=json.dumps(dic1)   #将数据对象转化为json对象。符合语法的对象会自动转化为json
b=json.loads(a)  #将json字符串转化为数据对象。如果json转化后不符合相应类型语法，则报错

c=json.dump('数据对象','目标文件')  #将数据对象转化为json，并直接写入到文件中
d=json.load("目标文件")  #直接读取文件内容，并转化为数据对象

#元组：仅python使用。dumps成json格式时，会转化为列表形式。
#元组形式的字符串（带双括号），loads时会报错

import pickle
#pickle为仅python内部的一种数据格式，可以把各种对象封装进文件，包括函数、类等
#pickle转化后为字节

#load或dump时，必须与b模式打开

pickle.dumps()
pickle.dump()
pickle.loads()
pickle.load()     #使用方法与json相同

import shelve
#创建一个文件，然后把各种数据类型以字典形式写入文件
#会生成三个文件：dat、bak、dir。
f=shelve.open("文件名") #创建一个文件，以字典形式操作
f['k1']={'name':'aaa','age':18}
f.close()
#使用时
f1=shelve.open("文件名") #同样需要打开
f1.get('k1')['age']  #取出相应值
