#匿名函数：lambda
func1=lambda x: x+1   #参数x，输入x值，然后返回x+1的值
func2=lambda x,y,z: x+y+z  #传入3个参数，然后整体输出冒号后面的值

a=func2(1,2,3)  #将返回值赋值给a

#重要应用
#匿名函数可以直接作为整体传入到其他函数（包括内置函数）的参数中，对数据进行处理后返回，作为参数传入

#map函数：序列元素处理映射
a1=map(lambda x:x**2,[1,2,3,4,5])
#map函数将传入的列表元素，传入第一个参数中处理，返回迭代器
l1=[1,2,3]
l2=[6,7,8]
map(lambda x:l1.append(x),l2)  #往l1中添加l2的元素。但是由于append方法返回none。因此map函数只返回none。但是l1列表添加了


#filter函数：序列元素筛选映射
filter(lambda x:x>3,[1,2,3,4,5])  #筛选符合条件的序列元素。参数中的函数必须返回bool值

#reduce函数：序列聚合函数
from functools import reduce
reduce(lambda x,y:x+y,[1,2,3,4,5],10)  #对传入的序列元素进行聚合处理。10为初始值。
#传入函数需有两个参数，聚合方法为从左至右，前两个值聚合处理后生成新值，与后面的值再次聚合

#内置函数
abs(-1)  #取绝对值
all('序列') #输入的序列的所有元素都为True，则返回True，如果有一个为False，则为假
any('序列') #同上，任何一个为真则返回真
bin(3) #将十进制转化为二进制
b=bytes('abc',encoding='utf8') #把字符串转换为字节
b.decode('utf8')  #解码为字符串
bool();str();int();list(),dict(),tuple(),set()
float()
chr(50)  #根据ascii码表将数字转化为对应的字符
ord("单个字符")  #chr的反操作
dir('变量或类型') #输出某个对象的所有方法
divmod(10,3)  #取出商数和余数，以元组形式
enumerate("序列") #将序列的索引与元素捆绑为元组，再以列表形式输出
eval()  #去除两端的引号，以执行
hash('对象')  #可hash对象均为不可变对象，不可hash对象均为可变对象
help()
hex(20)  #十进制转为十六进制
oct(20)  #十进制转为八进制
id("对象")  #对象的内存id
isinstance('对象',str)  #判断对象是否是输入的类型的实例
len()
iter()  #迭代器
globals() #输出全局变量
locals()  #输出本地（局部）变量
max("序列")  #取最大值
min("序列")
sum('序列')
zip('序列一','序列二','……')  #将多个序列对应的元素一一捆绑成元组，然后组成列表。长度以最短列表为准
next()
open()
pow(10,3,2)  #两个参数，相当于10**3，三个参数则乘方后取余数
range()
repr('对象')  #面向对象使用
reversed('序列')  #反转排序。返回新值
round(3.5)  #四舍五入。保留小数点
slice() #切片
s='feijuncheng'; sl=slice(3,5);s[sl]  #取出切片。更加灵活
sl.start;sl.step;sl.stop  #取出切片的元素
sorted('序列') #排序
type("对象")
vars('对象') #如果没有参数，则返回局部变量的名称和值，如果有参数，则查看该对象的方法，以字典返回
__import__("模块名称（字符串）")

if __name__ == "__main__":  #判断当前模块是否是启动的主模块（即不是其他模块文件调用）
    print('启动模块')
