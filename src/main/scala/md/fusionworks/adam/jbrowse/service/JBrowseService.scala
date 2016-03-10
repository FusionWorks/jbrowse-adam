package md.fusionworks.adam.jbrowse.service

import md.fusionworks.adam.jbrowse.config.{TrackType, ConfigLoader}
import md.fusionworks.adam.jbrowse.config.TrackType.TrackType
import md.fusionworks.adam.jbrowse.model._
import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import md.fusionworks.adam.jbrowse.core.Implicits._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.LongType
import org.apache.spark.storage.StorageLevel

object JBrowseService {

  val sc = SparkContextFactory.getSparkContext()
  val sqlContext = SparkContextFactory.getSparkSqlContext

  val tracksConfig = ConfigLoader.getTracksConfig
  val paths = tracksConfig.map(_.filePath)

  case class DataFrameEntry(trackType: TrackType, dataFrame: DataFrame)

  lazy val DFList: Map[String, DataFrameEntry] = tracksConfig.map( track =>
    track.id -> DataFrameEntry(track.fileType, sqlContext.read.parquet(track.filePath).persist(StorageLevel.MEMORY_AND_DISK))
  ).toMap


  def getTrackList(baseUrl: String): TrackList = {
    def getFileName(path: String) = path.substring(path.lastIndexOf("/") + 1)

    val tracks = tracksConfig.map(trackConfig => {
      val fileName = getFileName(trackConfig.filePath)
      Track(
        `type` = trackConfig.trackType,
        storeClass = "JBrowse/Store/SeqFeature/REST",
        baseUrl = s"$baseUrl/${trackConfig.id}",
        label = s"${fileName}_${trackConfig.fileType}",
        key = s"$fileName ${trackConfig.fileType}"
      )
    })
    TrackList(tracks = tracks)
  }

  def getRefSeqs: List[RefSeqs] = {
    val referenceDF = DFList.find(_._2.trackType == TrackType.Reference).head._2.dataFrame
    val filteredDF = referenceDF.filter(referenceDF("fragmentStartPosition") >= 0 && referenceDF("fragmentStartPosition") != null)

    val filteredDFCount = filteredDF.count()
    val count = if (filteredDFCount != 0) filteredDFCount else 1

    val selectedDF = filteredDF.select("contig", "fragmentStartPosition")
      .withColumn("fragmentEndPosition",
        filteredDF("fragmentStartPosition") + (filteredDF("contig.contigLength") / count).cast(LongType))

    selectedDF.groupBy("contig.contigName")
      .agg(min("fragmentStartPosition"), max("fragmentEndPosition"))
      .orderBy("contigName")
      .collect()
      .toList
      .map(x => RefSeqs(x.getString(0), x.getLong(1), x.getLong(2)))
  }

  def getGlobal: Global = Global(0.02, 234235, 87, 87, 42, 2.1)

  def getFeatures(start: Long, end: Long, contigName: String, trackId: String): Features = {
    val dataFrameEntry = DFList(trackId)
    dataFrameEntry.trackType match {
      case TrackType.Alignment => getAlignmentFeatures(start, end, contigName, dataFrameEntry.dataFrame)
      case TrackType.Reference => getReferenceFeatures(start, end, contigName, dataFrameEntry.dataFrame)
      case TrackType.Variants => getVariantFeatures(start, end, contigName, dataFrameEntry.dataFrame)
    }
  }

  def getAlignmentFeatures(start: Long, end: Long, contigName: String, dataFrame: DataFrame): Features = {
    val features = dataFrame.filterAlignmentDF(start, end, contigName)
      .alignmentDfToRDD
      .toJBrowseFormat(contigName)
      .collect()
      .toList
      .sortBy(x => x("start"))

    Features(features)
  }


  def getVariantFeatures(start: Long, end: Long, contigName: String,  dataFrame: DataFrame): Features = {
    val features = dataFrame.filterAlignmentDF(start, end, contigName)
      .variantsDfToRDD
      .toJBrowseFormat
      .collect()
      .toList
      .sortBy(x => x("start").asInstanceOf[Long])

    Features(features)
  }


  def getReferenceFeatures(start: Long, end: Long, contigName: String, dataFrame: DataFrame): Features = {
    val features = dataFrame.filterReferenceDF(start, end, contigName).referenceDfToRDD.toJBrowseFormat
      .collect().toList.sortBy(x => x("start"))
    Features(features)
  }
}


