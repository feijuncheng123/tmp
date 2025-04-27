file=open(r"d:\文件路径\文件名称.txt",'r+',encoding="gbk")
#文件可以直接以迭代形式读取
file.read()  #读取文件所有内容
file.readable() #文件是否可读。w模式下为false
file.readline() #一次读取一行，会改变指针
file.readlines() #按行读取所有内容，返回列表
file.close()  #处理完毕后需要关闭。回收内存

f2=open(r"d:\文件路径\文件名称.txt",'w+',encoding="utf8")
#写模式下无法读取。
#写模式下，如果文件存在，则会把文件彻底清空，如果不存在，则新增一个文件。
f2.write("字符串")  #写入内容，如果按行写入，需要加入换行符
f2.writelines(["列表\n"])   #以列表形式传入多行，然后写入。必须均为字符串
f2.close()

f3=open(r"E:\python_study\untitled\1.txt","a")  #以追加模式打开
#追加模式不可读取。
#指针自动位于最后，写入时自动在文件尾部添加
f3.write("string")  #会返回写入的字符数
f3.close()

f4=open(r"E:\python_study\untitled\1.txt","r+")
#以读取并且追加写入的方式打开。
#注意：光标打开时默认在0位。读取后会跳到相应位置末尾。
#注意：追加内容会在光标位置处追加，而且会覆盖后面内容（内容不会自动往后跳）
f4.close()

with open(r"E:\python_study\untitled\1.txt","r+") as f5,open(r"E:\python_study\untitled\2.txt","r+") as f6:
    pass
#文件管理器，无需关闭操作

#以二进制形式打开
f7=open(r"E:\python_study\untitled\1.txt","rb") #无需编码
f7.read().decode("utf8") #将内容解码为字符串
f7.close()

f8=open(r"E:\python_study\untitled\2.txt","wb")
f8.write("你好".encode('gbk'))  #字符串需要制定编码模式才能写入二进制文件
f8.encoding  #返回文件打开编码（非原始数据编码）
f8.closed  #判断是否已关闭
f8.flush()  #将文件内容刷新进入硬盘保存

#编码问题
#假设原始文件以utf8编码（或其他），open中以gbk打开，可以打开，但无法读取read()，但可以写入。导致两种编码字符混合在一个文件


#光标
#光标位置按照字节移动
f9=open(r"E:\python_study\untitled\2.txt","r+")
f9.tell()  #返回当前光标位置
f9.readline() #读取后，会改变光标位置
f9.seek(5)  #指定光标到特定位置

f9.seek(5,1)  #1代表相对位置，往当前光标后偏移5个位置。用2代表按照尾部的相对位置,正数为往后，负数为往前。默认为0，绝对位置。必须用带b模式打开文件
#seek指定2，默认从最后一行读取，文件较大时可以节省空间，无需整个文件全部读取
f9.read(5)  #读取光标后的4个字符
f9.truncate(10)  #截取从开头到10的位置的数据保留，其余数据删除。光标无效

f9.close()