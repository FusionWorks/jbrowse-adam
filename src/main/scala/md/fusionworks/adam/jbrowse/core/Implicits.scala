package md.fusionworks.adam.jbrowse.core

import htsjdk.samtools.SAMFileHeader
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, DataFrame}
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.SAMFileHeaderWritable
import org.bdgenomics.formats.avro.{Variant, AlignmentRecord, NucleotideContigFragment}
import parquet.org.codehaus.jackson.map.ObjectMapper
import org.bdgenomics.adam.rdd.ADAMContext._


object Implicits {
  implicit def dfToDataFrameFunctions(df: DataFrame): DataFrameFunctions = new DataFrameFunctions(df)

  implicit def toReferenceRDDFunctions(rdd: RDD[NucleotideContigFragment]): ReferenceRDDFunctions =
    new ReferenceRDDFunctions(rdd)

  implicit def toAlignmentRDDFunctions(rdd: RDD[AlignmentRecord]): AlignmentRDDFunctions =
    new AlignmentRDDFunctions(rdd)
  implicit def toVariantRDDFunctions(rdd: RDD[Variant]): VariantRDDFunctions =
    new VariantRDDFunctions(rdd)
}



class DataFrameFunctions(df: DataFrame) {

  def filterReferenceDF(start: Long, end: Long, contigName: String) = {
    val firstRow = df.first()
    val contigLength = firstRow.getAs[Row]("contig").getAs[Long]("contigLength")
    val numberOfFragmentsInContig = firstRow.getAs[Integer]("numberOfFragmentsInContig")
    val fragmentLength = contigLength / numberOfFragmentsInContig

    df.filter(df("contig.contigName") === contigName)
      .filter(
        df("fragmentStartPosition") < end && df("fragmentStartPosition") != null &&
          df("fragmentStartPosition") + fragmentLength > start && df("fragmentStartPosition") != null
      )
  }

  def referenceDfToRDD = {
    df.toJSON.mapPartitions(partition => {
      val mapper = new ObjectMapper()
      partition.map(str => mapper.readValue(str, classOf[NucleotideContigFragment]))
    })
  }


  def filterAlignmentDF(start: Long, end: Long, contigName: String) = {
    df.filter(df("contig.contigName") === contigName)
      .filter(
        df("start") < end && df("start") != null &&
          df("end") > start && df("end") != null
      )
  }

  def alignmentDfToRDD = {
    df.toJSON.mapPartitions(partition => {
      val mapper = new ObjectMapper()
      partition.map(str => mapper.readValue(str, classOf[AlignmentRecord]))
    })
  }

  def filterVariantDF(start: Long, end: Long, contigName: String) = {
    df.filter(df("contig.contigName") === contigName)
      .filter(
        df("start") < end && df("start") != null &&
          df("end") > start && df("end") != null
      )
  }


  def variantsDfToRDD = {
    df.toJSON.mapPartitions(partition => {
      val mapper = new ObjectMapper()
      partition.map(str => mapper.readValue(str, classOf[Variant]))
    })
  }

}

class ReferenceRDDFunctions(rdd: RDD[NucleotideContigFragment]) {

  def toJBrowseFormat = {
    rdd.map(record => RecordConverter.referenceRecToJbFormat(record))
  }

}

class AlignmentRDDFunctions(rdd: RDD[AlignmentRecord]) {

  def toJBrowseFormat(contigName: String) = {
    val header = RecordConverter.getHeader(contigName, rdd)
    val converter = new AlignmentRecordConverter
    rdd.mapPartitions { case partition =>
      partition.map(record => RecordConverter.alignmentRecToJbFormat(record, header, converter))
    }
  }

}

class VariantRDDFunctions(rdd: RDD[Variant]) {

  def toJBrowseFormat = {
    rdd.map(record => RecordConverter.variantRecToJbFormat(record))
  }
}

object RecordConverter {

  private var headerMap = Map[String, SAMFileHeader]()

  def getHeader(contigName: String, alignmentRDD: RDD[AlignmentRecord]): Broadcast[SAMFileHeaderWritable] = {
    val header = headerMap.get(contigName) match {
      case Some(h) => h
      case None =>
        val sd = alignmentRDD.adamGetSequenceDictionary()
        val rgd = alignmentRDD.adamGetReadGroupDictionary()
        val converter = new AlignmentRecordConverter
        val h = converter.createSAMHeader(sd, rgd)
        headerMap += (contigName -> h)
        headerMap(contigName)
    }

    alignmentRDD.context.broadcast(SAMFileHeaderWritable(header))
  }

  def alignmentRecToJbFormat(record: AlignmentRecord, header: Broadcast[SAMFileHeaderWritable], converter: AlignmentRecordConverter) = {
    val samRecord = converter.convert(record, header.value)
    var jbRecord = Map(
      "name" -> record.getReadName,
      "seq" -> record.getSequence,
      "start" -> record.getStart.toString,
      "end" -> record.getEnd.toString,
      "cigar" -> record.getCigar,
      "map_qual" -> record.getMapq.toString,
      "mate_start" -> record.getMateAlignmentStart.toString,
      "uniqueID" -> (record.getReadName + "_" + record.getStart),
      "qual" -> record.getQual.map(_ - 33).mkString(" ").toString,
      "flag" -> samRecord.getFlags.toString,
      "insert_size" -> samRecord.getInferredInsertSize.toString,
      "ref_index" -> samRecord.getReferenceIndex.toString,
      "mate_ref_index" -> samRecord.getMateReferenceIndex.toString,
      "as" -> samRecord.getAttributesBinarySize.toString,
      "strand" -> (if (samRecord.getReadNegativeStrandFlag) 1 else -1).toString
    )
    if (!record.getRecordGroupName.isEmpty)
      jbRecord += ("rg" -> record.getRecordGroupName)
    jbRecord
  }

  def referenceRecToJbFormat(referenceRecord: NucleotideContigFragment) = {
    Map(
      "seq" -> referenceRecord.getFragmentSequence,
      "flag" -> referenceRecord.getFragmentNumber.toString,
      "start" -> referenceRecord.getFragmentStartPosition.toString,
      "seq_id" -> referenceRecord.getContig.getContigName
    )
  }

  def variantRecToJbFormat(variantRecord: Variant) = {
    Map(
      "start" -> variantRecord.getStart,
      "end" -> variantRecord.getEnd,
      "seq_id" -> variantRecord.getContig.getContigName,
      "reference_allele" -> variantRecord.getReferenceAllele,
      "alternative_alleles" -> Map(
        "values" -> List(variantRecord.getAlternateAllele),
        "meta" -> Map(
          "description" -> "VCF ALT field, list of alternate non-reference alleles called on at least one of the samples"
        )
      ),
      "debug_to_string" -> variantRecord.toString
    )
  }
}