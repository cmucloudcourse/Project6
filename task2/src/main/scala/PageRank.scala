/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Computes the PageRank of URLs from an input file. Input file should
  * be in format of:
  * URL         neighbor URL
  * URL         neighbor URL
  * URL         neighbor URL
  * ...
  * where URL and their neighbors are separated by space(s).
  *
  * This is an example implementation for learning how to use Spark. For more conventional use,
  * please refer to org.apache.spark.graphx.lib.PageRank
  *
  * Example Usage:
  * {{{
  * bin/run-example SparkPageRank data/mllib/pagerank_data.txt 10
  * }}}
  */
object PageRank {


  def main(args: Array[String]) {

    val INPUT_FILE = sys.env("INPUT_FILE_PATH")
    //"wasb://spark@cmuccpublicdatasets.blob.core.windows.net/Graph"
    val OUTPUT_FILE = sys.env("OUTPUT_FILE_PATH") //"wasb:///pagerank-output"

    val spark = SparkSession
      .builder
      .appName("PageRank")
      .config(getConfig)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer", "1024k")
      .config("spark.kryoserializer.buffer.max", "1024m")
      .config("spark.kryo.registrationRequired", "true")
      .getOrCreate()


    val iters = if (args.length > 1) args(1).toInt else 10
    val lines = spark.read.textFile(INPUT_FILE).rdd



    val links = lines.map { s =>
      val parts = s.split("\t")
      (parts(0), parts(1))
    }.groupByKey()

    val followee = links.values.flatMap(v =>v )
    val follower = links.map(link => (link._1))
    val dangNodes = followee.subtract(follower).distinct().collect()


    val dangArr = dangNodes.map(node => (node, Iterable[String]()))

    val dangRDD = spark.sparkContext.parallelize(dangArr)

    val finalLinks = links.union(dangRDD)

    finalLinks.cache()

    val numNodes = 1006458





    var ranks = finalLinks.mapValues(v => 1.0 / numNodes)


    for (i <- 1 to iters) {
      val dangling = spark.sparkContext.doubleAccumulator
      val contribs = finalLinks.join(ranks).values.flatMap {
        case (urls, rank) => {
          val size = urls.size
          if (size == 0) {
            dangling.add(rank)
            List()
          } else {
            urls.map(url => (url, rank / size))
          }
        }
      }

      contribs.count()
      val danglingVal = dangling.value

      ranks = contribs.reduceByKey(_ + _, finalLinks.getNumPartitions).mapValues(v => (0.15 / numNodes) + 0.85 * (v + (danglingVal / numNodes)))

    }

    val output = ranks.collect()
    //    output.foreach(tup => println(s"${tup._1} has rank:  ${tup._2} ."))
    spark.sparkContext.parallelize(output).map(tup => tup._1 + "\t" + tup._2) saveAsTextFile (OUTPUT_FILE)
    spark.stop()
  }


  private def getConfig = {
    val conf = new SparkConf()
    conf.registerKryoClasses(
      Array(
        classOf[scala.collection.mutable.WrappedArray.ofRef[_]],
        Class.forName("scala.reflect.ClassTag$$anon$1"),
        Class.forName("[Lorg.apache.spark.util.collection.CompactBuffer"),
        Class.forName("org.apache.spark.internal.io.FileCommitProtocol$TaskCommitMessage"),
        Class.forName("java.lang.Class"),
        Class.forName("PageRank")
      )
    )
  }
}