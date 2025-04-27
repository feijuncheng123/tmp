/**
  * 功能：imei变动处理
  * 作者：费俊程
  * 创建日期：2018.12.21：16.16
  */

package com.security
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}


class ImeiProcess(sparksess:SparkSession,imeibasepath:String){
    import sparksess.implicits._

    def imeiProcess(): DataFrame ={
        val imeidata=sparksess.read.parquet(s"$imeibasepath*/*").withColumn("filename",input_file_name())
        imeidata.map(row=> {
            val filepath=row.getAs[String]("filename").split("/")
            val filename=filepath.slice(filepath.size-3,filepath.size-2)(0)
            (row.getAs[String]("USER_NUMBER"),filename)
        }).toDF()
    }
}