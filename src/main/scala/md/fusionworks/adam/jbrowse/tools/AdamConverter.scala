package md.fusionworks.adam.jbrowse.tools

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.ADAMContext._

object AdamConverter {

  def fastaToADAM(inputPath: String, outputPath: String) = {
    getSparkContext(inputPath).loadSequence(inputPath).adamParquetSave(outputPath)
  }

  def vcfToADAM(inputPath: String, outputPath: String) = {
    getSparkContext(inputPath).loadVariants(inputPath).adamParquetSave(outputPath)
  }

  def bam_samToADAM(inputPath: String, outputPath: String) = {
    getSparkContext(inputPath).loadAlignments(inputPath).adamParquetSave(outputPath)
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
      AdamConverter.bam_samToADAM(args(0), args(1))
    case Some("sam") =>
      AdamConverter.bam_samToADAM(args(0), args(1))
    case Some("vcf") =>
      AdamConverter.vcfToADAM(args(0), args(1))
    case _ =>
      println("Unknown file extension.")
  }
}