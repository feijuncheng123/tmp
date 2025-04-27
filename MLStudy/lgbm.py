import pandas as pd
import numpy as np
import lightgbm as lgb
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics.classification import f1_score
from xgboost import plot_importance
from matplotlib import pyplot


source=pd.read_csv(r"E:\python\Pro1\untitled\MLStudy\data\aisai\data.csv")
dropCols=["SUB_NO","CALL_MONTH","CALL_STD_DURATION","CALLING_STD_DURATION","CALLED_STD_DURATION","ADJACENT_STD_PERIOD",
          "CUST_ID","RECHARGE_NUM","RECHARGE_MONTH"]
modeldata=source.drop(dropCols,axis=1)

modeldata["dur_per_times"]=modeldata["CALL_DURATION"]/modeldata["CALL_TIMES"].replace(0, np.inf)  #次均通话时长
modeldata["dur_per_num"]=modeldata["CALL_DURATION"]/modeldata["CALL_NUMS"].replace(0, np.inf) #人均通话时长
modeldata["times_per_num"]=modeldata["CALL_TIMES"]/modeldata["CALL_NUMS"].replace(0, np.inf) #人均通话次数
modeldata["calling_dur_per_times"]=modeldata["CALLING_DURATION"]/modeldata["CALLING_TIMES"].replace(0, np.inf) #次均主叫通话时长
modeldata["calling_dur_per_num"]=modeldata["CALLING_DURATION"]/modeldata["CALLING_NUMS"].replace(0, np.inf) #人均主叫通话时长
modeldata["calling_times_per_num"]=modeldata["CALLING_TIMES"]/modeldata["CALLING_NUMS"].replace(0, np.inf) #人均主叫通话次数
modeldata["called_dur_per_times"]=modeldata["CALLED_DURATION"]/modeldata["CALLED_TIMES"].replace(0, np.inf) #次均被叫通话时长
modeldata["called_dur_per_num"]=modeldata["CALLED_DURATION"]/modeldata["CALLED_NUMS"].replace(0, np.inf) #人均被叫通话时长
modeldata["called_times_per_num"]=modeldata["CALLED_TIMES"]/modeldata["CALLED_NUMS"].replace(0, np.inf) #人均被叫通话次数

modeldata["percent_of_calling_dur"]=modeldata["CALLING_DURATION"]/modeldata["CALL_DURATION"].replace(0, np.inf)  #主叫时长占比
modeldata["percent_of_calling_times"]=modeldata["CALLING_TIMES"]/modeldata["CALL_TIMES"].replace(0, np.inf)  #主叫次数占比
# modeldata["num_of_callback"]=modeldata["CALLED_NUMS"] + modeldata["CALLING_NUMS"] - modeldata["CALL_NUMS"]  #回拨人数

modeldata["percent_of_workday_times"]=modeldata["WORKING_DAY_TIMES"]/modeldata["CALL_TIMES"].replace(0, np.inf)  #工作日通话次数占比
# modeldata["percent_of weekend_times"]=modeldata["WEEKEND_TIMES"]/modeldata["CALL_TIMES"].replace(0, np.inf)  #周末通话次数占比

featureCols=["CALL_DURATION","CALL_MAX_DURATION","CALL_MIN_DURATION","CALL_TIMES","CALL_NUMS","CALLING_DURATION","CALLING_MAX_DURATION",
           "CALLING_MIN_DURATION","CALLING_TIMES","CALLING_NUMS","CALLED_DURATION","CALLED_MAX_DURATION","CALLED_MIN_DURATION",
           "CALLED_TIMES","CALLED_NUMS","SHORT_TIMES","FIXED_TIMES","ADJACENT_MAX_PERIOD","ADJACENT_MIN_PERIOD","CALL_IN_00_07_TIMES",
           "CALL_IN_07_13_TIMES","CALL_IN_13_19_TIMES","CALL_IN_19_24_TIMES","WORKING_DAY_TIMES","WEEKEND_TIMES","INTERNATIONAL_TIMES",
           "CALLING_STATION_NUMS","FLOW_USAGE","SEND_MSG_NUMS","RECEIVE_MSG_NUMS","CALL_CHARGE","FLOW_CHARGE","MESSAGE_CHARGE",
           "RECHARGE_TIMES","MAX_RECHARGE_AMOUNT","MIN_RECHARGE_AMOUNT","TOTAL_RECHARGE_AMOUNT","dur_per_times","dur_per_num","times_per_num",
            "calling_dur_per_times","calling_dur_per_num","calling_times_per_num","called_dur_per_times","called_dur_per_num","called_times_per_num",
            "percent_of_calling_dur","percent_of_calling_times","percent_of_workday_times"]

X=modeldata[featureCols]
y=modeldata["STATUS"]
train_x_all, test_x_all, train_y_all, test_y=train_test_split(X,y,random_state=88888)
train_data=lgb.Dataset(train_x_all,train_y_all,free_raw_data=False)

params = {
    'boosting_type': 'goss',
    'objective': 'binary',
    'metric': 'binary_logloss',
    'n_estimators':1000,
    'max_depth': 8,
    'num_leaves':30,
    'learning_rate': 0.04,
    'feature_fraction': 0.1,
    'feature_fraction_bynode':0.7,
    'top_rate':0.3,
    'other_rate':0.1,
    'lambda_l1':0,
    'lambda_l2':1,
    'num_threads':10
}

gbm=lgb.train(params,train_data,valid_sets={train_data},early_stopping_rounds=10)

y_pred=gbm.predict(test_x_all)
test=np.where(y_pred>0.5,1,0)
from collections import  Counter
Counter(test)


for x,y in sorted(zip(featureCols,gbm.feature_importance("gain")),key=lambda x:-x[1]):
    print(x,y)
gbm.save_model('model.txt')