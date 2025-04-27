modelCols=["credit_level","gender","inet_pd_inst_cnt","star_level",
    "dt_m_1000","dt_m_1003","dt_m_1004","dt_m_1005","dt_m_1006","dt_m_1009","dt_m_1011","dt_m_1012","dt_m_1015","dt_m_1017",
    "dt_m_1027","dt_m_1028","dt_m_1041","dt_m_1044","dt_m_1051","dt_m_1053","dt_m_1067",
    "dt_m_1069","dt_m_1073","dt_m_1074","dt_m_1075","dt_m_1085","dt_m_1086","dt_m_1087","dt_m_1096","dt_m_1099","dt_m_1102",
    "dt_m_1105","dt_m_1108","dt_m_1111","dt_m_1594","dt_m_1601","dt_m_1617","dt_m_1618","dt_m_1620","dt_m_1630","dt_m_1633",
    "last_year_capture_user_flag","app1_visits","app2_visits","app3_visits","app4_visits","app5_visits","app6_visits",
    "app7_visits","app8_visits","access_net_dur","tmlRegister_net_dur","prdOpen_dur","tmlRegister_dur","prdOpen_net_dur",
    "brand","product","cust_point_level","age","market_price_level","app_cnt","dt_m_1012_type",
    "dt_m_1027_type","dt_m_1032_type","dt_m_1034_type","dt_m_1075_type","dt_m_1086_type","dt_m_1087_type",
    "dt_m_1096_type","dt_m_1102_type","dt_m_1108_type","dt_m_1594_type","dt_m_1617_type","dt_m_1620_type",
    "dt_m_1630_type","dt_m_1633_type","app1_visits_type","app2_visits_type","app3_visits_type",
    "app4_visits_type","app5_visits_type","app6_visits_type","app7_visits_type","app8_visits_type",
    "dt_m_1087_pref","in_10s_per","in_10_30s_per","in_30_60s_per","out_60s_per","dt_m_1035_pref"]


import xgboost as xgb
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics.classification import f1_score
from xgboost import plot_importance
from matplotlib import pyplot

data=pd.read_csv("E:\zte\data\data.csv")
lb=LabelEncoder()
for col in ["brand","product"]:
    data[col]=lb.fit_transform(data[col])

submitData=data[data["label"].isnull()]
submitData["user"].count()

trainData=data[~data["label"].isnull()]
trainData["user"].count()
# trainData["sample_weight"]=trainData.groupby("label")["user"].transform(lambda x:x.count()/322757)

trainData["label_index"]=lb.fit_transform(trainData["label"])
# X=trainData[modelCols]
y=trainData["label_index"]
train_x_all, test_x_all, train_y, test_y=train_test_split(trainData,y,random_state=0)

m=train_x_all[train_x_all["label"]=='7C26FADD409BD4B9']
n=train_x_all[train_x_all["label"]!='7C26FADD409BD4B9']
sample=m.sample(60000,random_state=963741)
train=n.append(sample)

train_x=train_x_all[modelCols]
test_x=test_x_all[modelCols]
# dtrain=xgb.DMatrix(train_x,label=train_y,missing=np.NaN)
# dtest=xgb.DMatrix(test_x,missing=np.NaN)




params={'booster':'gbtree',
    'objective': 'multi:softmax',
    'num_class': 5,
    'eval_metric': 'mlogloss',
    'max_depth':10,
    'lambda':10,
    'subsample':0.75,
    'colsample_bytree':0.75,
    'min_child_weight':2,
    'eta': 0.025,
    'seed':0,
    'nthread':8,
    'verbosity':3}


xlf = xgb.XGBClassifier(booster="gbtree",
                        objective="multi:softmax",
                        num_class=5,
                        eval_metric="mlogloss",
                        n_estimators=150,
                        max_depth=10,
                        reg_alpha=0,
                        reg_lambda=1,
                        subsample=0.7,
                        colsample_bytree=0.3,
                        learning_rate=0.1,
                        scale_pos_weight=10,
                        n_jobs=10
                        )

xgbModel=xlf.fit(train_x,train_y,sample_weight=train_x_all["sample_weight"])

kfold = StratifiedKFold(n_splits=10, random_state=7)
results = cross_val_score(xlf, train_x, train_y, cv=kfold,scoring="f1_macro")
print(results)
plot_importance(xlf)
pyplot.show()
for x,y in zip(modelCols,xlf.feature_importances_):
    print(x,y,sep="\n")
y_pred = xgbModel.predict(test_x)
macro_f1=f1_score(test_y,y_pred,average='macro')
print(macro_f1)

submit_pred=xgbModel.predict(submitData[modelCols])
submitData["pred_label"]=lb.inverse_transform(submit_pred)

submitData.groupby("pred_lebal")["user"].count()
submitData.to_csv("E:\zte\data\chongqin.csv",index=False,header=False)
# num_round = 10
# evallist = [(dtrain, 'train')]
# bst = xgb.train(params, dtrain, num_round, evallist)
# ypred=bst.predict(dtest)
# y_pred = (ypred >= 0.5)*1
#
#
# from sklearn import metrics
# print ('AUC: %.4f' % metrics.roc_auc_score(test_y,ypred))
# print ('ACC: %.4f' % metrics.accuracy_score(test_y,y_pred))
# print ('Recall: %.4f' % metrics.recall_score(test_y,y_pred))
# print ('F1-score: %.4f' %metrics.f1_score(test_y,y_pred))
# print ('Precesion: %.4f' %metrics.precision_score(test_y,y_pred))
# metrics.confusion_matrix(test_y,y_pred)