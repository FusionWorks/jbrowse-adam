package md.fusionworks.adam.jbrowse.models

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.SAMFileHeaderWritable
import org.bdgenomics.adam.rdd.ADAMContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.formats.avro.AlignmentRecord
import parquet.org.codehaus.jackson.map.ObjectMapper
import spray.json.DefaultJsonProtocol





object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat6(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featureFormat = jsonFormat16(Feature)
  implicit val featuresFormat = jsonFormat1(Features)
}

object JbrowseUtil {
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqc = new SQLContext(sc)
  val ac = new ADAMContext(sc)
  val adamPath = ConfigFactory.load().getString("adam.path")
  val readAC = ac.loadAlignments(adamPath)
  val df = sqc.read.parquet(adamPath)
  val tableSQL = sqc.read.parquet(adamPath)
  tableSQL.registerTempTable("ADaM_Table")
  val end = sqc.sql("SELECT MAX(`end`) FROM ADaM_Table").collect().apply(0).getLong(0)
  val start = sqc.sql("SELECT MIN(start) FROM ADaM_Table").collect().apply(0).getLong(0)
  val mapper = new ObjectMapper()

  def getTrackList = {
    TrackList(tracks = List(
        Track(
        "mygene_track",
        "My ADAM Genes",
        "JBrowse/View/Track/HTMLFeatures",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data",
        List(Query("tyrannosaurus", Some("gene")))),
      Track(
        "my_sequence_track",
        "DNA",
        "JBrowse/View/Track/Sequence",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data",
        List(Query("tyrannosaurus", sequence = Some(true))))))
  }

  def getRefSeqs = {
    List(RefSeqs(end, "ctgA", "seq/ctgA", 20000, end, start), RefSeqs(66, "ctgB", "seq/ctgB", 20000, 66, 0))
  }

  def getGlobal = {
    Global(0.02, 234235, 87, 87, 42, 2.1)
  }


  def getFeatures(getFactStat: Long, getFactEnd: Long) = {

    val dataFrame=df.filter(df("start")>=getFactStat && df("start")!=null).filter(df("start")<=getFactEnd && df("start")!=null).orderBy("start")
    val rddAR = dataFrame.toJSON.map(str => mapper.readValue(str,classOf[AlignmentRecord]))

    val sd = rddAR.adamGetSequenceDictionary
    val rgd = rddAR.adamGetReadGroupDictionary
    val adamRecordConverter = new AlignmentRecordConverter
    val header = adamRecordConverter.createSAMHeader(sd, rgd)
    val hdrBcast = rddAR.context.broadcast(SAMFileHeaderWritable(header))

    val dataFeature=rddAR.map(x=> {
      val samRecord = adamRecordConverter.convert(x, hdrBcast.value)
      Feature(x.getReadName,x.getSequence,x.getStart,x.getEnd,x.getCigar,x.getMapq,x.getMateAlignmentStart,x.getReadName+"_"+x.getStart,x.getQual.map(_-33).mkString(" "),samRecord.getFlags,samRecord.getInferredInsertSize,samRecord.getReferenceIndex,samRecord.getMateReferenceName,samRecord.getMateReferenceIndex,samRecord.getAttributesBinarySize,if(samRecord.getReadNegativeStrandFlag) 1 else -1)
    }).collect().toList

    Features(features = dataFeature)


  }

}

case class TrackList(
                      tracks: List[Track]
                      )

case class Track(
                  label: String,
                  key: String,
                  `type`: String,
                  storeClass: String,
                  baseUrl: String,
                  query: List[Query]
                  )

case class Query(
                  organism: String,
                  soType: Option[String] = None,
                  sequence: Option[Boolean] = None
                  )


case class RefSeqs(
                    length: Long,
                    name: String,
                    seqDi: String,
                    seqChunkSize: Int,
                    end: Long,
                    start: Long
                    )


case class Global(
                   featureDensity: Double,
                   featureCount: Int,
                   scoreMin: Int,
                   scoreMax: Int,
                   scoreMean: Int,
                   scoreStdDev: Double
                   )


case class Features(
                     features: List[Feature]
                     )


case class Feature(
                    name: String,
                    seq: String,
                    start: Long,
                    end: Long,
                    cigar: String,
                    map_qual: Int,
                    mate_start: Long,
                    uniqueID: String,
                    qual: String,
                    flag: Int,
                    insert_size: Long,
                    ref_index: Int,
                    mate_ref: String,
                    mate_ref_index: Int,
                    as: Int,
                    strand: Int
                    )

