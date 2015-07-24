package md.fusionworks.adam.jbrowse.models

import com.typesafe.config.ConfigFactory
import htsjdk.samtools.{SAMFileHeader, SAMRecord}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.rdd.ADAMContext
import org.bdgenomics.adam.rdd.read.AlignmentRecordRDDFunctions
import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat6(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featureFormat = jsonFormat10(Feature)
  implicit val featuresFormat = jsonFormat1(Features)
}

object JbrowseUtil {
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val sqc = new SQLContext(sc)
  val ac = new ADAMContext(sc)
  val readAC = ac.loadAlignments("adamtest.adam")
  val adamPath = ConfigFactory.load().getString("adam.path")
  val tableSQL = sqc.read.parquet(adamPath)
  val df = sqc.read.parquet(adamPath)
  tableSQL.registerTempTable("ADaM_Table")
  val end = sqc.sql("SELECT MAX(`end`) FROM ADaM_Table").collect().apply(0).getLong(0)
  val start = sqc.sql("SELECT MIN(start) FROM ADaM_Table").collect().apply(0).getLong(0)


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

    val filt = readAC.filter(x=>(x.getStart!=null && x.getStart>=getFactStat && x.getStart<=getFactEnd))
    val al=new AlignmentRecordRDDFunctions(filt)
    val flags = al.adamConvertToSAM()._1.map(x=> ( x.get().getFlags)).collect.toList
    val itfl = flags

    val dfreg=df.select(
      df("readName"),df("sequence"),df("start"),df("`end`"),df("cigar"),df("mapq"),df("mateAlignmentStart"),df("qual")
    ).filter(df("start")>=getFactStat && df("start")!=null).filter(df("start")<=getFactEnd && df("start")!=null).orderBy("start")
    val sampledf=dfreg.collect.map(x=>Feature(x.getString(0),x.getString(1),x.getLong(2),x.getLong(3),x.getString(4),x.getInt(5),x.getLong(6),x.getString(0)+"_"+x.getLong(2),x.getString(7).map(_-33).mkString(" "),itfl )).toList
    Features(features = sampledf)



/*
        val regist = sqc.sql(s"SELECT readName, sequence, start, `end`, cigar, mapq, mateAlignmentStart, qual FROM ADaM_Table WHERE start >= $getFactStat AND start <= $getFactEnd ORDER BY start ASC ")

        val sampledata= regist.collect().map(x => Feature(x.getString(0),x.getString(1),x.getLong(2),x.getLong(3),x.getString(4),x.getInt(5),x.getLong(6),x.getString(0)+"_"+x.getLong(2),x.getString(7).map(_-33).mkString(" "))).toList
        Features(features = sampledata)
*/


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
                    flag: List[Int]
                    )

