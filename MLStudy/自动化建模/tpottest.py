colsName=["subs_id", "imsi", "duration", "join_month", "churn_out_subs", "msisdn_row_id", "area_id",
  "acct_nbr", "billing_cycle_type_id", "age", "subs_state","org_id", "subs_plan_id",
  "product_id", "subs_type", "network_type", "block_reason", "imp_grade_id", "cert_type_id",
  "cert_id", "new_stop_tag", "price_plan_id", "stop_tag", "on_tag", "remove_tag",
  "lte_user_tag", "vsd_06", "calling_other_network_duration_06", "vsd_07", "calling_other_network_duration_07",
  "vsd_08", "calling_other_network_duration_08", "vsd_09", "calling_other_network_duration_09",
  "vsd_10", "calling_other_network_duration_10", "call_duration_06", "call_ext_duration_06",
  "call_ext_num_06", "data_usage_volume_06", "interroam_called_duration_06", "call_duration_07",
  "call_ext_duration_07", "call_ext_num_07", "data_usage_volume_07", "interroam_called_duration_07",
  "call_duration_08", "call_ext_duration_08", "call_ext_num_08", "data_usage_volume_08",
  "interroam_called_duration_08", "call_duration_09", "call_ext_duration_09", "call_ext_num_09",
  "data_usage_volume_09", "interroam_called_duration_09", "call_duration_10", "call_ext_duration_10",
  "call_ext_num_10", "data_usage_volume_10", "interroam_called_duration_10", "recv_amount_06", "sdp_charge_06",
  "transfer_amount_06", "onetime_amount_06", "sms_interroam_num_06", "sms_ext_send_num_06",
  "recv_amount_07", "sdp_charge_07", "transfer_amount_07", "onetime_amount_07", "sms_interroam_num_07", "sms_ext_send_num_07",
  "recv_amount_08", "sdp_charge_08", "transfer_amount_08", "onetime_amount_08", "sms_interroam_num_08", "sms_ext_send_num_08", "recv_amount_09",
  "sdp_charge_09", "transfer_amount_09", "onetime_amount_09", "sms_interroam_num_09",
  "sms_ext_send_num_09", "recv_amount_10", "sdp_charge_10", "transfer_amount_10", "onetime_amount_10",
  "sms_interroam_num_10", "sms_ext_send_num_10", "interroam_calling_duration_06", "noroam_called_duration_06",
  "noroam_calling_duration_06", "interroam_calling_duration_07", "noroam_called_duration_07",
  "noroam_calling_duration_07", "interroam_calling_duration_08", "noroam_called_duration_08",
  "noroam_calling_duration_08", "interroam_calling_duration_09", "noroam_called_duration_09",
  "noroam_calling_duration_09", "interroam_calling_duration_10", "noroam_called_duration_10",
  "noroam_calling_duration_10", "loan_count_06", "month_bal_06", "month_cost_06", "loan_count_07", "month_bal_07",
  "month_cost_07", "loan_count_08", "month_bal_08", "month_cost_08", "loan_count_09",
  "month_bal_09", "month_cost_09", "loan_count_10", "month_bal_10", "month_cost_10"]

dropCols=["subs_id", "imsi", "duration", "msisdn_row_id", "area_id",
      "acct_nbr", "billing_cycle_type_id", "age", "subs_state","org_id", "subs_plan_id",
      "product_id", "subs_type", "network_type", "block_reason", "imp_grade_id", "cert_type_id",
      "cert_id", "new_stop_tag", "price_plan_id", "stop_tag", "on_tag", "remove_tag",
      "lte_user_tag"]

targetCols="churn_out_subs"

from tpot import TPOTClassifier
from sklearn.datasets import load_digits
from sklearn.model_selection import train_test_split
import pandas as pd
from sklearn.preprocessing import LabelEncoder

pd.set_option('display.max_columns', 50)
pd.options.display.float_format = '{:20,.2f}'.format

def preprocess(data):
    data.drop(columns=dropCols, axis=1, inplace=True)
    print(data.dtypes)
    object_cols = data.select_dtypes(["object"])
    lb = LabelEncoder()
    for col in object_cols:
        data[col] = lb.fit_transform(data[col])
    train_data = data.drop(targetCols, axis=1)
    target_data = data.loc[:, targetCols]
    return train_data,target_data

traindata=pd.read_csv("tpottest/test.csv",header=None,names=colsName)
train_data, target_data=preprocess(traindata)

pipeline_optimizer = TPOTClassifier(generations=5, population_size=20, cv=5,
                                    random_state=42, verbosity=2,n_jobs=-1)
pipeline_optimizer.fit(train_data, target_data)



x=pipeline_optimizer.predict_proba(X_test)    #这个方法是输出概率的

# y=list(map(lambda z:str(z),x))·
# print(y)
# pred=pipeline_optimizer.predict(X_test)
# X_test["predict"]=pred
# X_test["target"]=y_test.values
# print(X_test.head)
# result1=pd.Series(y)
# X_test['prob']=result1.values
# print(X_test.head)
# X_test.to_csv("result.csv")

# pipeline_optimizer.export('tpot_exported_pipeline.py')

