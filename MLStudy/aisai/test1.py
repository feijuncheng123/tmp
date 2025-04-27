import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


a=pd.read_csv("E:\zte\data\athio\1.csv")
a.drop("id",axis=1,inplace=True)
b=pd.read_csv("E:\zte\data\athio\2.csv")
data=a.append(b,ignore_index=True)

plt.style.use('bmh')



import traceback


from sklearn.ensemble.gradient_boosting import GradientBoostingClassifier
from sklearn.model_selection import GridSearchCV,train_test_split
from sklearn.externals import joblib

gbdt=GradientBoostingClassifier()
param_grid={'learning_rate':[0.01,0,1,0.05],"n_estimators":[500,1000,300],"subsample":[0.8,0.6,0.7],"max_depth":[6,8,5],
            "max_features":["auto",0.8,0.5,None]}
GridSearchModel=GridSearchCV(gbdt,
                       param_grid=param_grid,
                       cv=4,
                       scoring='f1',
                       n_jobs=10)
model=GridSearchModel.fit(train_data,train_target)
print("最佳参数：",model.best_params_)
print("最高分数：",model.best_score_)

best_treemodel=model.best_estimator_
tree_predict_result=best_treemodel.predict(test_x_all)
tree_confuse_table=pd.crosstab(total_test_target,tree_predict_result)

joblib.dump(best_treemodel,'gbdt_model.pkl')


import os
os.listdir()
os.mkdir("test")