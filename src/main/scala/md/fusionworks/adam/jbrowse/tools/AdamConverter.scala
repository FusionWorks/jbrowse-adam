package md.fusionworks.adam.jbrowse.tools

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.ADAMContext._

object AdamConverter {
  private val sc = SparkContextFactory.getSparkContext(Some("local[*]"))

  def fastaToADAM(inputPath: String, outputPath: String) = {
    sc.loadSequence(inputPath).adamParquetSave(outputPath)
  }

  def vcfToADAM(inputPath: String, outputPath: String) = {
    sc.loadVariants(inputPath).adamParquetSave(outputPath)
  }

  def bam_samToADAM(inputPath: String, outputPath: String) = {
    sc.loadAlignments(inputPath).adamParquetSave(outputPath)
  }

}
