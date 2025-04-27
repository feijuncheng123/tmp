import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import time
import numpy.random

data=pd.read_csv(r"E:\python\Pro1\untitled\数据挖掘与机器学习\data\LogiReg_data.txt",
                 header=None,names=["field1","field2","key"],engine="python")
print(data.head())
print(data.shape)
sample1=data[data["key"]==1]
sample0=data[data["key"]==0]
print(sample1.shape)
(fig,ax)=plt.subplots()  #画图。创建图形对象
print(ax)
ax.scatter(x=sample1['field1'],y=sample1["field2"],s=50,c='g',marker='P',label='admitted')
ax.scatter(sample0['field1'],sample0["field2"],50,c='r',marker='x',label='not admitted')
ax.legend()
ax.set_xlabel("field1")
ax.set_ylabel("field2")
plt.show()

def sigmod(Z): #这个为逻辑回归需要的sigmod函数
    return 1/(1+np.exp(-Z))  #np.exp的参数传入一些array_like的值，然后返回e对每个值的平方(array数组）

num=np.arange(-10,10,1)
fig1=plt.figure(figsize=(12,4))
ax=fig1.add_subplot(1,1,1)
ax.plot(num,sigmod(num),c="r")
plt.show()

def model(X,theta):   #这个为求X矩阵和θ的乘积的sigmod值，返回列向量
    return sigmod(np.dot(X,theta.T))   #dot为矩阵相乘运算,theta.T为转置为列向量

data.insert(0,'ones',1)  #在data的第0列插入一列，值完全为1
orig_data=data.as_matrix()  #转化为numpy运算的array（数组）
col_num=orig_data.shape[1]
X=orig_data[:,0:col_num-1]  #参数列
y=orig_data[:,col_num-1:col_num]   #目标列
theta=np.zeros([1,3])  #尽管行数为1，theta依然是二维数组，显示为[[1,2,3]]形式
print(X[:5])
print(y[:5])

def Likelihood_function(X,y,theta): #这个函数为最大似然函数，θ目前为0。y值为目标值，为0和1组成的常量
    left=np.multiply(-y,np.log(model(X,theta)))
    right=np.multiply(1-y,np.log(1-model(X,theta)))
    return  np.sum(left-right)/(len(X))

sig=model(X,theta)
print("sig:",sig)

value=Likelihood_function(X,y,theta)
print("value:",value)


def gradient(X,y,theta):
    #这个函数为最大似然函数的每个θ值的偏导数函数。在偏导数上，最大似然函数的梯度最大（但是用了-1/m转化为求最小值）
    grad=np.zeros(theta.shape)
    error=(model(X,theta)-y).ravel()
    for j in range(len(theta.ravel())):
        term=np.multiply(error,X[:,j])
        grad[0,j]=np.sum(term)/len(X)
    return grad

grad=gradient(X,y,theta)
print(grad)

STOP_ITER=0
STOP_COST=1
STOP_GRAD=2
def stopcriterion(ml_type,value,threshold):  #此函数为下降终止条件
    if ml_type == STOP_ITER: return value>threshold
    elif ml_type == STOP_COST:return abs(value[-1]-value[-2])<threshold
    elif ml_type == STOP_GRAD:return np.linalg.norm(value) < threshold

def shuffleData(data):  #此函数为打乱原始数据，随机化
    numpy.random.shuffle(data)
    cols=data.shape[1]
    X=data[:,0:cols-1]
    y=data[:,cols-1:]
    return X,y

def descent(data,theta,batchSize,stopType,thresh,alpha):
    init_time=time.time()
    i=0
    k=0
    X,y=shuffleData(data)
    grad=np.zeros(theta.shape)
    costs=[Likelihood_function(X,y,theta)]

    while True:
        grad = gradient(X[k:k + batchSize], y[k:k + batchSize], theta)
        k += batchSize  # 取batch数量个数据
        if k >= n:
            k = 0
            X, y = shuffleData(data)  # 重新洗牌
        theta = theta - alpha * grad  # 参数更新
        costs.append(Likelihood_function(X, y, theta))  # 计算新的损失
        i += 1

        if stopType == STOP_ITER:
            value = i
        elif stopType == STOP_COST:
            value = costs
        elif stopType == STOP_GRAD:
            value = grad
        if stopcriterion(stopType, value, thresh): break

    return theta, i - 1, costs, grad, time.time() - init_time


def runExpe(data, theta, batchSize, stopType, thresh, alpha):
    #import pdb; pdb.set_trace();
    theta, iter, costs, grad, dur = descent(data, theta, batchSize, stopType, thresh, alpha)
    name = "Original" if (data[:,1]>2).sum() > 1 else "Scaled"
    name += " data - learning rate: {} - ".format(alpha)
    if batchSize==n: strDescType = "Gradient"
    elif batchSize==1:  strDescType = "Stochastic"
    else: strDescType = "Mini-batch ({})".format(batchSize)
    name += strDescType + " descent - Stop: "
    if stopType == STOP_ITER: strStop = "{} iterations".format(thresh)
    elif stopType == STOP_COST: strStop = "costs change < {}".format(thresh)
    else: strStop = "gradient norm < {}".format(thresh)
    name += strStop
    print ("***{}\nTheta: {} - Iter: {} - Last cost: {:03.2f} - Duration: {:03.2f}s".format(
        name, theta, iter, costs[-1], dur))
    fig, ax = plt.subplots(figsize=(12,4))
    ax.plot(np.arange(len(costs)), costs, 'r')
    ax.set_xlabel('Iterations')
    ax.set_ylabel('Cost')
    ax.set_title(name.upper() + ' - Error vs. Iteration')
    return theta

if __name__ == '__main__':
    n=100
    runExpe(orig_data, theta, batchSize=n, stopType=STOP_GRAD, thresh=0.000001, alpha=0.001)
    plt.show()