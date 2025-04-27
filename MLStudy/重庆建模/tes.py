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

import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import MaxAbsScaler
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics.classification import precision_score,recall_score,accuracy_score,f1_score
from tpot import TPOTClassifier

# NOTE: Make sure that the class is labeled 'target' in the data file
tpot_data = pd.read_csv('/home/bdp/h2o/sichuan/widetable.csv')
df_obj = tpot_data.select_dtypes(['float64','int64'])
tpot_data[tpot_data[df_obj.columns]<0]=np.nan
tpot_data[modeCols]=tpot_data[modeCols].fillna(tpot_data[modeCols].mode().iloc[0])
tpot_data[meanCols]=tpot_data[meanCols].fillna(tpot_data[meanCols].mean().iloc[0])

lb=LabelEncoder()
for col in ["brand","product"]:
    tpot_data[col]=lb.fit_transform(tpot_data[col])

submitData=tpot_data[tpot_data["label"].isna()]
modelData=tpot_data[~tpot_data["label"].isna()]
modelData["label_index"]=lb.fit_transform(modelData["label"])
train_data=modelData[modelCols]
target_data=modelData['label_index']


training_features, testing_features, training_target, testing_target = \
            train_test_split(train_data.values, target_data.values, random_state=None)

# Average CV score on the training set was:0.8297202641253423
exported_pipeline = make_pipeline(
    MaxAbsScaler(),
    DecisionTreeClassifier(criterion="entropy", max_depth=7, min_samples_leaf=9, min_samples_split=16)
)

exported_pipeline.fit(training_features, training_target)
results = exported_pipeline.predict(testing_features)

f1=f1_score(testing_target,results,average="macro")
print(f1)

submit=exported_pipeline.predict(submitData[modelCols])
submitData["pred_label"]=lb.inverse_transform(submit)

result=submitData[["user","pred_label"]]
result.to_csv("/home/bdp/h2o/sichuan/dt.csv",header=False,index=False)