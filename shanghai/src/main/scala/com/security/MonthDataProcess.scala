
/**
  * 功能：月度汇总数据处理、模型大部分数据、沉默用户数据
  * 作者：费俊程
  * 创建日期：2018.12.21：16.16
  */
package com.security

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

class MonthDataProcess(sparksess:SparkSession,resourcepath:String) {
    import sparksess.implicits._

    private val monthiotgprs:Dataset[MonthGprs]=sparksess.read.parquet(s"$resourcepath/iot_gprs").as[MonthGprs]
    private val monthiotsms: Dataset[MonthSms] =sparksess.read.parquet(s"$resourcepath/iot_sms").as[MonthSms]
    private val monthiovgprs: Dataset[MonthGprs] =sparksess.read.parquet(s"$resourcepath/iov_gprs").as[MonthGprs]
    private val monthiovsms: Dataset[MonthSms] =sparksess.read.parquet(s"$resourcepath/iov_sms").as[MonthSms]
    private val monthiovvoice: Dataset[MonthVoice] =sparksess.read.parquet(s"$resourcepath/iov_voice").as[MonthVoice]

    /**
      * 4.7.6号码月使用情况表，不包含Industry_id、CUST_ID字段
      * @return
      */
    def monthDataProcee(): DataFrame ={
        val iotgprsdata=gprsdataprocess(monthiotgprs)
        val iot=iotgprsdata.alias("iotgprsdata")
            .join(monthiotsms.alias("sms"),Seq("USER_NUMBER"),"outer")
            .select($"USER_NUMBER",$"iotgprsdata.*",$"RECENT_30_COUNT".alias("TOTAL_SMS"))
            .withColumn("TOTAL_VOICE",lit(0))

        val iovgprsdata=gprsdataprocess(monthiovgprs)
        val smsandvoice=monthiovsms.alias("sms")
            .join(monthiovvoice.alias("voice"),Seq("USER_NUMBER"),"outer")
            .select($"USER_NUMBER",$"sms.RECENT_30_COUNT".alias("TOTAL_SMS")
                ,($"RECENT_30_CALL_VOLUME"+$"RECENT_30_CALLED_VOLUME").alias("TOTAL_VOICE"))
        val iov=iovgprsdata.join(smsandvoice,Seq("USER_NUMBER"),"outer")
        iot.union(iov).na.fill(0).withColumn("CREATE_DATE",current_timestamp())
    }

    /**
      * monthDataProcee函数的辅助函数
      * @param ds:Dataset[MonthGprs]
      * @return
      */
    private def gprsdataprocess(ds:Dataset[MonthGprs])={
        ds.select(
            $"USER_NUMBER",
            $"RECENT_30_ROAM_VOLUME"+$"RECENT_30_LOCAL_VOLUME",

            $"RECENT_30_00_UP_VOLUME"+$"RECENT_30_01_UP_VOLUME"+$"RECENT_30_02_UP_VOLUME"
                +$"RECENT_30_03_UP_VOLUME"+$"RECENT_30_04_UP_VOLUME"+$"RECENT_30_05_UP_VOLUME"
                +$"RECENT_30_06_UP_VOLUME"+$"RECENT_30_07_UP_VOLUME"+$"RECENT_30_08_UP_VOLUME"
                +$"RECENT_30_09_UP_VOLUME"+$"RECENT_30_10_UP_VOLUME"+$"RECENT_30_11_UP_VOLUME",

            $"RECENT_30_00_DOWN_VOLUME"+$"RECENT_30_01_DOWN_VOLUME"+$"RECENT_30_02_DOWN_VOLUME"
                +$"RECENT_30_03_DOWN_VOLUME"+$"RECENT_30_04_DOWN_VOLUME"+$"RECENT_30_05_DOWN_VOLUME"
                +$"RECENT_30_06_DOWN_VOLUME"+$"RECENT_30_07_DOWN_VOLUME"+$"RECENT_30_08_DOWN_VOLUME"
                +$"RECENT_30_09_DOWN_VOLUME"+$"RECENT_30_10_DOWN_VOLUME"+$"RECENT_30_11_DOWN_VOLUME",

            $"RECENT_30_COUNT"
        ).toDF("USER_NUMBER","TOTAL_FLOW","UP_FLOW","DOWN_FLOW","TOTAL_FLOW_FC")
    }

    /**
      *计算建模特征表里的大部分字段，不包含稳定度数据、企业中位数数据
      * @return
      */
    def modelData(): DataFrame ={
        val basedata=monthiotgprs.select(
            $"USER_NUMBER",$"RECENT_30_DAY_COUNT",
            $"RECENT_30_01_COUNT"+$"RECENT_30_02_COUNT",$"RECENT_30_03_COUNT"+$"RECENT_30_04_COUNT",
            $"RECENT_30_05_COUNT"+$"RECENT_30_06_COUNT",$"RECENT_30_07_COUNT"+$"RECENT_30_08_COUNT",
            $"RECENT_30_09_COUNT"+$"RECENT_30_10_COUNT",$"RECENT_30_11_COUNT"+$"RECENT_30_00_COUNT",
            $"RECENT_30_01_UP_VOLUME"+$"RECENT_30_02_UP_VOLUME",$"RECENT_30_03_UP_VOLUME"+$"RECENT_30_04_UP_VOLUME",
            $"RECENT_30_05_UP_VOLUME"+$"RECENT_30_06_UP_VOLUME",$"RECENT_30_07_UP_VOLUME"+$"RECENT_30_08_UP_VOLUME",
            $"RECENT_30_09_UP_VOLUME"+$"RECENT_30_10_UP_VOLUME",$"RECENT_30_11_UP_VOLUME"+$"RECENT_30_00_UP_VOLUME",
            $"RECENT_30_01_DOWN_VOLUME"+$"RECENT_30_02_DOWN_VOLUME",$"RECENT_30_03_DOWN_VOLUME"+$"RECENT_30_04_DOWN_VOLUME",
            $"RECENT_30_05_DOWN_VOLUME"+$"RECENT_30_06_DOWN_VOLUME",$"RECENT_30_07_DOWN_VOLUME"+$"RECENT_30_08_DOWN_VOLUME",
            $"RECENT_30_09_DOWN_VOLUME"+$"RECENT_30_10_DOWN_VOLUME",$"RECENT_30_11_DOWN_VOLUME"+$"RECENT_30_00_DOWN_VOLUME",

            $"RECENT_30_00_4G_UP_VOLUME"+$"RECENT_30_01_4G_UP_VOLUME"+$"RECENT_30_02_4G_UP_VOLUME"
                +$"RECENT_30_03_4G_UP_VOLUME"+$"RECENT_30_04_4G_UP_VOLUME"+$"RECENT_30_05_4G_UP_VOLUME"
                +$"RECENT_30_06_4G_UP_VOLUME"+$"RECENT_30_07_4G_UP_VOLUME"+$"RECENT_30_08_4G_UP_VOLUME"
                +$"RECENT_30_09_4G_UP_VOLUME"+$"RECENT_30_10_4G_UP_VOLUME"+$"RECENT_30_11_4G_UP_VOLUME"
                +$"RECENT_30_00_4G_DOWN_VOLUME"+$"RECENT_30_01_4G_DOWN_VOLUME"+$"RECENT_30_02_4G_DOWN_VOLUME"
                +$"RECENT_30_03_4G_DOWN_VOLUME"+$"RECENT_30_04_4G_DOWN_VOLUME"+$"RECENT_30_05_4G_DOWN_VOLUME"
                +$"RECENT_30_06_4G_DOWN_VOLUME"+$"RECENT_30_07_4G_DOWN_VOLUME"+$"RECENT_30_08_4G_DOWN_VOLUME"
                +$"RECENT_30_09_4G_DOWN_VOLUME"+$"RECENT_30_10_4G_DOWN_VOLUME"+$"RECENT_30_11_4G_DOWN_VOLUME"
        ).toDF("USER_NUMBER","RECENT_30_DAY_COUNT","count_02to06","count_06to10","count_10to14",
            "count_14to18","count_18to22","count_22to02","upvolume_02to06","upvolume_06to10","upvolume_10to14",
            "upvolume_14to18","upvolume_18to22","upvolume_22to02","downvolume_02to06","downvolume_06to10",
            "downvolume_10to14","downvolume_14to18","downvolume_18to22","downvolume_22to02","volume_4G")
        basedata.alias("basedata")
            .join(monthiotsms,Seq("USER_NUMBER"),"left")
            .select($"basedata.*",$"RECENT_30_COUNT".alias("sms_count"))
    }


    def useDaysCount(): DataFrame ={
        val iotdays=monthiotgprs.select("USER_NUMBER","RECENT_30_DAY_COUNT")
        val iovdays=monthiovgprs.select("USER_NUMBER","RECENT_30_DAY_COUNT")
        iotdays.union(iovdays).withColumn("CREATE_DATE",current_timestamp())
    }
    
    def hourUseFrequency(): Dataset[Row] ={
        val iotfrequency=monthiotgprs.select($"USER_NUMBER",$"RECENT_30_00_COUNT",$"RECENT_30_01_COUNT",$"RECENT_30_02_COUNT",
            $"RECENT_30_03_COUNT",$"RECENT_30_04_COUNT", $"RECENT_30_05_COUNT",$"RECENT_30_06_COUNT",
            $"RECENT_30_07_COUNT",$"RECENT_30_08_COUNT", $"RECENT_30_09_COUNT",$"RECENT_30_10_COUNT",
            $"RECENT_30_11_COUNT")
        val iovfrequency=monthiovgprs.select($"USER_NUMBER",$"RECENT_30_00_COUNT",$"RECENT_30_01_COUNT",$"RECENT_30_02_COUNT",
            $"RECENT_30_03_COUNT",$"RECENT_30_04_COUNT", $"RECENT_30_05_COUNT",$"RECENT_30_06_COUNT",
            $"RECENT_30_07_COUNT",$"RECENT_30_08_COUNT", $"RECENT_30_09_COUNT",$"RECENT_30_10_COUNT",
            $"RECENT_30_11_COUNT")
        iotfrequency.union(iovfrequency)
    }

    def hourUpVolume(): Dataset[Row] ={
        val iotupvolume=monthiotgprs.select($"USER_NUMBER",$"RECENT_30_00_UP_VOLUME",$"RECENT_30_01_UP_VOLUME",
            $"RECENT_30_02_UP_VOLUME", $"RECENT_30_03_UP_VOLUME",$"RECENT_30_04_UP_VOLUME", $"RECENT_30_05_UP_VOLUME",
            $"RECENT_30_06_UP_VOLUME", $"RECENT_30_07_UP_VOLUME",$"RECENT_30_08_UP_VOLUME", $"RECENT_30_09_UP_VOLUME",
            $"RECENT_30_10_UP_VOLUME", $"RECENT_30_11_UP_VOLUME")
        
        val iovupvolume=monthiovgprs.select($"USER_NUMBER",$"RECENT_30_00_UP_VOLUME",$"RECENT_30_01_UP_VOLUME",
            $"RECENT_30_02_UP_VOLUME", $"RECENT_30_03_UP_VOLUME",$"RECENT_30_04_UP_VOLUME", $"RECENT_30_05_UP_VOLUME",
            $"RECENT_30_06_UP_VOLUME", $"RECENT_30_07_UP_VOLUME",$"RECENT_30_08_UP_VOLUME", $"RECENT_30_09_UP_VOLUME",
            $"RECENT_30_10_UP_VOLUME", $"RECENT_30_11_UP_VOLUME")
        iotupvolume.union(iovupvolume)
    }

    def hourDownVolume(): Dataset[Row] ={
        val iotdownvolume=monthiotgprs.select($"USER_NUMBER",$"RECENT_30_00_DOWN_VOLUME",$"RECENT_30_01_DOWN_VOLUME",
            $"RECENT_30_02_DOWN_VOLUME",$"RECENT_30_03_DOWN_VOLUME",$"RECENT_30_04_DOWN_VOLUME",$"RECENT_30_05_DOWN_VOLUME",
            $"RECENT_30_06_DOWN_VOLUME",$"RECENT_30_07_DOWN_VOLUME",$"RECENT_30_08_DOWN_VOLUME", $"RECENT_30_09_DOWN_VOLUME",
            $"RECENT_30_10_DOWN_VOLUME",$"RECENT_30_11_DOWN_VOLUME")

        val iovdownvolume=monthiovgprs.select($"USER_NUMBER",$"RECENT_30_00_DOWN_VOLUME",$"RECENT_30_01_DOWN_VOLUME",
            $"RECENT_30_02_DOWN_VOLUME",$"RECENT_30_03_DOWN_VOLUME",$"RECENT_30_04_DOWN_VOLUME",$"RECENT_30_05_DOWN_VOLUME",
            $"RECENT_30_06_DOWN_VOLUME",$"RECENT_30_07_DOWN_VOLUME",$"RECENT_30_08_DOWN_VOLUME", $"RECENT_30_09_DOWN_VOLUME",
            $"RECENT_30_10_DOWN_VOLUME",$"RECENT_30_11_DOWN_VOLUME")
        iotdownvolume.union(iotdownvolume)
    }

}