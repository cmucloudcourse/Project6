import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._


object FollowerDF {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Spark DF Answer")
      .config("spark.some.config.option", "Task1")
      .getOrCreate()

    val sc = spark.sparkContext
    val followerRDD = sc.textFile("wasb://spark@cmuccpublicdatasets.blob.core.windows.net/Graph")

    import spark.implicits._
    val columns = Array("followee","follower")
    val dataframe = followerRDD.map(line => (line.split("\t")(1), line.split("\t")(0))).toDF(columns: _*)
    val finaldf = dataframe.groupBy("followee").count().orderBy(desc("count")).limit(100)
    finaldf.show()
    finaldf.write.format("parquet").save("wasb:///followerDF-output")
  }

}