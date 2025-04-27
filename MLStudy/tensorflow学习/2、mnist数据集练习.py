import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data

mnist=input_data.read_data_sets("MNIST_data/", one_hot=True)

x=tf.placeholder('float',[None,784])
#None表示此张量的第一个维度可以是任何长度的

W = tf.Variable(tf.zeros([784,10]))
b = tf.Variable(tf.zeros([10]))
#一个Variable代表一个可修改的张量，存在用于描述交互性操作的图中。需要有个初始值
#可以用于计算输入值，也可以在计算中被修改

y = tf.nn.softmax(tf.matmul(x,W) + b)
#softmax(x)=exp(x)/∑exp(x)
#softmax将矩阵中某一个维度归一化
y_ = tf.placeholder("float", [None,10])
cross_entropy = -tf.reduce_sum(y_*tf.log(y))
#cross_entropy为交叉熵，为此处的loss函数。y为预测概率，y_为实际分布（0和1）
train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)
#GradientDescentOptimizer为梯度下降算法，0.01为学习率。最小化cross_entropy交叉熵函数
init = tf.initialize_all_variables()
#初始化创建的变量，返回构建了初始化变量的op操作

with tf.Session() as sess: #启动运算
    sess.run(init)   #初始化变量（将初始值赋予变量）
    for i in range(1000):  #循环1000次
        batch_xs, batch_ys = mnist.train.next_batch(100)
        #随机选取100张图片（batchsize)参与loss函数运算。
        #next_batch为从数据集中选取batchsize的下一个样本量，返回特征矩阵和目标向量
        sess.run(train_step, feed_dict={x: batch_xs, y_: batch_ys})

    correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(y_, 1))
    #tf.argmax为给出某一tensor对象在某一维度，某一坐标轴上的最大值的索引值，一维的向量则直接给出最大值索引
    #correct_prediction在此处为判断预测的类别和实际的类别是否相同。1是指横轴

    accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))
    #计算正确与错误的平均值
    print(sess.run(accuracy, feed_dict={x: mnist.test.images, y_: mnist.test.labels}))
