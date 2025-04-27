import numpy as np

data=np.genfromtxt('data.txt',delimiter=',',dtype=str)
np.array([1,2,3,4,5])
npary=np.array([[1,2,3],[4,5,6],[7,8,9]])
print(data.shape)  #行列数
dt=data.dtype  #序列中的数据类型。必须保持一致。只要有一个字符串，所有都会变字符串
data.dtype.name  #类型名称
data.astype(int)  #值类型的转换


sli=data[3,4]   #切片，某个值
data[:,2]  #选定第二列所有值
data[:,0:2]  #取0到2列的所有值
area=data[1:3,0:2]  #取相应区域值
area==6  #判断区域内任何一个值是否等于6，然后返回bool值组成的相同大小的矩阵
area[area==5]  #筛选出等于6的所有元素

#二维数组切片
a=np.arange(10,60,10).reshape((-1,1))+np.arange(0,6,1)
x1=a[(1,2,3),(4,5,6)]  #选取1,2,3行，4,5,6列交叉的元素
x2=a[3:,(4,5,6)]  #3行及以后，4,5,6列位置的切片



(area==6)&(area==5)  #逻辑与
(area==6)|(area==5)  #逻辑或

area.min()  #区域最小值
npary.sum(axis=1)  #对每行进行求和。设为0则为对列秋活

np.arange(15)  #生成一个0到14的矩阵
np.zeros((3,5))  #生成3行5列元素全部为0的矩阵.需要传入元组
np.ones((2,3,4),dtype=int)  #构造一个全部为1的三维矩阵。层数为2，每层的行列数为3和4
np.arange(10,30,5)  #构造一个10开始，30结束（不含30），步进为5的序列。10,15,20,25
y=np.random.random((2,3))  #生成一个2行3列的-1到1之间的随机数序列
x=np.arange(15).reshape(3,5)  #设定形状为3行5列
x.ndim  #返回维度，1维2维等
x.size  #共有几个元素

np.linspace(0,9,10)  #从0到9，按相同间隔取10个值。（含两端）


x=x-1  #所有元素都减1

#相乘：两个矩阵相乘，如果形状一样，按照相同位置两两相乘
x.dot(y) #矩阵相乘：x行的元素和y的列的元素两两相乘后相加得出元素。x一行的元素数必须和y一列的元素数相等，否则报错

np.exp(x)  #求冥
np.sqrt(x)  #开根号
np.floor(x) #向下取整
x.ravel()  #拉平为1维。在原数据上操作
x.shape=(6,2)  #拉平后，然后转换成相应的形状  。（在原始数据上修改）
x.T  #行列转置

x.reshape(3,-1)  #-1表示自动计算。将数据转化为3行，但列数自动计算.返回新对象。但是浅拷贝，改变视图而已

np.hstack((x,y))  #按行（左右）拼接两个矩阵
np.vstack((x,y))  #按列（上下）拼接两个矩阵

np.hsplit(x,3)  #按照行进行切分成3等份（左右切分）
np.vsplit(x,3)  #按列进行切分成3份（上下切分）
np.hsplit(x,(3,2,))  #按行切分，前两份分别有3列和2列，剩下的其他分

z=x.view()  #浅拷贝x。共用值，但形状等不共用。
z=x.copy()  #深拷贝。

x.argmax(axis=0)  #每一列的数据的最大值的索引号，返回列表。

np.tile(x,(3,4))  #对x的行列分别扩展3倍和4倍
np.sort(x,axis=1)  #对每一行进行排序
np.argsort(x)  #返回每一行的元素，按行内的大小排序的索引值。比如[5,3,4,2],返回[3,1,2,0],即最小的2的索引为3，3的索引为1


np.logspace(1,2,10,endpoint=True,base=10)
#生成等比数列，base参数为基数，1代表base参数10的1次方，为初始值，2代表2次方为终值，包含终点。相邻数之间的比例相等

a='abcd'
np.fromstring(a,dtype=np.int8)
#生成序列

np.ones_like(x)  #生成一个形状如传入的x的形状，但所有元素为1的序列
np.zeros_like(x)  #同上，生成0的

a = np.array([1, 2, 3])
b = np.array([4, 5, 6])
c = np.c_[a,b]  #将a、b按列进行合并（合并后为两列）
d=np.r_[a,b]  #将a、b按行进行合并（合并后为1列，但有6行）

