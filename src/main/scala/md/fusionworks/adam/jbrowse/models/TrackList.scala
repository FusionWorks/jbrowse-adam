package md.fusionworks.adam.jbrowse.models

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.rdd.ADAMContext
import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat6(RefSeqs)
  implicit val tracksConfFormat = jsonFormat5(TracksConf)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val contigFormet = jsonFormat6(Contig)
  implicit val featureFormat = jsonFormat3(Feature)
  implicit val featuresFormat = jsonFormat1(Features)
}

object JbrowseUtil {
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqc = new SQLContext(sc)
  val ac = new ADAMContext(sc)
  val reads = ac.loadAlignments("adamtest.adam")
  val test = sqc.read.parquet("adamtest.adam")
  test.registerTempTable("UserTable")
  val end = reads.map(_.getEnd).filter(_ != null).max
  val start = 0


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

  def getTracksConf = {
    TracksConf(
      "JBrowse/Store/SeqFeature/BigWig",
      "../../my-bigwig-file.bw",
      "Quantitative",
      "JBrowse/View/Track/Wiggle/XYPlot",
      "Coverage plot of NGS alignments from XYZ"
    )
  }

  def getGlobal = {
    Global(0.02, 234235, 87, 87, 42, 2.1)
  }

  def getFeatures(getFactStat: Long, getFactEnd: Long) = {

    val regist = sqc.sql("SELECT sequence, start, `end` FROM UserTable WHERE start >= \""+getFactStat+"\" AND `end` <= \""+getFactEnd+"\"")
    val sampledata= regist.map(x => Feature(x.getString(0),x.getLong(1),x.getLong(2))).collect().toList
    Features(features = sampledata)

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


case class TracksConf(
                       storeClass: String,
                       urlTemplate: String,
                       category: String,
                       `type`: String,
                       key: String
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

case class Contig(
                 contigName: String,
                 contigLength: Long,
                 contigMD5: String,
                 referenceURL: String,
                 assembly: String,
                 species: String
                   )


case class Feature(
                  seq: String,
                  start: Long,
                  end: Long
                    )

