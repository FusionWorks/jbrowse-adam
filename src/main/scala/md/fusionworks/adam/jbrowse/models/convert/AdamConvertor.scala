package md.fusionworks.adam.jbrowse.models.convert

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.{ADAMContext, ADAMRDDFunctions}

object AdamConvertor {

  def fastaToADAM(input :String, output :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val reads = ac.loadSequence(input)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(output)
  }

  def vcfToADAM(input :String, output :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val reads = ac.loadVariants(input)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(output)
  }

  def bam_samToADAM(input :String, output :String){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
      val reads = ac.loadAlignments(input)
      val save = new ADAMRDDFunctions(reads)
      save.adamParquetSave(output)
  }

}
