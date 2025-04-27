modelCols=["credit_level","membership_level","gender","cust_point","inet_pd_inst_cnt","star_level","market_price",
  "dt_m_1000","dt_m_1003","dt_m_1004","dt_m_1005","dt_m_1006","dt_m_1009","dt_m_1011","dt_m_1012","dt_m_1015","dt_m_1017",
  "dt_m_1027","dt_m_1028","dt_m_1032","dt_m_1034","dt_m_1035","dt_m_1041","dt_m_1043","dt_m_1051","dt_m_1052","dt_m_1067",
  "dt_m_1068","dt_m_1073","dt_m_1074","dt_m_1075","dt_m_1085","dt_m_1086","dt_m_1087","dt_m_1096","dt_m_1099","dt_m_1102",
  "dt_m_1105","dt_m_1108","dt_m_1111","dt_m_1594","dt_m_1601","dt_m_1617","dt_m_1618","dt_m_1620","dt_m_1630","dt_m_1633",
  "last_year_capture_user_flag","app1_visits","app2_visits","app3_visits","app4_visits","app5_visits","app6_visits",
  "app7_visits","app8_visits","age","access_net_dur","tmlRegister_net_dur","prd_open_dur","tmlRegister_prdopen_dur",
  "brand","product"]

modeCols=["credit_level","membership_level","gender","star_level","last_year_capture_user_flag","brand","product"]
meanCols=[i for i in modelCols if i not in modeCols]

from tpot import TPOTClassifier
import pandas as pd
import numpy as np
pd.set_option('display.max_rows', 100)
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split,GridSearchCV
from sklearn.metrics.classification import precision_score,recall_score,accuracy_score,f1_score

from xgboost import XGBClassifier

data=pd.read_csv("/home/bdp/h2o/sichuan/widetable.csv")
df_obj = data.select_dtypes(['float64','int64'])
data[data[df_obj.columns]<0]=np.nan
# data[data['label'].isna()]['user'].count()
lb=LabelEncoder()
for col in ["brand","product"]:
    data[col]=lb.fit_transform(data[col])

data[modeCols]=data[modeCols].fillna(data[modeCols].mode().iloc[0])
data[meanCols]=data[meanCols].fillna(data[meanCols].mean().iloc[0])

modelData=data[~data["label"].isna()]
submitData=data[data["label"].isna()]
modelData["label_index"]=lb.fit_transform(modelData["label"])
train_data=modelData[modelCols]
target_data=modelData['label_index']

# X_train, X_test, y_train, y_test = train_test_split(train_data, target_data,
#                                                     train_size=0.75, test_size=0.25)
tpot = TPOTClassifier(generations=5, population_size=20, verbosity=2,scoring="f1_macro",n_jobs=20)
tpot.fit(train_data, target_data)
tpot.export('tpot_best_model.py')
# p=tpot.predict(X_test)
# f1=f1_score(y_test,p,average="macro")
# print("f1=%s"%(f1*f1))

submit=tpot.predict(submitData[modelCols])
submitData["pred_label"]=lb.inverse_transform(submit)

result=submitData[["user","pred_label"]]
result.to_csv("/home/bdp/h2o/sichuan/tpot.csv",header=False,index=False)



