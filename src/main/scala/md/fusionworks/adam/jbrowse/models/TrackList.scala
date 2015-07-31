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
  implicit val refSeqsFormat = jsonFormat3(RefSeqs)
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


  def getTrackList: TrackList = {
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

  def getRefSeqs:List[RefSeqs] = {
   val filteredDataFrame = dataFrame.filter(dataFrame("start") >= 0 && dataFrame("start") != null)
    val colectDataFrame = filteredDataFrame.select("contig","start").groupBy("contig.contigName").agg(max("start"),min("start")).orderBy("contigName").collect().toList


   val data = colectDataFrame.map(x=>
        RefSeqs(
        name = x.getString(0),
        end = x.getLong(1),
        start = x.getLong(2)
        ))

   data

  }

  def getGlobal: Global = {
    Global(0.02, 234235, 87, 87, 42, 2.1)
  }


  def getFeatures(start: Long, end: Long, contigName: String): Features = {

    val filteredDataFrame = dataFrame.filter((
      dataFrame("start") >= start && dataFrame("start") != null &&
        dataFrame("start") <= end && dataFrame("start") != null ||
        (dataFrame("end") >= start && dataFrame("end") != null &&
          dataFrame("end") <= end && dataFrame("end") != null)) &&
      dataFrame("contig.contigName") === contigName
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
                    name: String,
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

