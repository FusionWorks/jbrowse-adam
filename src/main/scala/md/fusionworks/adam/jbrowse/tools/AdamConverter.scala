package md.fusionworks.adam.jbrowse.tools

import htsjdk.samtools.ValidationStringency
import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.apache.spark.rdd.RDD
import org.bdgenomics.adam.models.{RecordGroupDictionary, SequenceDictionary, SequenceRecord, VariantContext}
import org.bdgenomics.adam.projections.{AlignmentRecordField, Filter}
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.read.AlignmentRecordRDD
import org.bdgenomics.formats.avro.{Contig, RecordGroupMetadata}
import org.bdgenomics.utils.cli.{Args4jBase, ParquetArgs}
import org.kohsuke.args4j.{Argument, Option}

case class argsTemplate(var outputPath: String = null) extends ADAMSaveAnyArgs with ParquetArgs {
  val sortFastqOutput: Boolean = false
  val asSingleFile: Boolean = false
  val sortReads = false
}

object AdamConverter {

  def fastaToADAM(inputPath: String, outputPath: String) = {
    //getSparkContext(inputPath).loadSequence(inputPath).adamParquetSave(outputPath)
    getSparkContext(inputPath)
      .loadFasta(inputPath, 10000L)
      .adamParquetSave(argsTemplate(outputPath))

  }

  def vcfToADAM(inputPath: String, outputPath: String) = {
    //getSparkContext(inputPath).loadVariants(inputPath).adamParquetSave(outputPath)
    getSparkContext(inputPath)
      .loadVcf(inputPath, sd = None)
      .coalesce(numPartitions = -1, shuffle = false)
      .flatMap(_.genotypes)
      .adamParquetSave(argsTemplate(outputPath))
  }

  def transformToADAM(inputPath: String, outputPath: String) = {
    //getSparkContext(inputPath).loadAlignments(inputPath).rdd.adamParquetSave(outputPath)
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