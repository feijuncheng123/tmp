all_cols=["latn_id","acc_nbr","cust_type","urban_rural_id","sex","open_dur","years_old","invalid_user_flag",
          "call_5g_cnt","tmnl_brand","shopping_name","tmnl_price_level","tmnl_use_days","tmnl_source",
          "main_offer_spec_name","original_charge","original_dur","offer_gprs","offer_duration",
          "change_cnt","is_rh","is_bxl","is_hy","traffic_data_total_02","traffic_data_total_01",
          "traffic_data_total_12","number_of_deceleration","tw_flux_charge","llb_flux_charge","value_month_cnt",
          "value_month_charge","duration_calling_02","duration_calling_01","duration_calling_12","cnt_active_days",
          "acct_aggr_charge1","acct_aggr_charge2","acct_aggr_charge3","offer_inst_create_date","is_5g"]


drop_cols=["prod_inst_merg_state","prod_inst_id","acct_id","area_id","mobss_org_level_name2",
           "mobss_org_level_id3","mobss_org_level_id4","offer_inst_id","main_offer_spec_id",
           "date_no_","working_place_location","user_live_location","offer_gprs","offer_duration","change_cnt",
           "value_3month_cnt","value_3month_charge","mobss_org_level_name4","mobss_org_level_name3",
           "main_offer_spec_name"]

int_cols=["urban_rural_id","years_old","original_charge","is_hy","number_of_deceleration"]

double_cols=["traffic_data_total_02","traffic_data_total_01","traffic_data_total_12","duration_calling_02",
             "duration_calling_01","duration_calling_12","acct_aggr_charge1","acct_aggr_charge2","acct_aggr_charge3"]

tmp_cols=int_cols+double_cols
other_cols = [i for i in all_cols if i not in tmp_cols]

terminal_top50=["华为","OPPO","VIVO","苹果","null","荣耀","小米","百合","SAMSUNG","比酷","海信","诺而信","老来宝",
                "中兴","酷派","锋达通","天元","金立","广信","联想","全盈","红橙果","中诺","小天才","瑞翼","信天游",
                "IVVI","TCL","青橙","国美","小蜜蜂","蓝天","小辣椒","唐为","博瑞","360手机","洪洋伟业","魅族","MOTO",
                "同威","长虹","NUBIA","HALOVE九爱","酷比","BB安","本为","中维恒泰","朵唯","HTC","库麦"]


from pyspark.sql import SparkSession
from pyspark.sql.types import *
from pyspark.sql import functions as F
from pyspark.sql.window import Window
from pyspark.ml.classification import RandomForestClassifier,GBTClassifier
from pyspark.ml.feature import StringIndexer,VectorAssembler,VectorIndexer
from pyspark.ml.pipeline import Pipeline
import pandas as pd
import matplotlib.pyplot as plt
import  xgboost

from sklearn.datasets import load_breast_cancer


spark=SparkSession.builder.appName("model").config("spark.executor.instances",20).config("spark.executor.memory","4g").config("spark.executor.cores",4).getOrCreate()

# 读取数据源
df=spark.sql("select * from op_app_channel.iwhale_user_base")\
    .drop("tmnl_use_days")\
    .withColumn("tmnl_use_days",F.datediff(F.lit("2020-02-29 00:00:00"),F.col("terminal_register_date")))\
    .drop("terminal_register_date")


# 类型转换
string_to_int=[df[i].cast(IntegerType()) for i in int_cols]
string_to_double=[df[i].cast(DoubleType()) for i in double_cols]
select_cols=other_cols+string_to_int+string_to_double
a=df.select(*select_cols)\
    .withColumn("open_dur",F.datediff(F.lit("2020-04-01 00:00:00"),df['open_date']))\
    .withColumn("terminal_dur",F.datediff(F.lit("2020-04-01 00:00:00"),df['terminal_register_date']))\
    .withColumn("terminal_name",F.when(df['tmnl_brand'].isin(terminal_top50),df['tmnl_brand']).otherwise("other"))\
    .drop('open_date',"tmnl_brand","terminal_register_date")


# 描述性统计
a.describe().show()

# 零值统计
zero_cnt=[F.sum(F.when(a[i]== "",1).when(a[i]== "0",1).otherwise(0)).alias(i) for i in a.columns]
zero_df=a.select(*zero_cnt)

# 唯一值统计
unique_cnt=[F.countDistinct(a[i]).alias(i) for i in a.columns]
unique_df=a.select(*unique_cnt)

# 终端、终端档次统计
a.groupBy("tmnl_brand").pivot("is_5g").count().show(500)
a.groupBy("tmnl_price_level").pivot("is_5g").count().sort(F.desc("1")).show(500)
a.groupBy("original_charge").pivot("is_5g").count().show(500)
a.groupBy("is_5g").mean("open_dur").show(500)
a.orderBy()

# 对负值进行替换(全部替换为0值),填充空值为0
negative_cols=["original_dur","tw_flux_charge","llb_flux_charge","original_charge","acct_aggr_charge3"]
others=[F.col(i) for i in all_cols if i not in negative_cols and i != "tmnl_use_days"]

negative_to_p=[F.when(F.col(i)<0,0).otherwise(F.col(i)).alias(i) for i in negative_cols]
special_neg=F.when(F.col("tmnl_use_days")<0,1).otherwise(F.col("tmnl_use_days")).alias("tmnl_use_days")
others.extend(negative_to_p)
others.append(special_neg)
processed_data=df.select(*others).fillna(0)

processed_data.printSchema()


#定义衍生变量
func01=Window.partitionBy("shopping_name")
func02=Window.partitionBy("main_offer_spec_name")
processed_data_01=processed_data\
    .withColumn("shopping_name_cnt",F.count("acc_nbr").over(func01))\
    .withColumn("main_offer_spec_name_cnt",F.count("acc_nbr").over(func02))\
    .withColumn("terminal_name",F.when(df['tmnl_brand'].isin(terminal_top50),df['tmnl_brand']).otherwise("other"))\
    .withColumn("sample_flag",
                F.when(F.col("offer_inst_create_date").between("20200301","20200331"),1)
                .when(F.col("offer_inst_create_date")>="20200401",3)
                .when(F.col("offer_inst_create_date").isNull(),0).otherwise(2))\
    .withColumn("is_gprs_over_1k",
                F.when(F.col("offer_gprs")>1000,1).when(F.col("offer_gprs").between(20.1,1000),2)
                .when(F.col("offer_gprs").between(10,20),3).when(F.col("offer_gprs")<10,4).when(F.col("offer_gprs")==0,0))\
    .drop("tmnl_brand")


# 特征处理
model_cols=["cust_type","urban_rural_id","sex","open_dur","years_old","invalid_user_flag","call_5g_cnt",
            "offer_gprs","offer_duration","is_rh","is_bxl","is_hy","traffic_data_total_02",
            "traffic_data_total_01","traffic_data_total_12","number_of_deceleration","value_month_cnt",
            "value_month_charge","duration_calling_02","duration_calling_01","duration_calling_12",
            "cnt_active_days","acct_aggr_charge1","acct_aggr_charge2","original_dur","tw_flux_charge",
            "llb_flux_charge","original_charge","acct_aggr_charge3","tmnl_use_days","shopping_name_cnt",
            "main_offer_spec_name_cnt","terminal_name_indexed","tmnl_price_level_indexed","tmnl_source_indexed",
            "is_gprs_over_1k"]

model_source_data=processed_data_01.filter("sample_flag != 2")
stringCols=["terminal_name","tmnl_price_level","tmnl_source"]
stringIndexerStage=[StringIndexer(inputCol=i, outputCol=i+"_indexed",handleInvalid="keep") for i in stringCols]
vectorAssembleStage=VectorAssembler(inputCols=model_cols,outputCol="features")
vectorIndexer=VectorIndexer(maxCategories=60,inputCol="features",outputCol="features_indexed")
stringIndexerStage.extend([vectorAssembleStage,vectorIndexer])
model_pipline=Pipeline(stages=stringIndexerStage).fit(processed_data)

model_data=model_pipline.transform(processed_data)

# 采样
positive_sample=model_data.filter("is_5g=1")
negative_sample=model_data.filter("is_5g=0").sample(withReplacement=False,fraction=0.05,seed=666666)
sample_data=positive_sample.union(negative_sample)

# 切分训练集测试集
train_data,test_data=sample_data.randomSplit([0.8, 0.2], 888888)


# 调用算法
rf=RandomForestClassifier(featuresCol="features_indexed",
                          labelCol="is_5g",
                          maxDepth=7,
                          maxBins=60,
                          numTrees=200,
                          subsamplingRate=0.7,
                          seed=12345)
gbdt=GBTClassifier(featuresCol="features_indexed",
                   labelCol="is_5g",
                   maxDepth=7,
                   maxBins=60,
                   maxIter=100,
                   subsamplingRate=0.7,
                   seed=12345)


# 训练
rf_model=rf.fit(train_data)
gbdt_model=gbdt.fit(train_data)


# 字段重要性
rf_impl=rf_model.featureImportances
x=list(rf_impl.toArray())
list(zip(model_cols,x)).sort(key=lambda x:x[1])

# 测试模型
rf_test=rf_model.transform(test_data)
gbdt_test=gbdt_model.transform(test_data)


# 计算混淆矩阵
check=rf_test.select("is_5g","prediction")
check.cache()
truePositive=check.filter("is_5g=1").filter("prediction=1").count()
falsePositive=check.filter("is_5g=0").filter("prediction=1").count()
trueNegative=check.filter("is_5g=0").filter("prediction=0").count()
falseNegative=check.filter("is_5g=1").filter("prediction=0").count()
precision=truePositive/(truePositive+falsePositive)
recall=truePositive/(truePositive+falseNegative)

# 保存
result_source_data=model_data.filter("sample_flag=0")
result=rf_model.transform(result_source_data)

result

df.write.saveAsTable("op_app_channel.iwhale_user_result")


prob_df1=df.withColumn("probability_new",F.col("probability").cast("String"))\
    .withColumn('p0',F.split(F.regexp_replace("probability_new", "^\[|\]", ""), ",")[0].cast(DoubleType()))\
    .withColumn('p1',F.split(F.regexp_replace("probability_new", "^\[|\]", ""), ",")[1].cast(DoubleType()))\
    .select("acc_nbr","offer_inst_create_date","p0","p1","prediction")


# 转化为pandas对象
data=a.drop(*drop_cols).toPandas().fillna(0)
data.dtypes
data.head()

spark.stop()

import seaborn as sns
sns.pairplot()

check.show()



from sparkxgb import XGBoostClassifier

xgb=XGBoostClassifier(objective='binary:logistic',
                      objectiveType="classification",
                      eta=0.1,
                      maxDepth=7,
                      lambda_=1,
                      colsampleBytree=0.6,
                      subsample=0.7,
                      growPolicy="lossguide",
                      evalMetric="auc",
                      numWorkers=10,
                      useExternalMemory=True,
                      numEarlyStoppingRounds=20,
                      maximizeEvaluationMetrics=True,
                      labelCol="is_5g",
                      trainTestRatio=0.3,
                      seed=666666
                      )
xgb.fit()