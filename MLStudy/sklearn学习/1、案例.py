from sklearn import datasets

from sklearn import svm


iris = datasets.load_iris()
digits = datasets.load_digits()
print(digits.data[0])
#data为每张图片矩阵拉直后组成的二维矩阵
print(digits.target)
#为目标向量
print(digits.images[0])
#为图片矩阵，每张图片拉直后构成data的一行

clf = svm.SVC(gamma=0.001, C=100.)

clf.fit(digits.data[:-1], digits.target[:-1])
pre=clf.predict(digits.data[-1:])  #使用模型预测
print(pre)

clf_iris = svm.SVC()
X, y = iris.data, iris.target
clf_iris.fit(X, y)

import pickle
s = pickle.dumps(clf_iris)
#可以使用pickle固化模型
clf2 = pickle.loads(s)
clf2.predict(X[0:1])

from sklearn.externals import joblib
#也可以使用joblib进行模型固化
joblib.dump(clf_iris, 'filename.pkl')
clf = joblib.load('filename.pkl')

