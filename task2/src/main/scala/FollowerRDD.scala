import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.{FileSplit, InputSplit, TextInputFormat}
import org.apache.spark.rdd.{HadoopRDD, RDD}
import org.apache.spark.{SparkConf, SparkContext}

object FollowerRDD {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val followerRDD = sc.textFile("wasb://spark@cmuccpublicdatasets.blob.core.windows.net/Graph")

    val data = followerRDD.map(line => (line.split("\t")(0), 1))
      .reduceByKey(_+_)
      .sortBy(line => line._2,false)
      .top(100).map(line => line._1+"\t"+line._2)


    sc.parallelize(data).saveAsTextFile("wasb:///followerRDD-output")
  }

}