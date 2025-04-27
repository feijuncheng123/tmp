import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data

mnist=input_data.read_data_sets("MNIST_data/", one_hot=True)

def weight_variable(shape):
    initial = tf.truncated_normal(shape, stddev=0.1)
    return tf.Variable(initial)


def bias_variable(shape):
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)


def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='SAME')
#tf.nn.conv2d函数：用卷积核W对图像x进行卷积运算。
#其中：1、第一个参数input：指需要做卷积的输入图像，它要求是一个Tensor，
# 具有[batch, in_height, in_width, in_channels]这样的shape，具体含义是[训练时一个batch的图片数量,
# 图片高度, 图片宽度, 图像通道数]，注意这是一个4维的Tensor，要求类型为float32和float64其中之一。
#2、第二个参数filter：相当于CNN中的卷积核，它要求是一个Tensor，
# 具有[filter_height, filter_width, in_channels, out_channels]这样的shape，
# 具体含义是[卷积核的高度，卷积核的宽度，图像通道数，卷积核个数]，要求类型与参数input相同，
# 有一个地方需要注意，第三维in_channels，就是参数input的第四维（要求一致）。
# 每张图会跟每个卷积都进行运算。最后生成batch*out_channels张图片
#3、第三个参数strides：卷积时在图像每一维的步长，这是一个一维的向量，长度4。
#4、第四个参数padding：string类型的量，只能是"SAME","VALID"其中之一，SAME指生成后的特征图形状与原始图相同，
# VALID则卷积核从原始图边界开始点乘卷积。
#5、第五个参数：use_cudnn_on_gpu:bool类型，是否使用cudnn加速，默认为true

#返回tensor。shape为[batch, *, *, out_channels]

def max_pool_2x2(x):
    return tf.nn.max_pool(x, ksize=[1, 2, 2, 1],
                          strides=[1, 2, 2, 1], padding='SAME')
#tf.nn.max_pool：池化函数。
#1、第一个参数value：需要池化的输入，一般池化层接在卷积层后面，
# 所以输入通常是feature map，依然是[batch, height, width, channels]这样的shape
#2、第二个参数ksize：池化窗口的大小，取一个四维向量，一般是[1, height, width, 1]，
# 因为我们不想在batch和channels上做池化，所以这两个维度设为了1
#3、第三个参数strides：和卷积类似，窗口在每一个维度上滑动的步长，一般也是[1, stride,stride,1]
#第四个参数padding：和卷积类似，可以取'VALID' 或者'SAME'
#返回一个Tensor，类型不变，shape仍然是[batch, height, width, channels]这种形式


x = tf.placeholder("float", shape=[None, 784])
y_ = tf.placeholder("float", shape=[None, 10])




W_conv1 = weight_variable([5, 5, 1, 32])
b_conv1 = bias_variable([32])

x_image = tf.reshape(x, [-1,28,28,1])

h_conv1 = tf.nn.relu(conv2d(x_image, W_conv1) + b_conv1)
h_pool1 = max_pool_2x2(h_conv1)

W_conv2 = weight_variable([5, 5, 32, 64])
b_conv2 = bias_variable([64])

h_conv2 = tf.nn.relu(conv2d(h_pool1, W_conv2) + b_conv2)
h_pool2 = max_pool_2x2(h_conv2)

W_fc1 = weight_variable([7 * 7 * 64, 1024])
b_fc1 = bias_variable([1024])

h_pool2_flat = tf.reshape(h_pool2, [-1, 7*7*64])
h_fc1 = tf.nn.relu(tf.matmul(h_pool2_flat, W_fc1) + b_fc1)

keep_prob = tf.placeholder("float")
h_fc1_drop = tf.nn.dropout(h_fc1, keep_prob)
#dropout：一般用于全连接层，为防止过拟合，随机剪除部分神经元，让其不参与运算，但会保留权重值。可能在下一次训练时又参与进来。
#但在测试及验证中：每个神经元都要参加运算，但其输出要乘以概率p。
#第一个参数x：指输入。
#第二个参数keep_prob: 设置神经元被选中的概率,在初始化时keep_prob是一个占位符。
# tensorflow在run时设置keep_prob具体的值，例如keep_prob: 0.5

W_fc2 = weight_variable([1024, 10])
b_fc2 = bias_variable([10])

y_conv=tf.nn.softmax(tf.matmul(h_fc1_drop, W_fc2) + b_fc2)

sess = tf.InteractiveSession()

cross_entropy = -tf.reduce_sum(y_*tf.log(y_conv))
train_step = tf.train.AdamOptimizer(1e-4).minimize(cross_entropy)
#用更加复杂的ADAM优化器来做梯度最速下降
correct_prediction = tf.equal(tf.argmax(y_conv,1), tf.argmax(y_,1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))
sess.run(tf.initialize_all_variables())
for i in range(20000):
  batch = mnist.train.next_batch(50)
  if i%100 == 0:
    train_accuracy = accuracy.eval(feed_dict={
        x:batch[0], y_: batch[1], keep_prob: 1.0})
    print("step %d, training accuracy %g"%(i, train_accuracy))
  train_step.run(feed_dict={x: batch[0], y_: batch[1], keep_prob: 0.5})

print("test accuracy %g"%accuracy.eval(feed_dict={
    x: mnist.test.images, y_: mnist.test.labels, keep_prob: 1.0}))



