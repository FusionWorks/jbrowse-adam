package md.fusionworks.adam.jbrowse.models

import htsjdk.samtools.SAMFileHeader
import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.SAMFileHeaderWritable
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.formats.avro.{AlignmentRecord, NucleotideContigFragment}
import parquet.org.codehaus.jackson.map.ObjectMapper
import spray.json.{DefaultJsonProtocol, _}

object JsonProtocol extends DefaultJsonProtocol {

  implicit object TrackTypeFormat extends RootJsonFormat[FileType.TrackType] {
    def write(obj: FileType.TrackType): JsValue = JsString(obj.toString)

    def read(json: JsValue): FileType.TrackType = json match {
      case JsString(str) => FileType.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit val tracksConfigFormat = jsonFormat3(TrackConfig)
  implicit val trackFormat = jsonFormat5(Track)
  implicit val trackListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat3(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featuresFormat = jsonFormat1(Features)
}

object FileType extends Enumeration {
  type TrackType = Value
  val Alignment, Reference, Variants = Value
}

import FileType._

case class TrackConfig(filePath: String, fileType: TrackType, trackType: String)

object JBrowseUtil {

  import JsonProtocol._

  private var headerMap = Map[String, SAMFileHeader]()
  val sc = SparkContextFactory.getSparkContext
  val sqlContext = SparkContextFactory.getSparkSqlContext

  val tracksConfig = scala.io.Source.fromFile("tracksConfig.json").mkString.parseJson.convertTo[List[TrackConfig]]
  val paths = tracksConfig.map(_.filePath)

  val alignmentDF = sqlContext.read.parquet(paths(0).toString)
  val referenceDF = sqlContext.read.parquet(paths(1).toString)

  val mapper = new ObjectMapper()

  def getTrackList: TrackList = {
    val tracks = tracksConfig.map(trackConfig => {
      val fileName = trackConfig.filePath.substring(trackConfig.filePath.lastIndexOf("/") + 1)
      Track(
        `type` = trackConfig.trackType,
        storeClass = "JBrowse/Store/SeqFeature/REST",
        baseUrl = s"http://localhost:8080/data",
        label = s"${fileName}_${trackConfig.fileType.toString}",
        key = s"$fileName ${trackConfig.fileType.toString}"
      )
    })
    TrackList(tracks = tracks)
  }

  def getRefSeqs: List[RefSeqs] = {
    val filteredDataFrame = referenceDF.filter(referenceDF("fragmentStartPosition") >= 0 && referenceDF("fragmentStartPosition") != null)
    val colectDataFrame = filteredDataFrame.select("contig", "fragmentStartPosition")
      .groupBy("contig.contigName").agg(min("fragmentStartPosition"), max("fragmentStartPosition")) // todo: compute correct end
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

    val alignmentRecordsRDD = filteredDataFrame.toJSON.map(str => mapper.readValue(str, classOf[AlignmentRecord]))
    val converter = new AlignmentRecordConverter


    val header = headerMap.get(contigName) match {
      case Some(h) => h
      case None =>
        val sd = alignmentRecordsRDD.adamGetSequenceDictionary()
        val rgd = alignmentRecordsRDD.adamGetReadGroupDictionary()

        val h = converter.createSAMHeader(sd, rgd)
        headerMap += (contigName -> h)
        headerMap(contigName)
    }

    val hdrBcast = alignmentRecordsRDD.context.broadcast(SAMFileHeaderWritable(header))

    val featuresMap = alignmentRecordsRDD.map(x => {
      val samRecord = converter.convert(x, hdrBcast.value)
      var maps = Map(
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

    val position = referenceDF.first()

    val filteredDataFrame = referenceDF.filter(referenceDF("contig.contigName") === contigName)
      .filter(
        referenceDF("fragmentStartPosition") < end && referenceDF("fragmentStartPosition") != null &&
          referenceDF("fragmentStartPosition") + position.getAs[Row]("contig").getAs[Long]("contigLength") / position.getAs[Integer]("numberOfFragmentsInContig")
            > start && referenceDF("fragmentStartPosition") != null
      )

    val alignmentRecordsRDD = filteredDataFrame.toJSON.map(str => mapper.readValue(str, classOf[NucleotideContigFragment]))

    val featuresMap = alignmentRecordsRDD.map(x => {
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

case class TrackList(tracks: List[Track])

case class Track(
                  label: String,
                  key: String,
                  `type`: String,
                  storeClass: String,
                  baseUrl: String
                  )

case class RefSeqs(
                    name: String,
                    start: Long,
                    end: Long
                    )

case class Global(
                   featureDensity: Double,
                   featureCount: Int,
                   scoreMin: Int,
                   scoreMax: Int,
                   scoreMean: Int,
                   scoreStdDev: Double
                   )

case class Features(features: List[Map[String, String]])