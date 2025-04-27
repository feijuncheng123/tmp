info={'name':'feijuncheng','age':32}
d=dict([(1,2),(3,4),(1,5),(3,6)])
#可变对象不能作为key，如列表、字典等。元组则可以成为key
#key不能重复了，后输入的key值将覆盖前面的值

name=info['name'] #取出相应value

del info['age'] #删除键值对
for item in info: pass  #默认循环key

info.keys()  #key的迭代器
info.values() #value的迭代器
info.items()  #键值对的迭代器

info.clear()
info.copy()
v=info.fromkeys('迭代序列',value=None)
v=dict.fromkeys('迭代序列',value=None)  #以序列中的元素 作为key创建字典。设定默认值
v1=info.get('key',None) #取key的值，如果不存在不报错，返回设定的值。默认None
info.pop('key',None)  #取出相应值并删除，如不存在，返回默认值
info.popitem()  #随机删除并返回相应值，以元组形式返回
info.setdefault('heigh',168) #设置值，如果已存在则不改变，并获取对应值。如果不存在，则添加值，并返回值
info.update({'age':18,'hometown':'yunnan'}) #更新相应值，如不存在，则添加进去。
info.update(age=18,hometown='云南') #同上


#集合（可变类型）
s=set('可迭代序列，必须为不可变类型') #集合中不能包含列表、字典、和其他集合
s1={'s','e','t'}
s2=frozenset("定义不可变集合") #不可追加、不可删除、不可更改
#集合：不重复元素、无序、无索引切片、可迭代

s.add('添加元素') #添加元素，会被去重。只能更新一个值
s.clear()
s.copy()

s.pop() #随机取出并删除
s.remove("特定元素") #删除特定元素，不存在则报错
s.discard("特定元素")  #删除元素，不存在也不会报错
#求交集
s.intersection(s1) #求交集，互相交换结果一样
交集=s&s1
#求并集
s.union(s1)  #求并集，互相交换结果一样,返回新值
u= s|s1 #同上，求并集
#求差集
r=s-s1
s.difference(s1)
#交叉补集（并集减交集，或者两差集之和）
s.symmetric_difference(s1)
b=s^s1

#带update的方法
s.intersection_update(s1)  #用交集更新s，不返回新值
s.difference_update(s1)  #差集并更新s，不返回新值
s.symmetric_difference_update(s1) #交叉补集更新s1，不返回新值

s.isdisjoint(s1)  #如果交集为空，则为True
s.issubset(s1) #如果s是s1的子集，则返回True
t=s<=s1
s.issuperset(s1) #如果s是s1的父集，则返回True
su=s>=s1

s.update("可迭代序列") #将迭代序列中的值批量更新进s中

s.issubset()


