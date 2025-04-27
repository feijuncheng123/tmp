import pandas as pd
import cx_Oracle
import time
import numpy as np
import logging
import lightgbm
from datetime import datetime

logging.basicConfig(level=logging.INFO, datefmt='%Y-%m-%d %H:%M:%S',
                    format='%(lineno)d %(filename)s %(asctime)s %(levelname)s %(message)s')
logger=logging.getLogger()


featureCols=["CALL_DURATION","CALL_MAX_DURATION","CALL_MIN_DURATION","CALL_TIMES","CALL_NUMS","CALLING_DURATION","CALLING_MAX_DURATION",
           "CALLING_MIN_DURATION","CALLING_TIMES","CALLING_NUMS","CALLED_DURATION","CALLED_MAX_DURATION","CALLED_MIN_DURATION",
           "CALLED_TIMES","CALLED_NUMS","SHORT_TIMES","FIXED_TIMES","CALL_IN_00_07_TIMES",
           "CALL_IN_07_13_TIMES","CALL_IN_13_19_TIMES","CALL_IN_19_24_TIMES","WORKING_DAY_TIMES","WEEKEND_TIMES","INTERNATIONAL_TIMES",
           "CALLING_STATION_NUMS","FLOW_USAGE","SEND_MSG_NUMS","RECEIVE_MSG_NUMS","CALL_CHARGE","FLOW_CHARGE","MESSAGE_CHARGE",
            "dur_per_times","dur_per_num","times_per_num",
            "calling_dur_per_times","calling_dur_per_num","calling_times_per_num","called_dur_per_times","called_dur_per_num","called_times_per_num",
            "percent_of_calling_dur","percent_of_calling_times","percent_of_workday_times"]


class handler:
    def __init__(self):
        logger.info("开始进行数据库链接！")
        self.connect = cx_Oracle.connect("fms", "fms123", cx_Oracle.makedsn("10.45.50.78",1521,"orcl"))
        self.cursor=self.connect.cursor()
        logger.info("开始加载模型文件！")
        self.gbm = lightgbm.Booster(model_file='/root/model/lgbm_model.txt')


    def getSTatus(self):
        self.cursor.execute("select * from FMS_AI_ENGINE")
        st = self.cursor.fetchall()
        title = [i[0] for i in self.cursor.description]
        status=dict(zip(title,st[0]))
        return status

    def getData(self,tb):
        startTime=time.time()
        self.cursor.execute(" select * from %s"%tb)
        data = self.cursor.fetchall()
        title = [i[0] for i in self.cursor.description]
        df = pd.DataFrame(data,columns=title)
        endTime=time.time()
        logger.info("取数耗时：%s"%(round(endTime-startTime,4)))
        return df



    def dataProcess(self,modeldata):
        modeldata["dur_per_times"] = modeldata["CALL_DURATION"] / modeldata["CALL_TIMES"].replace(0, np.inf)
        modeldata["dur_per_num"] = modeldata["CALL_DURATION"] / modeldata["CALL_NUMS"].replace(0, np.inf)
        modeldata["times_per_num"] = modeldata["CALL_TIMES"] / modeldata["CALL_NUMS"].replace(0, np.inf)
        modeldata["calling_dur_per_times"] = modeldata["CALLING_DURATION"] / modeldata["CALLING_TIMES"].replace(0,np.inf)
        modeldata["calling_dur_per_num"] = modeldata["CALLING_DURATION"] / modeldata["CALLING_NUMS"].replace(0,np.inf)
        modeldata["calling_times_per_num"] = modeldata["CALLING_TIMES"] / modeldata["CALLING_NUMS"].replace(0,np.inf)
        modeldata["called_dur_per_times"] = modeldata["CALLED_DURATION"] / modeldata["CALLED_TIMES"].replace(0,np.inf)
        modeldata["called_dur_per_num"] = modeldata["CALLED_DURATION"] / modeldata["CALLED_NUMS"].replace(0,np.inf)
        modeldata["called_times_per_num"] = modeldata["CALLED_TIMES"] / modeldata["CALLED_NUMS"].replace(0,np.inf)
        modeldata["percent_of_calling_dur"] = modeldata["CALLING_DURATION"] / modeldata["CALL_DURATION"].replace(0,np.inf)
        modeldata["percent_of_calling_times"] = modeldata["CALLING_TIMES"] / modeldata["CALL_TIMES"].replace(0,np.inf)
        modeldata["percent_of_workday_times"] = modeldata["WORKING_DAY_TIMES"] / modeldata["CALL_TIMES"].replace(0,np.inf)
        return modeldata


    def predictDta(self,df):
        model_data=df[featureCols]
        y_pred = self.gbm.predict(model_data, num_iteration=self.gbm.best_iteration)
        pred_label = np.where(y_pred > 0.5, 1, 0)
        result=pd.crosstab(df["STATUS"],pred_label)
        logger.info(result)
        df["pred_label"]=pred_label
        return df

    def FMS_ALERT_INFO_01(self,df,infoDict):
        df["trigger_time"]=datetime.now()
        df["rule_id"]=infoDict["RULE_ID"]
        df["monitor_table_id"]=infoDict["MONITOR_TABLE_ID"]
        df["monitor_table_name"]=infoDict["MONITOR_TABLE_NAME"]
        df["rule_version"] = infoDict["RULE_VERSION"]
        insertData=df[["trigger_time","rule_id","rule_version","BATCH_ID","SUB_ID","monitor_table_id","monitor_table_name"]].values.tolist()
        sql="""insert into FMS_ALERT_INFO(ALERT_ID,TRIGGER_TIME,RULE_ID,RULE_VERSION,BATCH_ID,ALERT_OBJ_ID,MONITOR_TABLE_ID,MONITOR_TABLE_NAME) 
        values(FMS_SEQ_ALERT_ID.nextval,:trigger_time,:rule_id,:rule_version,:batch_id,:alert_obj_id,:monitor_table_id,:monitor_table_name)"""
        self.cursor.executemany(sql,insertData)
        self.connect.commit()
        logger.info("FMS_ALERT_INFO数据已提交，提交总数为：%s,样例数据：%s"%(len(insertData),insertData[0]))


    def FMS_ALARM_INFO_02(self,df,infoDict):
        df["trigger_time"] = datetime.now()
        df["update_time"] = datetime.now()
        df["fraud_type_id"] = infoDict["FRAUD_TYPE_ID"]
        df["rule_set_id"] = infoDict["RULE_SET_ID"]
        df["rule_set_version"] = infoDict["RULE_SET_VERSION"]
        df["analysis_object_id"] = infoDict["ANALYSIS_OBJECT_ID"]
        df["monitor_table_id"]=infoDict["MONITOR_TABLE_ID"]
        df["monitor_table_name"]=infoDict["MONITOR_TABLE_NAME"]
        insertData=df[["fraud_type_id","rule_set_id","rule_set_version","BATCH_ID","update_time","analysis_object_id","SUB_ID","trigger_time","SUB_NO","monitor_table_id","monitor_table_name"]].values.tolist()
        sql="""insert into FMS_ALARM_INFO(ALARM_ID,FRAUD_TYPE_ID,RULE_SET_ID,RULE_SET_VERSION,BATCH_ID,UPDATE_TIME,ANALYSIS_OBJECT_ID,ALARM_OBJECT_ID,TRIGGER_TIME,ALARM_OBJECT_NUM,
        MONITOR_TABLE_ID,MONITOR_TABLE_NAME) values(FMS_SEQ_ALARM_ID.nextval,:fraud_type_id,:rule_set_id,:rule_set_version,:batch_id,:update_time,:analysis_object_id,
        :alert_obj_id,:trigger_time,:SUB_NO,:monitor_table_id,:monitor_table_name)"""
        self.cursor.executemany(sql,insertData)
        self.connect.commit()
        logger.info("FMS_ALARM_INFO数据已提交，提交总数为：%s,样例数据：%s"%(len(insertData),insertData[0]))

    def FMS_ALARM_ALERT_RELATION_03(self,batch_id):
        sql="""insert into FMS_ALARM_ALERT_RELATION(ALARM_ID,ALERT_ID,ANALYSIS_OBJECT_ID,ALARM_OBJECT_ID) 
        select a.ALARM_ID,b.ALERT_ID,9,a.ALARM_OBJECT_ID from FMS_ALARM_INFO a inner join FMS_ALERT_INFO b on a.ALARM_OBJECT_ID=b.ALERT_OBJ_ID 
        where a.BATCH_ID=%s and b.BATCH_ID=%s"""%(batch_id,batch_id)
        self.cursor.execute(sql)
        self.connect.commit()
        logger.info("FMS_ALARM_ALERT_RELATION数据已提交")

    def FMS_TASK_INFO_04(self,infoDict):
        self.cursor.execute("select TASK_ID_SEQ.nextval as task_id from dual")
        task_id=self.cursor.fetchall()
        dispatch_time=datetime.now()
        t=time.strftime('%m%d%H%M',time.localtime(time.time()))
        sql="""insert into FMS_TASK_INFO(TASK_ID,TASK_NO,FLOW_STATE,DISPATCH_USER_ID,DISPATCH_USER_NAME,DISPATCH_TIME,CREATE_TIME,FRAUD_TYPE_ID) 
        values (:1,:2,2,43,'fms',:3,:4,:5)"""
        self.cursor.execute(sql,(task_id[0][0],"%s%s"%(t,task_id[0][0]),dispatch_time,dispatch_time,infoDict["FRAUD_TYPE_ID"]))
        self.connect.commit()
        logger.info("FMS_TASK_INFO数据已提交")
        return task_id[0][0]

    def FMS_ALARM_TASK_REL_05(self,task_id,batch_id):
        alarm_id_sql="select ALARM_ID from FMS_ALARM_INFO where BATCH_ID= %s"%batch_id
        self.cursor.execute(alarm_id_sql)
        alarm_id = self.cursor.fetchall()
        sql="insert into FMS_ALARM_TASK_REL(TASK_ID,ALARM_ID) values (%s,:dt)"%task_id
        self.cursor.executemany(sql, alarm_id)
        self.connect.commit()
        logger.info("FMS_ALARM_TASK_REL数据已提交,提交数据量为:%s"%len(alarm_id))

    def FMS_POC_CALL_STATEMENT_ALT_06(self,batch_id):
        sql="insert into FMS_POC_CALL_STATEMENT_ALT select a.*,b.ALERT_ID from FMS_POC_CALL_STATEMENT a inner join FMS_ALERT_INFO b on a.SUB_ID=b.ALERT_OBJ_ID where b.BATCH_ID=%s"%batch_id
        self.cursor.execute(sql)
        self.connect.commit()
        logger.info("FMS_POC_CALL_STATEMENT_ALT已更新")

    def update_state(self,batch_id):
        self.cursor.execute("UPDATE FMS_AI_ENGINE SET ENGINE_STATE = 'end'")
        self.cursor.execute("UPDATE FMS_TIF_BATCH_MGR SET PROCESS_STATE = 1,FINISH_TIME = SYSDATE where BATCH_ID= :batch_id",{"batch_id":batch_id})
        self.connect.commit()
        logger.info("ENGINE_STATE 状态更新为end！")

    def close(self):
        self.connect.close()


def main():
    modelHandler=handler()
    while True:
        statusDict=modelHandler.getSTatus()
        if statusDict["ENGINE_STATE"] == "start":
            logger.info("状态更新为start，开始执行！")
            tablename=statusDict["MONITOR_TABLE_NAME"]
            sourceData=modelHandler.getData(tablename)
            processDaata=modelHandler.dataProcess(sourceData)
            resultDF=modelHandler.predictDta(processDaata)
            oracleData=resultDF[resultDF["pred_label"]==1][["SUB_NO","BATCH_ID","SUB_ID"]].drop_duplicates()
            batchid=oracleData["BATCH_ID"].unique().tolist()
            logger.info("batchId:%s"%batchid[0])
            if len(batchid)>1:
                logger.warning("batchid数量超过1个，请核查！")
            modelHandler.FMS_ALERT_INFO_01(oracleData,statusDict)
            modelHandler.FMS_POC_CALL_STATEMENT_ALT_06(batchid[0])
            modelHandler.FMS_ALARM_INFO_02(oracleData,statusDict)
            modelHandler.FMS_ALARM_ALERT_RELATION_03(batchid[0])
            task_id=modelHandler.FMS_TASK_INFO_04(statusDict)
            modelHandler.FMS_ALARM_TASK_REL_05(task_id,batchid[0])
            modelHandler.update_state(batchid[0])
        elif statusDict["ENGINE_STATE"]=="close’":
            logger.info("状态更新为close，关闭程序！")
            modelHandler.close()
            break
        else:
            logger.info("状态为end，3秒后重试！")
            time.sleep(3)
            continue


if __name__ == '__main__':
    main()


