import pandas as pd
import time
import numpy as np
import logging
from sklearn.externals import joblib
from datetime import datetime
import os

os.chdir(os.path.abspath(os.path.dirname(__file__)))

logging.basicConfig(level=logging.INFO, datefmt='%Y-%m-%d %H:%M:%S',
                    format='%(lineno)d %(filename)s %(asctime)s %(levelname)s %(message)s')
logger=logging.getLogger()

# header=["SUB_NO","CALL_MONTH","CALL_DURATION","CALL_MAX_DURATION","CALL_MIN_DURATION","CALL_STD_DURATION","CALL_TIMES","CALL_NUMS",
#         "CALLING_DURATION","CALLING_MAX_DURATION","CALLING_MIN_DURATION","CALLING_STD_DURATION","CALLING_TIMES","CALLING_NUMS",
#         "CALLED_DURATION","CALLED_MAX_DURATION","CALLED_MIN_DURATION","CALLED_STD_DURATION","CALLED_TIMES","CALLED_NUMS","SHORT_TIMES",
#         "FIXED_TIMES","ADJACENT_MAX_PERIOD","ADJACENT_MIN_PERIOD","ADJACENT_STD_PERIOD","CALL_IN_00_07_TIMES","CALL_IN_07_13_TIMES",
#         "CALL_IN_13_19_TIMES","CALL_IN_19_24_TIMES","WORKING_DAY_TIMES","WEEKEND_TIMES","INTERNATIONAL_TIMES","CALLING_STATION_NUMS",
#         "FLOW_USAGE","SEND_MSG_NUMS","RECEIVE_MSG_NUMS","CALL_CHARGE","FLOW_CHARGE","MESSAGE_CHARGE","FLAG","STATUS","CUST_ID",
#         "RECHARGE_TIMES","MAX_RECHARGE_AMOUNT","MIN_RECHARGE_AMOUNT","TOTAL_RECHARGE_AMOUNT","BATCH_ID","SUB_ID"]


featureCols=["CALL_DURATION","CALL_MAX_DURATION","CALL_MIN_DURATION","CALL_TIMES","CALL_NUMS","CALLING_DURATION","CALLING_MAX_DURATION",
           "CALLING_MIN_DURATION","CALLING_TIMES","CALLING_NUMS","CALLED_DURATION","CALLED_MAX_DURATION","CALLED_MIN_DURATION",
           "CALLED_TIMES","CALLED_NUMS","SHORT_TIMES","FIXED_TIMES","CALL_IN_00_07_TIMES",
           "CALL_IN_07_13_TIMES","CALL_IN_13_19_TIMES","CALL_IN_19_24_TIMES","WORKING_DAY_TIMES","WEEKEND_TIMES","INTERNATIONAL_TIMES",
           "CALLING_STATION_NUMS","FLOW_USAGE","SEND_MSG_NUMS","RECEIVE_MSG_NUMS","CALL_CHARGE","FLOW_CHARGE","MESSAGE_CHARGE",
            "dur_per_times","dur_per_num","times_per_num",
            "calling_dur_per_times","calling_dur_per_num","calling_times_per_num","called_dur_per_times","called_dur_per_num","called_times_per_num",
            "percent_of_calling_dur","percent_of_calling_times","percent_of_workday_times"]


class predict_handle:
    def __init__(self,model_path,data_path):
        logger.info("开始加载模型:%s"%model_path)
        self.model = joblib.load(model_path)
        logger.info("预测文件路径为:%s"%data_path)
        self.data_path=data_path

    def read_data(self):
        # data = pd.read_csv(self.data_path,header=None,names=header, dtype={'SUB_NO':'str','CALL_MONTH':'str'})
        data = pd.read_csv(self.data_path, dtype={'SUB_NO':'str','CALL_MONTH':'str'})
        data["SUB_NO"] = data["SUB_NO"].astype("str")
        df = data.fillna(0)
        return df

    def dataProcess(self, modeldata):
        modeldata["dur_per_times"] = modeldata["CALL_DURATION"] / modeldata["CALL_TIMES"].replace(0, np.inf)
        modeldata["dur_per_num"] = modeldata["CALL_DURATION"] / modeldata["CALL_NUMS"].replace(0, np.inf)
        modeldata["times_per_num"] = modeldata["CALL_TIMES"] / modeldata["CALL_NUMS"].replace(0, np.inf)
        modeldata["calling_dur_per_times"] = modeldata["CALLING_DURATION"] / modeldata["CALLING_TIMES"].replace(0, np.inf)
        modeldata["calling_dur_per_num"] = modeldata["CALLING_DURATION"] / modeldata["CALLING_NUMS"].replace(0, np.inf)
        modeldata["calling_times_per_num"] = modeldata["CALLING_TIMES"] / modeldata["CALLING_NUMS"].replace(0, np.inf)
        modeldata["called_dur_per_times"] = modeldata["CALLED_DURATION"] / modeldata["CALLED_TIMES"].replace(0, np.inf)
        modeldata["called_dur_per_num"] = modeldata["CALLED_DURATION"] / modeldata["CALLED_NUMS"].replace(0, np.inf)
        modeldata["called_times_per_num"] = modeldata["CALLED_TIMES"] / modeldata["CALLED_NUMS"].replace(0, np.inf)
        modeldata["percent_of_calling_dur"] = modeldata["CALLING_DURATION"] / modeldata["CALL_DURATION"].replace(0, np.inf)
        modeldata["percent_of_calling_times"] = modeldata["CALLING_TIMES"] / modeldata["CALL_TIMES"].replace(0, np.inf)
        modeldata["percent_of_workday_times"] = modeldata["WORKING_DAY_TIMES"] / modeldata["CALL_TIMES"].replace(0, np.inf)
        return modeldata

    def predictData(self,df):
        model_data=df[featureCols]
        y_pred = self.model.predict(model_data)
        result=pd.crosstab(df["STATUS"],y_pred)
        logger.info("\n"+result.to_string())
        df["pred_label"]=y_pred
        return df


if __name__ == '__main__':
    predict_tool=predict_handle("model/gbdt_model.pkl","data/predict_data.csv")
    data=predict_tool.read_data()
    modeldata=predict_tool.dataProcess(data)
    result=predict_tool.predictData(modeldata)
