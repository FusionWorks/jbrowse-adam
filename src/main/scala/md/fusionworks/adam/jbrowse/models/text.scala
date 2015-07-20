package md.fusionworks.adam.jbrowse.models

/**
 * Created by fret on 7/6/15.
 */


import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.projections._
import org.bdgenomics.adam.rdd.ADAMContext

import scala.io.Source


class text {

  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val ac = new ADAMContext(sc)
  val reads = ac.loadAlignments(
    "src/main/resources/adam2.adam",
    projection = Some(Projection(
      AlignmentRecordField.sequence,
      AlignmentRecordField.readMapped,
      AlignmentRecordField.mapq)))

  val kmers = reads.flatMap {
    read => read.getSequence.sliding(21).map(k => (k, 1L))
  }
  //println("_____________________________\n"+reads.count())

}
class lista {
  val lines = Source.fromFile("C:/Users/Giorgio/Desktop/jbrowse-adam/Test/part-00000").getLines.mkString
}




/*
import org.apache.spark.SparkContext
import org.bdgenomics.adam.projections._
import org.bdgenomics.adam.rdd.ADAMContext
import org.apache.spark.SparkConf
import org.apache.log4j.Logger
import org.apache.log4j.Level

class text {

   // val conf = new SparkConf().setMaster("http://localhost:8080/").setAppName("Simple Application")
   // val sc = new SparkContext(conf)
   val conf = new SparkConf().setAppName("Simple Application").setMaster("spark://jbrowse:7077").set("spark.cores.max", "10")
  Logger.getLogger("org").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  val sc = new SparkContext(conf)
    val ac = new ADAMContext(sc)
    // Load alignments from disk
    val reads = ac.loadAlignments(
      "Ad2f.adam",
      projection = Some(Projection(
        AlignmentRecordField.sequence,
        AlignmentRecordField.readMapped,
        AlignmentRecordField.mapq)))

  println("_____________________________\n"+reads)

  }


import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "jbrowse/setup.sh" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }
}
*/


/*

val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
val sc = new SparkContext(conf)

*/


/*
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.bdgenomics.adam.projections.{AlignmentRecordField, Projection}
import org.bdgenomics.adam.rdd.ADAMContext


val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
val sc = new SparkContext(conf)
val ac = new ADAMContext(sc)
val reads = ac.loadAlignments(
"Ad2f.adam",
projection = Some(Projection(
AlignmentRecordField.sequence,
AlignmentRecordField.readMapped,
AlignmentRecordField.mapq)))

println("_____________________________\n"+reads.count())


_______________________________________




import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.bdgenomics.adam.projections.{AlignmentRecordField, Projection}
import org.bdgenomics.adam.rdd.ADAMContext


val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
val sc = new SparkContext(conf)
val ac = new ADAMContext(sc)
// Load alignments from disk
val reads = ac.loadAlignments(
  "src/main/resources/Ad2f.adam",
  projection = Some(Projection(
    AlignmentRecordField.sequence,
    AlignmentRecordField.readMapped,
    AlignmentRecordField.mapq)))

val kmers = reads.flatMap {
  read => read.getSequence.sliding(21).map(k => (k, 1L))
}.reduceByKey((k1: Long, k2: Long) => k1 + k2).map(_.swap).sortByKey(ascending = false)


//reads.first()
//reads.collect()
//reads.distinct().count()
//reads.take(10).foreach(println)
//println("_____________________________\n"+reads.foreach(println))



*/
