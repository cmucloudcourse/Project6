import org.apache.spark.{SparkConf, SparkContext}

object FollowerDF {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val followerRDD = sc.textFile("wasb://spark@cmuccpublicdatasets.blob.core.windows.net/Graph")

    val data = followerRDD.map(line => (line.split("\t")(1), 1))
      .reduceByKey(_+_)
      .sortBy(line => line._2,false).collect()
      .take(100)


    sc.parallelize(data).sortBy(_._2, false).map(line => line._1+"\t"+line._2).saveAsTextFile("wasb:///followerDF-output")
  }

}