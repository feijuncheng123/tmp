package com.test

import org.apache.spark.sql.SparkSession

object MyTest {
  def main(args: Array[String]): Unit = {
    val spark=SparkSession.builder().getOrCreate();
    import spark.implicits._

    val data=spark.read.parquet("");


    data.cache()

  }

}
