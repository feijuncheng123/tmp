li1=[1,2,3,'a','b',[5,'feijuncheng']]
li2=list("feijuncheng") #参数需为可迭代类型
#切片、索引，支持for循环
#列表可在原数据上进行修改
li1[1:3]=[10,11] #可以对切片段进行赋值，修改相应的元素

del li1[5][0]  #方式一，删除元素
li1.pop(4)  #从列表中取出相应索引的值，并从原列表删除。位置默认为最后一个。原位修改
li1.remove('b')  #删除列表中的指定值，左边优先删除。原位修改
li2.clear()  #清空原列表，原位修改

arg="参数"
li2.append(arg) #在列表最后添加一个元素。在原数据修改，不返回新值
li2.extend("可迭代对象")  #将可迭代对象扩展进列表中，原位修改
li1.insert(2,"c") #在指定索引位置2处插入"c"，原位修改


li2.copy() #浅拷贝，返回新值。
li2.reverse() #将列表顺序反转，原位修改
li2.sort(reverse=False)  #排序，升序或者降序。还有另外两个参数，可根据函数排序，原位修改


li1.count('a')  #计算元素个数
li1.index('a') #获取值的索引位置，返回最左边第一个位置

#元组
t1=(1,2,3,4) #可尾部加个逗号
t2=tuple("可迭代序列")
#元组元素不可被增加、修改、删除
#满足切片，索引。切片返回元组。可迭代
t1.count(1) #计算元素出现次数
t1.index(3) #获取元素的索引值




