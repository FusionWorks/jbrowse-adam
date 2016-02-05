package md.fusionworks.adam.jbrowse.tools

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.{ADAMContext, ADAMRDDFunctions}

object AdamConverter {

  def fastaToADAM(inputPath :String, outputPath :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val reads = ac.loadSequence(inputPath)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(outputPath)
  }

  def vcfToADAM(inputPath :String, outputPath :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val reads = ac.loadVariants(inputPath)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(outputPath)
  }

  def bam_samToADAM(inputPath :String, outputPath :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
      val reads = ac.loadAlignments(inputPath)
      val save = new ADAMRDDFunctions(reads)
      save.adamParquetSave(outputPath)
  }

}
