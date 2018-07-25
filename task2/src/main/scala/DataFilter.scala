//import java.util
//import java.util.ArrayList
//
//import org.apache.hadoop.io.{LongWritable, Text}
//import org.apache.hadoop.mapred.{FileSplit, InputSplit, TextInputFormat}
//import org.apache.spark.rdd.{HadoopRDD, RDD}
//import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.log4j.Level
//import org.apache.log4j.Logger
//
//object DataFilter {
//
//  private val prefixBlacklist = Set("special:", "media:", "talk:", "user:", "user_talk:", "wikipedia:",
//    "wikipedia_talk:", "file:", "timedtext:", "file_talk:", "timedtext_talk:", "mediawiki:", "mediawiki_talk:",
//    "template:", "template_talk:", "help:", "help_talk:", "category:", "category_talk:", "portal:", "portal_talk:",
//    "topic:", "book:", "book_talk:", "draft:", "draft_talk:", "module:", "gadget:", "module_talk:", "gadget_talk:",
//    "education_program:", "gadget_definition:", "education_program_talk:", "gadget_definition_talk:")
//
//  private val filenameExtensionBlacklist: Set[String] = Set(".png", ".gif", ".jpg", ".jpeg", ".tiff", ".tif",
//    ".xcf", ".mid", ".ogg", ".ogv", ".svg", ".djvu", ".oga", ".flac", ".opus", ".wav", ".webm", ".ico", ".txt",
//    "_(disambiguation)")
//
//  private val specialPages: Set[String] = Set("404.php", "Main_Page", "-")
//
//  def main(args: Array[String]): Unit = {
//    val conf = new SparkConf()
//    val sc = new SparkContext(conf)
//
//
//    /**
//      * When Spark reads a file from HDFS (or WASB on HDInsight),
//      * a partition is created for each input split.
//      *
//      * In this task, each input split is exactly one hourly worth of the pageview data.
//      *
//      * We provide you with the code to parse the raw data into the format of
//      * RDD[(date: Int, line: String)], which is an RDD of all lines in the entire dataset with its date.
//      *
//      * For example, the line "en Kobe_Bryant 168 0" in the file "pageviews-20161109-000000"
//      * will be parsed to
//      * (9: Int, "en Kobe_Bryant 168 0": String)
//      * The '9' stands for the 9th day in November 2016.
//      *
//      * What you need to do is use proper RDD transformations and actions to
//      * generate the exact same result of the MapReduce program you developed in Project 1.
//      *
//      * You do not have to sort the output within the Spark program,
//      * the submitter will take care of it.
//      * export INPUT_FILE_PATH="wasb://wikipediatraf@cmuccpublicdatasets.blob.core.windows.net/201611/"
//      * export  OUTPUT_FILE_PATH="wasb:///filter-output"
//      *
//      */
//    val fileRDD = sc.hadoopFile[LongWritable, Text, TextInputFormat](sys.env("INPUT_FILE_PATH"))
//    val hadoopRDD = fileRDD.asInstanceOf[HadoopRDD[LongWritable, Text]]
//    val data: RDD[(Int, String)] = hadoopRDD.mapPartitionsWithInputSplit((inputSplit: InputSplit, iterator: Iterator[(LongWritable, Text)]) => {
//      val filepath = inputSplit.asInstanceOf[FileSplit].getPath
//      val splits = filepath.toString.split("-")
//      val date = splits(splits.length - 2).substring(6, 8).toInt
//      for (item <- iterator) yield (date, item._2.toString)
//    })
//
//    /**
//      * Implement the function marked with "To be implemented" to finish the following
//      * filter rules.
//      *
//      * 1. Remove malformed lines that don't have four columns;
//      * 2. Percent decoding (you can use the `PercentDecoder.scala`);
//      * 3. Remove the rows if the first column is not "en" or "en.m";
//      * 4. Remove the titles with invalid namespaces;
//      * 5. Filter out all page titles that start with lowercase English characters;
//      * 6. Filter out page titles with miscellaneous filename extensions;
//      * 7. Filter out all disambiguation pages with the suffix "_(disambiguation)";
//      * 8. Remove special pages ("404.php", "Main_Page", "-");
//      * Feel free to go to Project 1 for more details about the filter.
//      *
//      * Replace the `???` with proper code to output the page views for all dates in
//      * a single line in the format as below,
//      * 1. Each line must have 30 days, even if there were zero page views for any particular day;
//      * 2. Output articles that have over 100,000 page-views (100,000 excluded);
//      * 3. The daily views should be sorted in chronological order.
//      */
//    val decodedOutputs = data.map(mapPercentDecode)
//    val intermediateOutput: RDD[(Int, Array[String])] = decodedOutputs.filter(x => filterLength(x) && filterDomain(x) && filterPrefix(x)
//      && filterSuffix(x) && filterSpecialPage(x) && filterFirstLetter(x))
//
//    /**
//      * Aggregate intermediateOutput, similar to how the reducer aggregated intermediate K,V pairs
//      * into the final output in the MapReduce program.
//      */
//    val io1 = intermediateOutput.map(invertKeys).groupByKey()
//    val io2 = io1.mapValues(aggregateIterable).filter(x => x._2 != null)
//      .collect()
//
//    //    output = intermediateOutput.map(rdd => rdd._2.map(titleCountTuple => mapTitleCount(titleCountTuple)))
//
//    /**
//      * The output path should be `wasb:///filter-output`.
//      * This path should not exist.
//      */
//
//    sc.parallelize(io2)
//      .sortBy(_._2._1, false)
//      .map(line => {
//        line._2._1+"\t"+line._1+"\t"+line._2._2.mkString("\t")
//      }).saveAsTextFile(sys.env("OUTPUT_FILE_PATH"))
//
//    sc.stop()
//  }
//
//  /**
//    * Perform percent-decoding and split the record into columns,
//    * separated by single or consecutive whitespaces.
//    *
//    * We pre-implemented this method for you to help you follow and
//    * learn how to perform test-driven development.
//    *
//    * @param x (date, record) where record refers to the pageview record
//    * @return (date, columns)
//    */
//  def mapPercentDecode(x: (Int, String)) = {
//    (x._1, PercentDecoder.decode(x._2).split("\\s+"))
//  }
//
//  /**
//    * Check if column length equals to 4, which can later be used for
//    * the RDD filter.
//    *
//    * @param x (date, columns)
//    * @return true if length equals to 4
//    */
//  def filterLength(x: (Int, Array[String])): Boolean = {
//    val result = if(x._2.length == 4) true else false
//    result
//  }
//
//  /**
//    * Check if the domain code is en or en.m (case sensitive)
//    *
//    * @param x (date, columns)
//    * @return true if the domain code is en or en.m
//    */
//  def filterDomain(x: (Int, Array[String])): Boolean = {
//    val result = if( "en".equals(x._2(0)) || "en.m".equals(x._2(0))) true else false
//    result
//  }
//
//  /**
//    * Check if the title starts with any blacklisted prefix, case
//    * insensitive.
//    *
//    * @param x (date, columns)
//    * @return true if the title doesn't start with any blacklisted
//    *         prefix
//    */
//  def filterPrefix(x: (Int, Array[String])): Boolean = {
//    val result = if(!prefixBlacklist.exists(name => x._2(1).toLowerCase.startsWith(name))) true else false
//    result
//  }
//
//  /**
//    * Check if the title ends with any blacklisted suffix, case
//    * insensitive.
//    *
//    * @param x (date, columns)
//    * @return true if the title doesn't end with any blacklisted suffix
//    */
//  def filterSuffix(x: (Int, Array[String])): Boolean = {
//    val result = if(!filenameExtensionBlacklist.exists(name => x._2(1).toLowerCase.endsWith(name))) true else false
//    result
//  }
//
//  /**
//    * Check if the title is any special page, case sensitive.
//    *
//    * @param x (date, columns)
//    * @return true if the title isn't any special page
//    */
//  def filterSpecialPage(x: (Int, Array[String])): Boolean = {
//    val result = if(!specialPages.exists(name => x._2(1).equals(name))) true else false
//    result
//  }
//
//  /**
//    * Check if the first letter of the title is an English lowercase
//    * letter.
//    *
//    * Many other Unicode characters are lowercase too.
//    * Only [a-z] should count.
//    *
//    * @param x (date, columns)
//    * @return true if the first letter of the title is not an English
//    *         lowercase letter
//    */
//  def filterFirstLetter(x: (Int, Array[String])): Boolean = {
//    val range = 'a' to 'z'
//    val result = if(!range.contains(x._2(1).charAt(0))) true else false
//    result
//  }
//
//
//  def invertKeys(x: (Int,Array[String])): (String, (Int, Long)) = {
//    val tuple = (x._1,x._2(2).toLong)
//    (x._2(1), tuple)
//  }
//
//  def aggregateIterable(v: Iterable[(Int, Long)]) = {
//    val arr = new Array[Long](30)
//    var totalcount = 0l
//    println("Printing a test line *****")
//    for (x <- v) {
//      arr(x._1-1)= arr(x._1-1)+x._2
//      totalcount=totalcount+x._2
//    }
//    //return..
//    if(totalcount >100000) (totalcount,arr) else null
//  }
//
//}
