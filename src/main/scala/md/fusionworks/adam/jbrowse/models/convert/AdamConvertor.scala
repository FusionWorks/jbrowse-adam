package md.fusionworks.adam.jbrowse.models.convert

import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.bdgenomics.adam.rdd.{ADAMContext, ADAMRDDFunctions}

object AdamConvertor {

  def fasta(){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val fileDirectory = new java.io.File("/home/sergiu/!files/convert/input/")
    for(file <- fileDirectory.listFiles if file.getName endsWith ".fasta"){
      val nameFile = file.getName
      println(s"convert file: $nameFile")
    val reads = ac.loadSequence(file.toString)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(s"/home/sergiu/!files/convert/output/$nameFile.adam")}
  }

  def vcf(){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val fileDirectory = new java.io.File("/home/sergiu/!files/convert/input/")
    for(file <- fileDirectory.listFiles if file.getName endsWith ".vcf"){
      val nameFile = file.getName
      println(s"convert file: $nameFile")
    val reads = ac.loadVariants(file.toString)
    val save = new ADAMRDDFunctions(reads)
    save.adamParquetSave(s"/home/sergiu/!files/convert/output/$nameFile.adam")}
  }

  def bam_sam(){
    val sc = SparkContextFactory.getSparkContext
    val ac = new ADAMContext(sc)
    val fileDirectory = new java.io.File("/home/sergiu/!files/convert/input/")
    for(file <- fileDirectory.listFiles if file.getName endsWith ".bam" ) {
      val nameFile = file.getName
      println(s"convert file: $nameFile")
      val reads = ac.loadAlignments(file.toString)
      val save = new ADAMRDDFunctions(reads)
      save.adamParquetSave(s"/home/sergiu/!files/convert/output/$nameFile.adam")
    }
    for(file <- fileDirectory.listFiles if file.getName endsWith ".sam" ) {
      val nameFile = file.getName
      println(s"convert file: $nameFile")
      val reads = ac.loadAlignments(file.toString)
      val save = new ADAMRDDFunctions(reads)
      save.adamParquetSave(s"/home/sergiu/!files/convert/output/$nameFile.adam")
    }
  }
}
