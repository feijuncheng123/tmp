name="feiJunCheng"
'''一旦创建，不能修改自身。仅能重新创建'''
a=name[-3:] #索引，切片
l=len(name)

name.capitalize() #首字母大写
name.lower()  #全部小写，casefold方法相同，但适用其他语言
name.upper()  #同上

#填充
name.center(20,"_"默认空白)  #20为包含name在内的总长度，如果短于name，则不添加任何单位，长于则在两侧平均添加。默认值必须为字符串
name.ljust(20,"_") #同上，但是往右边填充下划线
name.rjust(20,"_")  #与上同理

#去除空格
name.strip("arg")  #默认去除字符串两端空格或非打印字符。如果给定参数，参数必须为字符串。且会去重参数里包含的字符，无关顺序。但开头字符一定要有
name.lstrip()  #去除右边空格
name.rstrip()  #同上

#分割
name.split("j",2) #按j分割字符串，分割2次。如果没有指定次数，则默认全部分割
name.rsplit() #同上，但从右边开始分割
name.splitlines(True or False) #只根据换行符分割。去除或者包含换行符

name.partition("n")  #同为分割，但只能分割1次，分为三部分。包含分割的元素n为一部分
name.rpartition("n") #同上

#替换
name.replace("n","N",1)  #替换n为N，只替换1个，如没有输入替换次数，则默认全部替换

#计数与查找
name.count("en")  #计数，计算en出现的个数。大小写敏感
name.endswith("ng") #返回bool值，是否以ng结尾
name.startswith("ei")  #同上，是否以ei开头
name.find("ch",开始位置(默认首位),结束位置(默认结尾))  #从开始位置往后找，找到后返回第一个找到的位置。找不到返回-1
name.index()  #同上查找字符。但是找不到直接报错。

#格式化
name.format() #格式化文本。返回新值。稍后系统说
name.format_map() #按照字典传参进行字符串格式化，稍后说

#类型判断
x="abc~!^ew234"
d="123"
x.isalnum() #是否全部为数字或字母（不能包含特殊符号）
x.isalpha() #是否只包含字母或者汉字
x.islower() #是否全部为小写
x.isupper() #同上
x.isprintable()  #是否包含非打印字符，如\t,\n等非显示字符
x.isspace() #是否全部为空格，\t\n等。空字符串，

#数字判断
isdecimal()   #需严格为十进制数字。不能含小数点
isdigit()  #还包含了罗马数字（ VIII，XXI之类），单字节二进制数
isnumeric() #包含罗马数字，汉字数字（一、壹等）。但不包含二进制数

#标识符判断（用于判断变量名等是否符合python规范）
var="fun_c"
var.isidentifier()  #字母、数字、下划线。也包含汉字（汉字也可以作为变量名使用）
import keyword
keyword.iskeyword(var) #用于判断字符串是否为关键字，可以与isidentifier结合使用

#拼接
a="_"
b=a.join(name) #name需是可迭代的序列
"f_e_i_j……"  #用字符串a，将迭代序列name中的每个元素拼接起来


y="abcdefgex\tdfvknfvlas"
l=y.expandtabs(5)  #每5个进行计数，遇到\t缩进符，不足5个则用\t补充到5个。刚好满足则\t就填5个空格，然后往后计数。主要用于表格字段对齐等。


#字符串格式化
    #百分号格式化
s1="I am %s,my age is %d"%('feijuncheng',32) #%号后空号内传入的值，实际为元组，可改为%tuple()进行传值
#%s中s代表字符串，可以接受任何值。%d则只能接收整型数字。
#%f接收浮点型数值。可%.2f设定小数点后2位（四舍五入)
#%%两个百分号，则为输入百分号
s2="I am %(name)-60s,my age is %(age)d"%{'name':'feijuncheng','age':18}
#在百分号与数据类型之间的括号，表示字典的key值，然后以字典形式输入值
#-60代表总共60个位置，然后左对齐。+号的则为右对齐


    #format方法格式化
string1="I am {n}, my age is {age}" #需要用大括号指定占位符
s1=string1.format(n="feijuncheng",age="32") #用占位符的名称作为参数进行字符串替换，
string2="I am {0}, my age is {1}" #按顺序指定占位符
s2=string2.format("feijuncheng","32") #安装顺序填充

string1="I am {n}, my age is {age}"
s3=string1.format_map({"n":"feijuncheng","age":"32"})  根据字典传入参数
print(s1,string)
s4="I am {0[0]}, my age is {1[2]}".format(['a','b','c'],[1,2,3])
#参数以元组形式传入，大括号内的序号按照索引取值

#翻译替换
对应关系 = str.maketrans("单个字典或者两个等长字符串","字符串2") #将字符之间进行对应
name.translate(对应关系) #根据上面对应关系将name相应的字符转换为对应的字符

