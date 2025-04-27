#熵：表示随机变量的不确定性（混乱程度）。数值越大混乱程度越高
#熵主要通过：-p1logp1-p2logp2……,每一类发生概率乘上概率的log值的负数累加衡量

#信息增益：表示特征x对目标y分类后，每一类y的熵值的减少程度。
#某一特征对目标变量的信息增益计算方法：
# 1、首先用该特征中每一类别对目标分类，计算每一类别中目标的熵值；
# 2、计算特征中每一类别的概率，使用每类的概率值乘上该类的目标熵值，累计计算得出总熵值
# 3、用目标总熵值减去该特征分类的总熵值，得出的差值就是信息增益程度，差值越高说明对熵的减小幅度越大

#上面为ID3算法内容，问题：特征中可能存在类似身份ID一样的字段，会导致该特征熵值为0，但毫无预测效果
#C4.5算法：解决了ID3算法的问题，计算特征自身的熵值，然后用特征的信息增益除上自身熵值，得出信息增益率进行比较
#CART算法：没用信息增益解决，而使用GINI系数代替熵值
#GINI系数：1-∑p平方，概率越靠近1（越确定），GINI系数越小。类似熵值

#剪枝：可能分的过细导致过拟合，需要踢除某些分支中的特征。
#预剪枝：一边构建决策树一边剪枝（更常用）
#后剪枝：构建完毕后在剪枝

#剪枝策略：
#预剪枝：
#1、限定深度（比较常用）
#2、限制叶子节点。（叶子节点就是最终没有在往下分裂的节点）
#3、限制叶子节点样本数。即每个叶子节点包含的样本数不能低于某个值
#4、限制信息增益。即某一次分裂所产生的增益不能低于某一个值

#后剪枝：
#1、C(t)=∑（熵值*本叶子节点样本数）
#衡量函数：C=C(t)+α|叶子节点总数|  剪枝策略：每一个节点分裂数，让该函数越小越好。α为系数，越大则叶子节点数要越少

from sklearn.datasets.california_housing import fetch_california_housing
#内置房价数据集（2w多行，9列）
housing=fetch_california_housing()
print(housing.data.shape)
from sklearn import tree
dtr=tree.DecisionTreeRegressor(max_depth=2)  #构造决策树，树最大深度为2
#主要参数：
# 1、criterion：选择熵值还是GINI系数；
# 2、splitter：选择特征，best或者random。数据量比较大时，可以选择随机选择特征，best指全部遍历，默认值；
# 3、max_features：指定最多特征。默认全部（None）；
# 4、max_depth；
# 5、min_samples_split：常用参数。控制全部节点样本数，叶子节点样本数不得小于该数字
# 6、min_samples_leaf：控制叶子节点样本数，小于会被剪枝
# 7、其他不常用

dtr.fit(housing.data[:,[6,7]],housing.target)
dot_date=tree.export_graphviz(dtr,  #构造的决策树
                              out_file=None,
                              feature_names=housing.feature_names[6:8], #输出的特征名字
                              filled=True,
                              impurity=False,
                              rounded=True)
#export_graphviz输出可视化数据
import pydotplus
from graphviz import Digraph
# dot_date=Digraph(dot_date)
graph=pydotplus.graph_from_dot_data(dot_date)
graph.get_nodes()[7].set_fillcolor('#FFF2DD')
# from IPython.display import Image
# Image(graph.create(format='png'))
#绘制决策树的图
graph.write("tree.png",format='png')

from sklearn.model_selection import train_test_split
data_train,data_test,target_train,target_test=train_test_split(housing.data,housing.target,test_size=0.1,random_state=42)
#对数据集切分成训练集和测试集。random_state为随机种子，相同种子生成的随机数相同（每一次执行都相同）
dtr1=tree.DecisionTreeRegressor(random_state=42)
dtr1.fit(data_train,target_train)
dtr1.score(data_test,target_test)  #


from sklearn.ensemble import RandomForestRegressor  #随机森林
#ensemble为集成算法
rfr = RandomForestRegressor(random_state=42)
rfr.fit(data_train, target_train)
rfr.score(data_test, target_test)


from sklearn.grid_search import GridSearchCV

tree_param_grid={'min_samples_split': list((3,6,9)),'n_estimators':list((10,50,100))}
grid=GridSearchCV(RandomForestRegressor(),param_grid=tree_param_grid,cv=5)
#将备选参数绑定到模型中。cv是指交叉验证次数（cross valid）。即将训练集均分成5份，分别选择其中1份作为验证集对另外4份建立的模型进行验证。然后计算平均值
grid.fit(data_train,target_train)
print(grid.grid_scores_, grid.best_params_, grid.best_score_)
#确定出最好参数和相应的值

