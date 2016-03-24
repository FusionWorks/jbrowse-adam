package md.fusionworks.adam.jbrowse.tools

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.utils.cli.{Args4jBase, ParquetArgs}

case class argsTemplate(var outputPath: String = null) extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  var sortFastqOutput: Boolean = false
  var asSingleFile: Boolean = false
  val sortReads = false
}

object AdamConverter {

  def fastaToADAM(inputPath: String, outputPath: String) = {
    getSparkContext(inputPath)
      .loadFasta(inputPath, 10000L)
      .adamParquetSave(argsTemplate(outputPath))

  }

  def vcfToADAM(inputPath: String, outputPath: String) = {
    getSparkContext(inputPath)
      .loadVcf(inputPath, sd = None)
      .flatMap(_.genotypes)
      .adamParquetSave(argsTemplate(outputPath))
  }

  def transformToADAM(inputPath: String, outputPath: String) = {
    val aRdd = getSparkContext(inputPath).loadBam(inputPath)
    val args = argsTemplate(outputPath)
    val (rdd, sd, rgd) = (aRdd.rdd, aRdd.sequences, aRdd.recordGroups)
    rdd.adamSave(args, sd, rgd, args.sortReads)
  }

  def getSparkContext(inputPath: String) = {
    if (inputPath.startsWith("file://"))
      SparkContextFactory.getSparkContext(Some("local[*]"))
    else
      SparkContextFactory.getSparkContext()
  }
}

// For cluster launch with spark-submit
object ConvertToAdam extends App {

  args(0).toLowerCase.split('.').drop(1).lastOption match {
    case Some("fasta") =>
      AdamConverter.fastaToADAM(args(0), args(1))
    case Some("bam") =>
      AdamConverter.transformToADAM(args(0), args(1))
    case Some("sam") =>
      AdamConverter.transformToADAM(args(0), args(1))
    case Some("fastq") =>
      AdamConverter.transformToADAM(args(0), args(1))
    case Some("vcf") =>
      AdamConverter.vcfToADAM(args(0), args(1))
    case _ =>
      println("Unknown file extension.")
  }
}