package md.fusionworks.adam.jbrowse.models

import com.typesafe.config.ConfigFactory
import htsjdk.samtools.{SAMRecord, SAMFileHeader}
import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext, Row}
import org.apache.spark.sql.functions._
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.{RecordGroupDictionary, SequenceDictionary, SAMFileHeaderWritable}
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.formats.avro.{AlignmentRecord, NucleotideContigFragment}
import parquet.org.codehaus.jackson.map.ObjectMapper
import spray.json._

import ConfigLoader._

object ConfigLoader {
  final val jBrowseConf = ConfigFactory.load().getConfig("jbrowse")

  final val storeClass = jBrowseConf.getString("track.store.class")
  final val baseUrl = jBrowseConf.getString("track.base.url")

  final val alignmentFilePath = jBrowseConf.getString("alignment.file.path")
  final val AlignmentFileType = "Alignment"
  final val AlignmentTrackType = "JBrowse/View/Track/Alignments2"

  final val referenceFilePath = jBrowseConf.getString("reference.file.path")
  final val ReferenceFileType = "Reference"
  final val ReferenceTrackType = "JBrowse/View/Track/Sequence"

  final val alignmentTrack = TrackConfig(alignmentFilePath, AlignmentFileType, AlignmentTrackType)
  final val referenceTrack = TrackConfig(referenceFilePath, ReferenceFileType, ReferenceTrackType)
}

object JBrowseUtil {
  private val FragmentStartPosition = "fragmentStartPosition"

  private var headerMap = Map[String, SAMFileHeader]()
  val sc: SparkContext = SparkContextFactory.getSparkContext
  val sqlContext: SQLContext = SparkContextFactory.getSparkSqlContext

  val alignmentDF: DataFrame = sqlContext.read.parquet(alignmentFilePath)
  val referenceDF: DataFrame = sqlContext.read.parquet(referenceFilePath)

  val mapper = new ObjectMapper()

  def getTrackList: TrackList = {
    TrackList(tracks = TrackBuilder.from(alignmentTrack) :: TrackBuilder.from(referenceTrack) :: Nil)
  }

  def getRefSeqs: List[RefSeqs] = {
    val filteredDataFrame = referenceDF.filter(referenceDF(FragmentStartPosition) >= 0 && referenceDF(FragmentStartPosition) != null)
    val colectDataFrame = filteredDataFrame.select("contig", FragmentStartPosition)
      .groupBy("contig.contigName").agg(min(FragmentStartPosition), max(FragmentStartPosition)) // todo: compute correct end
      .orderBy("contigName").collect().toList
    colectDataFrame.map(x =>
      RefSeqs(
        name = x.getString(0),
        start = x.getLong(1),
        end = x.getLong(2)
      ))
  }

  def getGlobal: Global = Global(0.02, 234235, 87, 87, 42, 2.1)

  def getAlignmentFeatures(start: Long, end: Long, contigName: String): Features = {

    val filteredDataFrame = alignmentDF.filter(alignmentDF("contig.contigName") === contigName)
      .filter(
        alignmentDF("start") < end && alignmentDF("start") != null &&
          alignmentDF("end") > start && alignmentDF("end") != null
      )

    val alignmentRecordsRDD: RDD[AlignmentRecord] = filteredDataFrame.toJSON.map(str => mapper.readValue(str, classOf[AlignmentRecord]))
    val converter = new AlignmentRecordConverter


    val header: SAMFileHeader = headerMap.get(contigName) match {
      case Some(h) => h
      case None =>
        val sd: SequenceDictionary = alignmentRecordsRDD.adamGetSequenceDictionary()
        val rgd: RecordGroupDictionary = alignmentRecordsRDD.adamGetReadGroupDictionary()

        val h: SAMFileHeader = converter.createSAMHeader(sd, rgd)
        headerMap += (contigName -> h)
        headerMap(contigName)
    }

    val hdrBcast: Broadcast[SAMFileHeaderWritable] = alignmentRecordsRDD.context.broadcast(SAMFileHeaderWritable(header))

    val featuresMap: List[Map[String, String]] = alignmentRecordsRDD.map(x => {
      val samRecord: SAMRecord = converter.convert(x, hdrBcast.value)
      var maps: Map[String, String] = Map(
        "name" -> x.getReadName,
        "seq" -> x.getSequence,
        "start" -> x.getStart.toString,
        "end" -> x.getEnd.toString,
        "cigar" -> x.getCigar,
        "map_qual" -> x.getMapq.toString,
        "mate_start" -> x.getMateAlignmentStart.toString,
        "uniqueID" -> (x.getReadName + "_" + x.getStart),
        "qual" -> x.getQual.map(_ - 33).mkString(" ").toString,
        "flag" -> samRecord.getFlags.toString,
        "insert_size" -> samRecord.getInferredInsertSize.toString,
        "ref_index" -> samRecord.getReferenceIndex.toString,
        "mate_ref_index" -> samRecord.getMateReferenceIndex.toString,
        "as" -> samRecord.getAttributesBinarySize.toString,
        "strand" -> (if (samRecord.getReadNegativeStrandFlag) 1 else -1).toString
      )
      if (!x.getRecordGroupName.isEmpty)
        maps += ("rg" -> x.getRecordGroupName)
      maps
    }).collect().toList.sortBy(x => x("start"))

    Features(features = featuresMap)
  }


  def getReferenceFeatures(start: Long, end: Long, contigName: String): Features = {

    val position: Row = referenceDF.first()

    val filteredDataFrame: DataFrame = referenceDF.filter(referenceDF("contig.contigName") === contigName)
      .filter(
        referenceDF(FragmentStartPosition) < end && referenceDF(FragmentStartPosition) != null &&
          referenceDF(FragmentStartPosition) + position.getAs[Row]("contig").getAs[Long]("contigLength") / position.getAs[Integer]("numberOfFragmentsInContig")
            > start && referenceDF(FragmentStartPosition) != null
      )

    val alignmentRecordsRDD: RDD[NucleotideContigFragment] = filteredDataFrame.toJSON.map(str => mapper.readValue(str, classOf[NucleotideContigFragment]))

    val featuresMap: List[Map[String, String]] = alignmentRecordsRDD.map(x => {
      Map(
        //"description" ->x.getDescription,
        "seq" -> x.getFragmentSequence,
        "flag" -> x.getFragmentNumber.toString,
        "start" -> x.getFragmentStartPosition.toString,
        "seq_id" -> x.getContig.getContigName
      )
    }).collect().toList.sortBy(x => x("start"))
    Features(features = featuresMap)
  }

}


object TrackBuilder {
  def from(trackConfig: TrackConfig) = {
    val fileName = trackConfig.filePath.substring(trackConfig.filePath.lastIndexOf("/") + 1)
    Track(
      `type` = trackConfig.trackType,
      storeClass = ConfigLoader.storeClass,
      baseUrl = ConfigLoader.baseUrl,
      label = s"${fileName}_${trackConfig.fileType}",
      key = s"$fileName ${trackConfig.fileType}"
    )
  }
}


case class Track(label: String, key: String, `type`: String, storeClass: String, baseUrl: String)

case class TrackList(tracks: List[Track])

case class TrackConfig(filePath: String, fileType: String, trackType: String)

case class RefSeqs(name: String, start: Long, end: Long)

case class Global(
                   featureDensity: Double,
                   featureCount: Int,
                   scoreMin: Int,
                   scoreMax: Int,
                   scoreMean: Int,
                   scoreStdDev: Double
                   )

case class Features(features: List[Map[String, String]])

object JsonProtocol extends DefaultJsonProtocol {
  implicit val tracksConfigFormat = jsonFormat3(TrackConfig)
  implicit val trackFormat = jsonFormat5(Track)
  implicit val trackListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat3(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featuresFormat = jsonFormat1(Features)
}