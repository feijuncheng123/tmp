import pandas as pd
import numpy as np
from sklearn import tree
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier,GradientBoostingClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.preprocessing import MaxAbsScaler
from sklearn.model_selection import train_test_split,GridSearchCV,cross_val_score
from sklearn.metrics import f1_score
from sklearn.externals import joblib
import graphviz

sourcedata=pd.read_csv("")
sourcedata.fillna("0",inplace=True)
modeldata = MaxAbsScaler().fit(sourcedata).transform(sourcedata)

y=np.ravel(modeldata[""])
X=modeldata[""]

train_data,test_data,train_target,test_target=train_test_split(X,y,
                                                               test_size=0.2,
                                                               random_state=42)


dtc=DecisionTreeClassifier(max_depth=4)
tree_param_grid={'min_samples_leaf':[8000,10000,12000]}
treemodel=GridSearchCV(dtc,
                       param_grid=tree_param_grid,
                       cv=10,
                       scoring='f1',  #使用分类的总体准确度评估模型
                       n_jobs=3)

treemodel.fit(train_data,train_target)
print("预测分数：",treemodel.cv_results_)
print("最佳参数：",treemodel.best_params_)
print("最高分数：",treemodel.best_score_)

best_treemodel=treemodel.best_estimator_

dot_data = tree.export_graphviz(best_treemodel,
                                out_file=None,
                                feature_names=train_field,
                                class_names=np.unique(train_target),
                                filled=True,
                                rounded=True,
                                special_characters=True,
                                precision=0
                                )

graph = graphviz.Source(dot_data,encoding="utf8")
graph.save("tree.dot")


rfc=RandomForestClassifier(random_state=42,
                           min_samples_leaf=treemodel.best_params_['min_samples_leaf'], #使用树的最佳分裂叶子
                           n_jobs=10,
                           n_estimators=50,
                           )

print(cross_val_score(rfc,X,y,cv=3))

gbdt=GradientBoostingClassifier()
gbdt_param_grid={'min_samples_leaf':[8000,10000,12000]}
gbdtmodel=GridSearchCV(gbdt,
                       param_grid=tree_param_grid,
                       cv=10,
                       scoring='f1',  #使用分类的总体准确度评估模型
                       n_jobs=4)

gbdtmodel.fit(train_data,train_target)
print("预测分数：",gbdtmodel.cv_results_)
print("最佳参数：",gbdtmodel.best_params_)
print("最高分数：",gbdtmodel.best_score_)


bayes=GaussianNB()
print(cross_val_score(bayes,X,y,cv=3))


