from pyspark.sql import SparkSession
spark=SparkSession.builder().getOrCreate()
data=spark.sql("select * from zc_pro1778_role4.dwa_oneid_sample_data_tmp")


pyModelData = data.toPandas()


