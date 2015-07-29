package md.fusionworks.adam.jbrowse.models

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.SAMFileHeaderWritable
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.formats.avro.AlignmentRecord
import parquet.org.codehaus.jackson.map.ObjectMapper
import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val trackFormat = jsonFormat5(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat6(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featureFormat = jsonFormat16(Feature)
  implicit val featuresFormat = jsonFormat1(Features)
}

object JbrowseUtil {
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  val adamPath = ConfigFactory.load().getString("adam.path")

  val dataFrame = sqlContext.read.parquet(adamPath)
  val mapper = new ObjectMapper()


  def getTrackList = {
    TrackList(tracks = List(
      Track(
        "mygene_track",
        "My ADAM Genes",
        "JBrowse/View/Track/Alignments2",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data"
      ),
      Track(
        "my_sequence_track",
        "DNA",
        "JBrowse/View/Track/Sequence",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data"
      )))
  }

  def getRefSeqs = {
    val end = dataFrame.select(dataFrame("start")).agg(max(dataFrame("start"))).first().getLong(0)
    val start = dataFrame.select(dataFrame("start")).agg(min(dataFrame("start"))).first().getLong(0)

    List(RefSeqs(end, "ctgA", "seq/ctgA", 20000, end, start), RefSeqs(66, "ctgB", "seq/ctgB", 20000, 66, 0))
  }

  def getGlobal = {
    Global(0.02, 234235, 87, 87, 42, 2.1)
  }


  def getFeatures(start: Long, end: Long) = {

    val filteredDataFrame = dataFrame.filter(
      (dataFrame("start") >= start && dataFrame("start") != null &&
        dataFrame("start") <= end && dataFrame("start") != null) ||
        (dataFrame("end") >= start && dataFrame("end") != null &&
          dataFrame("end") <= end && dataFrame("end") != null)

    )

    val alignmentRecordsRDD = filteredDataFrame.toJSON.map(str => mapper.readValue(str, classOf[AlignmentRecord]))

    val sd = alignmentRecordsRDD.adamGetSequenceDictionary()
    val rgd = alignmentRecordsRDD.adamGetReadGroupDictionary()

    val converter = new AlignmentRecordConverter
    val header = converter.createSAMHeader(sd, rgd)
    val hdrBcast = alignmentRecordsRDD.context.broadcast(SAMFileHeaderWritable(header))

    val featureList = alignmentRecordsRDD.map(x => {
      val samRecord = converter.convert(x, hdrBcast.value)
      Feature(
        name = x.getReadName,
        seq = x.getSequence,
        start = x.getStart,
        end = x.getEnd,
        cigar = x.getCigar,
        map_qual = x.getMapq,
        mate_start = x.getMateAlignmentStart,
        uniqueID = x.getReadName + "_" + x.getStart,
        qual = x.getQual.map(_ - 33).mkString(" "),
        flag = samRecord.getFlags,
        insert_size = samRecord.getInferredInsertSize,
        ref_index = samRecord.getReferenceIndex,
        mate_ref = samRecord.getMateReferenceName,
        mate_ref_index = samRecord.getMateReferenceIndex,
        as = samRecord.getAttributesBinarySize,
        strand = if (samRecord.getReadNegativeStrandFlag) 1 else -1)
    }).collect().sortBy(_.start).toList

    Features(features = featureList)

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
                  baseUrl: String
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

