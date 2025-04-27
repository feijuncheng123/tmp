modelCols=["credit_level","membership_level","gender","inet_pd_inst_cnt","star_level",
  "dt_m_1000","dt_m_1003","dt_m_1004","dt_m_1005","dt_m_1006","dt_m_1009","dt_m_1011","dt_m_1012","dt_m_1015","dt_m_1017",
  "dt_m_1027","dt_m_1028","dt_m_1032","dt_m_1034","dt_m_1035","dt_m_1041","dt_m_1043","dt_m_1051","dt_m_1052","dt_m_1067",
  "dt_m_1068","dt_m_1073","dt_m_1074","dt_m_1075","dt_m_1085","dt_m_1086","dt_m_1087","dt_m_1096","dt_m_1099","dt_m_1102",
  "dt_m_1105","dt_m_1108","dt_m_1111","dt_m_1594","dt_m_1601","dt_m_1617","dt_m_1618","dt_m_1620","dt_m_1630","dt_m_1633",
  "last_year_capture_user_flag","app1_visits","app2_visits","app3_visits","app4_visits","app5_visits","app6_visits",
  "app7_visits","app8_visits","access_net_dur","tmlRegister_net_dur","prdOpen_dur","tmlRegister_dur","prdOpen_net_dur",
  "brand","product","cust_point_level","age_level","market_price_level","app_cnt"]


from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import GradientBoostingClassifier
import pandas as pd
import numpy as np
from sklearn.model_selection import cross_val_score,train_test_split,KFold,GridSearchCV
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics.classification import precision_score,recall_score,accuracy_score,f1_score

from sklearn.impute import SimpleImputer
from sklearn.naive_bayes import MultinomialNB
data=pd.read_csv("/home/bdp/h2o/sichuan/wideTable.csv")
data[modelCols]=data[modelCols].fillna(0)
lb=LabelEncoder()
for col in ["brand","product"]:
    data[col]=lb.fit_transform(data[col])


alltrain=data[~data["label"].isnull()]
submit=data[data["label"].isnull()]

m=alltrain[alltrain["label"]=='7C26FADD409BD4B9']
n=alltrain[alltrain["label"]!='7C26FADD409BD4B9']
sample=m.sample(50000,random_state=963741)
train=n.append(sample)

train["label_index"]=lb.fit_transform(train["label"])
X=train[modelCols]
y=train["label_index"]

train_data,test_data,train_target,test_target=train_test_split(X,y,test_size=0.2,random_state=65524)


dtc=DecisionTreeClassifier()
tree_param_grid={'max_depth':[3,5,2]}
treemodel=GridSearchCV(dtc,
                       param_grid=tree_param_grid,
                       cv=5,
                       scoring='f1_macro',
                       n_jobs=6)

treemodel.fit(train_data,train_target)


#训练全部
treemodel.fit(X,y)
print("最佳参数：",treemodel.best_params_)
print("最高分数：",treemodel.best_score_)

tree_predict_result=treemodel.predict(test_data)
macro_f1=f1_score(test_target,tree_predict_result,average='macro')
print("单棵决策树预测准确率：",macro_f1)
tree_confuse_table=pd.crosstab(test_target,tree_predict_result)

#预测提交
pred=treemodel.predict(submit[modelCols])
submit["pred_label"]=lb.inverse_transform(pred)
submit.groupby("pred_label")["user"].count()


rfc=RandomForestClassifier(random_state=42,
                           max_depth=11,
                           n_estimators=300,
                           max_features="sqrt",
                           class_weight="balanced",
                           n_jobs=10
                           )

rfc_param_grid={'n_estimators':[80,200,100]}
rfc_GridSearch=GridSearchCV(rfc,param_grid=rfc_param_grid,scoring="f1_macro",cv=5,n_jobs=1)
rfc_model=rfc_GridSearch.fit(train_data,train_target)
print("随机森林最佳参数：",rfc_model.best_params_)
print("随机森林最高分数：",rfc_model.best_score_)

rfc.fit()

rfc_predict_result=rfc_model.best_estimator_.predict(test_data)
rfc_macro_f1=f1_score(test_target,rfc_predict_result,average='macro')
print("随机森林测准确率：",rfc_macro_f1)
rfc_confuse_table=pd.crosstab(test_target,rfc_predict_result)

#预测提交
pred=rfc_model.predict(submit[modelCols])
submit["pred_label"]=lb.inverse_transform(pred)
submit.groupby("pred_label")["user"].count()
result=submit[['user','pred_label']]
result.to_csv("/home/bdp/h2o/sichuan/pythonresult/result_v8.csv",index=False,header=False)
submit.reset_index()


gbdt=GradientBoostingClassifier(random_state=42,
                           min_samples_leaf=treemodel.best_params_['min_samples_leaf'],
                           max_depth=treemodel.best_params_['max_depth'])

gbdt_param_grid={'n_estimators':[20,50,30]}
gbdt_GridSearch=GridSearchCV(gbdt,param_grid=gbdt_param_grid,scoring="f1_macro",cv=5,n_jobs=12,verbose=10)
gbdt_model=gbdt_GridSearch.fit(X,y)
print("随机森林最佳参数：",gbdt_model.best_params_)
print("随机森林最高分数：",gbdt_model.best_score_)
