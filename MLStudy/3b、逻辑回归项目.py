import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler
#用于归一化数据

from sklearn.linear_model import LogisticRegression
from sklearn.metrics import confusion_matrix,recall_score,classification_report
#导入logist回归模型，KFold指做几倍交叉验证（切分数据集），cross_val_score指交叉验证评估的结果
#confusion_matrix指混淆矩阵.

from sklearn.model_selection import KFold,cross_val_score,train_test_split

data=pd.read_csv(r"E:\python\Pro1\study_project\creditcard.csv")
#31列宽表
# print(data.head())

class_count=pd.value_counts(data['Class'],sort=True).sort_index()
#value_counts:计算data['Class']这一列中不同的值分别有多少个，并排序。
print(class_count)
class_count.plot(kind='bar')  #直接使用pandas内置的画图函数画图
plt.title('class_count')
plt.xlabel('class')
plt.ylabel('counts')
plt.show()

#对于严重分布不均衡数据：
#下采样：在有过多数据的分类中选出与较少数据的分类的数量差不多的样本数，然后组合在一起。（同样少）
#过采样：对较少数据分类，生成样本（重复），和过多数据的分类数据差不多一样（同样多）
data['normaccount']=StandardScaler().fit_transform(data['Amount'].reshape(-1,1))
data=data.drop(['Time','Amount'],axis=1)  #并非原处修改
X=data.loc[:,data.columns != "Class"]
#loc为按照标签值进行筛选，iloc为按照位置进行筛选。
y=data.loc[:,data.columns == "Class"]

num_negative=len(data[data['Class']==1])  #异常的样本数量
negative_index=np.array(data[data['Class']==1].index)  #异常样本的索引值

normal_index=data[data['Class']==0].index #正常样本的索引
normal_sample_index=np.random.choice(normal_index,num_negative,replace=False)
normal_sample_index=np.array(normal_sample_index)
#此处为下采样方式。从正常值中选取异常值的个数

under_sample_index=np.concatenate([negative_index,normal_sample_index])
#合并异常值索引及正常样本的索引

under_sample_data=data.loc[under_sample_index,:]
#行按照索引选取，列为全部
X_sample=under_sample_data.loc[:,under_sample_data.columns != 'Class']
y_sample=under_sample_data.loc[:,under_sample_data.columns == 'Class']


#交叉验证
#在训练集数据中平均划分为几类，每次训练选出一类作为验证其他类别创建的模型的效果，
# 即对训练集再次划分数据。划分出一个验证集来

X_train,X_test,y_train,y_test=train_test_split(X,y,test_size=0.3,random_state=0)
#对数据洗牌后，切分训练集和测试集，test_size为测试集的切分比例，
# random_state是指每一次切分都划分成一样的样本。（而不至于每次选样不同，构建出的模型也不同）

X_sample_train,X_sample_test,y_sample_train,y_sample_test=train_test_split(X_sample,y_sample,test_size=0.3,random_state=0)
#对样本集做相同的操作

#模型评估：
#精度：准确率。预测成功数/预测总数，即预测成功+预测失败
#recall:查全率。预测成功数/实际正确数，即预测成功+实际成功但预测失败了

def print_kfold_score(x_train_data,y_train_data):
    fold=KFold(len(y_train_data),5,shuffle=False)
    #切分训练集为5份

    c_param_range=[0.01,0.1,1,10,100]
    #正则化惩罚.防止选取了波动太大的模型，导致过拟合（高方差）
    #正则化惩罚分为l1和l2惩罚，l1惩罚类似加上L项，l2惩罚为类似加上1/2*L平方

    result_table=pd.DataFrame(index=range(len(c_param_range),2),columns=['C_parameter',
                                                                         'mean_recall_score'])
    result_table['C_parameter']=c_param_range

    j=0
    for c_param in c_param_range:
        print('---------------------')
        print('c_param:',c_param)
        print('---------------------')
        recall_accs=[]
        for iteration, indices in enumerate(fold, start=1):
            # Call the logistic regression model with a certain C parameter
            lr = LogisticRegression(C=c_param, penalty='l1')
            #设定为l1惩罚。lr为实例化模型。C参数为惩罚力度

            # Use the training data to fit the model. In this case, we use the portion of the fold to train the model
            # with indices[0]. We then predict on the portion assigned as the 'test cross validation' with indices[1]
            lr.fit(x_train_data.iloc[indices[0], :], y_train_data.iloc[indices[0], :].values.ravel())
            #fit进行训练。x_train_data为训练数据集，y_train_data为目标参数

            # Predict values using the test indices in the training data
            y_pred_undersample = lr.predict(x_train_data.iloc[indices[1], :].values)
            #利用训练好的参数预测验证集中的目标参数

            # Calculate the recall score and append it to a list for recall scores representing the current c_parameter
            recall_acc = recall_score(y_train_data.iloc[indices[1], :].values, y_pred_undersample)
            #根据预测结果和实际数值计算召回率
            recall_accs.append(recall_acc)  #收集每次计算的召回率，作为展示
            print('Iteration ', iteration, ': recall score = ', recall_acc)

            # The mean value of those recall scores is the metric we want to save and get hold of.
        result_table.loc[j, 'Mean recall score'] = np.mean(recall_accs)
        j += 1
        print('')
        print('Mean recall score ', np.mean(recall_accs))
        print('')

    best_c = result_table.loc[result_table['Mean recall score'].idxmax()]['C_parameter']

    # Finally, we can check which C parameter is the best amongst the chosen.
    print('*********************************************************************************')
    print('Best model to choose from cross validation is with C parameter = ', best_c)
    print('*********************************************************************************')

    return best_c


model=print_kfold_score(X_sample,y_sample)
print(model)


#过采样：
#smote采样：
#1、识别小样本群体；
#2、计算小样本中每个样本距离其他小样本的欧式距离，然后对距离排序；
#3、根据要夸张的样本数，选取前几个距离，然后使用每个距离根据x_new=x_old+rand(0,1)*Euclidean_Distance计算产生新样本
#4、rand(0,1)为0到1之间的随机数

from imblearn.over_sampling import SMOTE
#imblearn为非平衡数据建模模块
oversample=SMOTE(random_state=0)
#random_state=0为随机数种子，每次选样为一样的
os_features,os_labels=oversample.fit_sample(X_train,y_train)
#产生过采样后的特征集和目标集



