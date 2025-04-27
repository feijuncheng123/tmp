import tensorflow as tf

#tensor（张量）：
#1、任何维度的数组均称为张量，以阶表示张量。中括号层数即为阶数

a=tf.constant([1,2,3])  #创建一个常量一阶张量
b=tf.constant([4,5,6])

c=a+b
print(c)
#显示Tensor("add:0", shape=(3,), dtype=int32)
#其中：add为节点名，0指第0个输出，小括号中，3表示第一个维度有3个元素（长度）
#计算图（Graph）：只创建了运算方程，但并没有执行计算
#会话（Session）：创建会话执行计算

v=tf.Variable(tf.random_normal([2,3],stddev=2,mean=0,seed=1))
#创建一个变量v，承载一个2行3列的正太分布随机矩阵，标准差为2，均值为0，随机数种子为1。
#Variable创建的变量会随着计算变化
v1=tf.placeholder([2,3]) #占位变量，在执行计算时需要使用feed_dic以字典形式传入值
tf.truncated_normal() #去掉过大离群点的正态分布
tf.random_uniform() #产生平均分布
tf.zeros() #全0数组
tf.ones() #全1数组
tf.fill([2,3],6)  #定值为6的[2,3]数组


