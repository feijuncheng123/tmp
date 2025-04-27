/**
  * 功能：每日数据处理，使用稳定度计算
  * 作者：费俊程
  * 创建日期：2018.12.21：16.16
  */

package com.security

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}


class DailyDataProcess(sparksess:SparkSession,resourcepath:String){
    import sparksess.implicits._

    private val iotgprsdata: Dataset[Gprs] =sparksess.read.parquet(s"$resourcepath*/iot_gprs").as[Gprs]

    /**
      * 4.7.5 号码日使用情况表，不包含CUST_ID字段
      * @param arg:Int  当日流量高于平均流量的倍数，用于计算流量波动
      * @return
      */
    def dailyDataProcee(arg:Int): DataFrame ={
        val iotsmsdata: Dataset[Sms] =sparksess.read.parquet(s"$resourcepath*/iot_sms").as[Sms]
        val iovgprsdata: Dataset[Gprs] =sparksess.read.parquet(s"$resourcepath*/iov_gprs").as[Gprs]
        val iovsmsdata: Dataset[Sms] =sparksess.read.parquet(s"$resourcepath*/iov_sms").as[Sms]
        val iovvoicedata: Dataset[Voice] =sparksess.read.parquet(s"$resourcepath*/iov_voice").as[Voice]
        //处理物联网流量数据
        val iotgprsdailydata=gprsdataprocess(iotgprsdata)

        //合并物联网流量与短信数据
        val iottotal=iotgprsdailydata.alias("iot")
            .join(iotsmsdata, Seq("START_TIME","USER_NUMBER"), "outer")
            .select($"START_TIME",$"USER_NUMBER",$"iot.*",$"SEND_MESSAGE_COUNT".alias("TOTAL_SMS"))
            .withColumn("TOTAL_VOICE",lit(0))
        iottotal.printSchema()

        //处理车联网流量数据
        val iovgprsdailydata=gprsdataprocess(iovgprsdata)
        //合并车联网短信与语音数据
        val smsvoice=iovsmsdata.alias("iotsms")
            .join(iovvoicedata, Seq("START_TIME","USER_NUMBER"), "outer")
            .select($"START_TIME",$"USER_NUMBER"
                , $"SEND_MESSAGE_COUNT".alias("TOTAL_SMS")
                , ($"CALL_VOLUME"+$"CALLED_VOLUME").alias("TOTAL_VOICE"))

        //合并车联网流量和其他数据
        val iovtotal=iovgprsdailydata.alias("iov")
            .join(smsvoice, Seq("START_TIME","USER_NUMBER"), "outer")
            .select("START_TIME","USER_NUMBER","iov.*","TOTAL_SMS","TOTAL_VOICE")
        iovtotal.printSchema()

        //合并全部数据
        val total=iottotal.union(iovtotal).withColumnRenamed("START_TIME","DAY_CYCLE")
        val volumemean=total.groupBy("USER_NUMBER").agg(mean("TOTAL_FLOW").alias("MEAN_TOTAL_FLOW"))

        //注册一个udf函数：
        val dailydatavsmean=udf((dailydata:Long,meandata:Double,times:Int)=>{if(dailydata/meandata>=times)1 else 0},IntegerType)

        //进行合并并计算波动字段
        total.alias("total").
            join(volumemean,Seq("USER_NUMBER"),"left")
            .withColumn("IS_FLOW_WAVE", dailydatavsmean($"TOTAL_FLOW", $"MEAN_TOTAL_FLOW",lit(arg)))
            .select("USER_NUMBER","TOTAL_FLOW","UP_FLOW","DOWN_FLOW","TOTAL_VOICE","TOTAL_SMS","DAY_CYCLE","IS_FLOW_WAVE")
            .na.fill(0)
            .withColumn("CREATE_DATE",current_timestamp())
    }

    /**
      * dailyDataProcee函数的辅助函数，处理易于计算的字段
      * @param ds:Dataset[Gprs]
      * @return
      */
    private def gprsdataprocess(ds:Dataset[Gprs])={
        ds.select(
            $"USER_NUMBER",

            $"ROAM_VOLUME"+$"LOCAL_VOLUME",

            $"FLOW_00_UP_VOLUME"+$"FLOW_01_UP_VOLUME"+$"FLOW_02_UP_VOLUME"
                +$"FLOW_03_UP_VOLUME"+$"FLOW_04_UP_VOLUME"+$"FLOW_05_UP_VOLUME"
                +$"FLOW_06_UP_VOLUME"+$"FLOW_07_UP_VOLUME"+$"FLOW_08_UP_VOLUME"
                +$"FLOW_09_UP_VOLUME"+$"FLOW_10_UP_VOLUME"+$"FLOW_11_UP_VOLUME",

            $"FLOW_00_DOWN_VOLUME"+$"FLOW_01_DOWN_VOLUME"+$"FLOW_02_DOWN_VOLUME"
                +$"FLOW_03_DOWN_VOLUME"+$"FLOW_04_DOWN_VOLUME"+$"FLOW_05_DOWN_VOLUME"
                +$"FLOW_06_DOWN_VOLUME"+$"FLOW_07_DOWN_VOLUME"+$"FLOW_08_DOWN_VOLUME"
                +$"FLOW_09_DOWN_VOLUME"+$"FLOW_10_DOWN_VOLUME"+$"FLOW_11_DOWN_VOLUME",

            $"START_TIME")
            .toDF("USER_NUMBER","TOTAL_FLOW","UP_FLOW","DOWN_FLOW","START_TIME")
    }

    /**
      * 计算绘图中需要用到的欧式距离，包含上行、下行流量、流量通话的次数的每日每小时的欧式距离
      * @return
      */
    def EucliDistance(): DataFrame ={
        val upvolume=iotgprsdata.select("USER_NUMBER","START_TIME","FLOW_00_UP_VOLUME","FLOW_01_UP_VOLUME",
            "FLOW_02_UP_VOLUME","FLOW_03_UP_VOLUME","FLOW_04_UP_VOLUME","FLOW_05_UP_VOLUME", "FLOW_06_UP_VOLUME",
            "FLOW_07_UP_VOLUME","FLOW_08_UP_VOLUME","FLOW_09_UP_VOLUME", "FLOW_10_UP_VOLUME","FLOW_11_UP_VOLUME")
            .toDF("USER_NUMBER","START_TIME","a1","a2","a3","a4","a5","a6","a7","a8","a9","a10","a11","a12")
            .withColumn("lastday",$"START_TIME").withColumn("dist",lit(0.0))
            .as[Distance]

        val downvolume=iotgprsdata.select("USER_NUMBER","START_TIME","FLOW_00_DOWN_VOLUME",
            "FLOW_01_DOWN_VOLUME","FLOW_02_DOWN_VOLUME","FLOW_03_DOWN_VOLUME","FLOW_04_DOWN_VOLUME",
            "FLOW_05_DOWN_VOLUME", "FLOW_06_DOWN_VOLUME","FLOW_07_DOWN_VOLUME","FLOW_08_DOWN_VOLUME",
            "FLOW_09_DOWN_VOLUME", "FLOW_10_DOWN_VOLUME","FLOW_11_DOWN_VOLUME")
            .toDF("USER_NUMBER","START_TIME","a1","a2","a3","a4","a5","a6","a7","a8","a9","a10","a11","a12")
            .withColumn("lastday",$"START_TIME").withColumn("dist",lit(0.0))
            .as[Distance]

        val volumecount=iotgprsdata.select("USER_NUMBER","START_TIME","FLOW_00_COUNT","FLOW_01_COUNT",
            "FLOW_02_COUNT","FLOW_03_COUNT","FLOW_04_COUNT","FLOW_05_COUNT", "FLOW_06_COUNT","FLOW_07_COUNT",
            "FLOW_08_COUNT","FLOW_09_COUNT", "FLOW_10_COUNT","FLOW_11_COUNT")
            .toDF("USER_NUMBER","START_TIME","a1","a2","a3","a4","a5","a6","a7","a8","a9","a10","a11","a12")
            .withColumn("lastday",$"START_TIME").withColumn("dist",lit(0.0))
            .as[Distance]

        val updistance=calcuDist(upvolume)
        val downdistance=calcuDist(downvolume)
        val countdistance=calcuDist(volumecount)

        updistance.alias("up").join(downdistance.alias("down"),Seq("USER_NUMBER","START_TIME","lastday"),"outer")
            .join(countdistance.alias("count"),Seq("USER_NUMBER","START_TIME","lastday"),"outer")
            .select($"USER_NUMBER",$"START_TIME",$"lastday",
                $"up.dist".alias("updistance"),
                $"down.dist".alias("downdistance"),$"count.dist".alias("countdistance"))
            .na.fill(-1.0)
    }

    /**
      * 在EucliDistance函数的基础上计算每个号码的平均欧式距离，建模用的三个字段
      * @param df:DataFrame
      * @return
      */
    def meanDistance(df:DataFrame): DataFrame ={
        val meanupdistance=calcuMeanDist(df,"updistance")
        val meandowndistance=calcuMeanDist(df,"downdistance")
        val meancountdistance=calcuMeanDist(df,"countdistance")
        meanupdistance.join(meandowndistance,Seq("USER_NUMBER"),"outer")
            .join(meancountdistance,Seq("USER_NUMBER"),"outer").na.fill(-1)
    }

    /**
      * meanDistance函数的辅助函数，计算每个字段的欧式距离
      * @param df:DataFrame
      * @param targetcol:String
      * @return
      */
    private def calcuMeanDist(df:DataFrame,targetcol:String)={
        df.select("USER_NUMBER",targetcol)
            .filter(df(targetcol) > -1)
            .groupBy("USER_NUMBER").mean().toDF("USER_NUMBER",targetcol)
    }

    /**
      * 计算欧式距离的主要逻辑函数
      * @param ds:Dataset[Distance]
      * @return
      */
    private def calcuDist(ds:Dataset[Distance]): DataFrame ={
        val distance=ds
            .groupByKey(_.USER_NUMBER)
            .flatMapGroups((key,rows)=>{
                val sortrows=rows.toList.sortBy(_.START_TIME.toInt)
                var initdist=sortrows.head
                val startdist=initdist
                val distancearray=for(row<-sortrows.tail)yield {
                    val x=Distance.unapply(initdist).get.productIterator.toArray.slice(2,14).map(_.asInstanceOf[Long])
                    val y=Distance.unapply(row).get.productIterator.toArray.slice(2,14).map(_.asInstanceOf[Long])
                    val d = math.sqrt(x.zip(y).map(p=>(p._1-p._2)*(p._1-p._2)).sum)
                    val lastday=initdist.START_TIME
                    initdist=row
                    row.copy(lastday = lastday,dist = d)
                }
                startdist +: distancearray.toArray
            }).as[Distance]
        distance.select("USER_NUMBER","START_TIME","lastday","dist")
    }
}
