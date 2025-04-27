from sklearn import linear_model
from sklearn import datasets
import numpy as np

reg = linear_model.LinearRegression()
reg.fit([[0, 0], [1, 1], [2, 2]], [0, 1, 2])
x=reg.coef_
#coef_为计算后的线性回归系数值。
#做回归分析，需要注意多重共线性问题。（可加入正则化惩罚防止共线性问题）
# print(x)

#岭回归通过对系数的大小施加惩罚来解决 普通最小二乘的一些问题。
# ridge coefficients ( 岭系数 ) 最小化一个带罚项的残差平方和，
ridge = linear_model.Ridge(alpha = 0.5)
ridge.fit([[0, 0], [0, 0], [1, 1]], [0, .1, 1])
bias=ridge.intercept_
#intercept_为截距


diabetes = datasets.load_diabetes()
#diabetes为包含了10个生理学特征（身高、体重、血压等）的442个样本的数据集
diabetes_X_train = diabetes.data[:-20]
diabetes_X_test = diabetes.data[-20:]
diabetes_y_train = diabetes.target[:-20]
diabetes_y_test = diabetes.target[-20:]
regr = linear_model.LinearRegression()
regr.fit(diabetes_X_train, diabetes_y_train)
np.mean((regr.predict(diabetes_X_test)-diabetes_y_test)**2)
#预测的目标值与实际目标值差异的平方（平方差）
regr.score(diabetes_X_test, diabetes_y_test)
#为可释方差得分：1-var{（y-ŷ)}/var{y}



X = np.c_[ .5, 1].T
y = [.5, 1]
test = np.c_[0, 2].T


