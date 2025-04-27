
import pandas as pd
import logging
import cx_Oracle

logging.basicConfig(level=logging.INFO, datefmt='%Y-%m-%d %H:%M:%S',
                    format='%(lineno)d %(filename)s %(asctime)s %(levelname)s %(message)s')
logger=logging.getLogger()


# feature_field=[]


class loadData:
    def __init__(self):
        logger.info("开始进行数据库链接！")
        self.connect = cx_Oracle.connect("fms", "smart", cx_Oracle.makedsn("10.45.50.199",1521,"cc"))
        self.cursor=self.connect.cursor()


    def getData(self):
        self.cursor.execute(" select a.*,b.* from FMS_POC_CALL_STATEMENT a left join FMS_POC_RECHARGE_STATEMENT b on a.SUB_NO = b.RECHARGE_NUM")
        data = self.cursor.fetchall()
        title = [i[0] for i in self.cursor.description]
        df = pd.DataFrame(data,columns=title)
        return df


if __name__ == '__main__':
    ob=loadData()
    df=ob.getData()
    df.to_csv(r"E:\python\Pro1\untitled\MLStudy\data\aisai\data.csv",index=False)



