import lightgbm as lgb
import pandas as pd
import numpy as np
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split

data=pd.read_csv("E:\zte\data\data.csv")
trainData=data[~data["label"].isnull()]
trainData["user"].count()
lb=LabelEncoder()
trainData["label_index"]=lb.fit_transform(trainData["label"])
# X=trainData[modelCols]
y=trainData["label_index"]
train_x_all, test_x_all, train_y_all, test_y=train_test_split(trainData,y,random_state=0)

train_data=lgb.Dataset(train_x_all,train_y_all,feature_name=[""],categorical_feature=[""])
test_data=lgb.Dataset(test_x_all,test_y,reference=train_data)


params = {
    'boosting_type': 'gbdt',
    'objective': 'multiclassova',
    'is_unbalance':True,
    'num_class':5,
    'metric': 'multi_logloss',
    'n_estimators':150,
    'max_depth': 9,
    'learning_rate': 0.1,
    'feature_fraction': 0.3,
    'bagging_fraction': 0.8,
    'bagging_freq': 2,
    'lambda_l1':0.1,
    'lambda_l2':0.2,
    'verbose': 0,
    'num_threads':10
}

sorted()
gbm=lgb.train(params,train_data,valid_sets=test_data,early_stopping_rounds=5)
x=gbm.predict(test_x_all)

np.argmax(x)
gbm.feature_importance()

import tensorflow as tf

sess=tf.Session()