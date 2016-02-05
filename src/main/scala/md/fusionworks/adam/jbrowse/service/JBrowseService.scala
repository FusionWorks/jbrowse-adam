package md.fusionworks.adam.jbrowse.service

import htsjdk.samtools.SAMFileHeader
import md.fusionworks.adam.jbrowse.config.ConfigLoader
import md.fusionworks.adam.jbrowse.model._
import md.fusionworks.adam.jbrowse.spark.SparkContextFactory
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel
import org.bdgenomics.adam.converters.AlignmentRecordConverter
import org.bdgenomics.adam.models.SAMFileHeaderWritable
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.formats.avro.{AlignmentRecord, NucleotideContigFragment}
import parquet.org.codehaus.jackson.map.ObjectMapper

object JBrowseService {

  private var headerMap = Map[String, SAMFileHeader]()
  val sc = SparkContextFactory.getSparkContext
  val sqlContext = SparkContextFactory.getSparkSqlContext

  val tracksConfig = ConfigLoader.getTracksConfig
  val paths = tracksConfig.map(_.filePath)

  lazy val alignmentDF = sqlContext.read.parquet(paths.head.toString).persist(StorageLevel.MEMORY_AND_DISK)
  lazy val referenceDF = sqlContext.read.parquet(paths(1).toString).persist(StorageLevel.MEMORY_AND_DISK)


  def getTrackList: TrackList = {
    def getFileName(path: String) = path.substring(path.lastIndexOf("/") + 1)

    val tracks = tracksConfig.map(trackConfig => {
      val fileName = getFileName(trackConfig.filePath)
      Track(
        `type` = trackConfig.trackType,
        storeClass = "JBrowse/Store/SeqFeature/REST",
        baseUrl = ConfigLoader.getBaseUrl,
        label = s"${fileName}_${trackConfig.fileType}",
        key = s"$fileName ${trackConfig.fileType}"
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

    val alignmentRecordsRDD =
      filteredDataFrame.toJSON.mapPartitions(partition => {
        val mapper = new ObjectMapper()
        partition.map(str => mapper.readValue(str, classOf[AlignmentRecord]))
      })
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

    val alignmentRecordsRDD =
      filteredDataFrame.toJSON.mapPartitions(partition => {
        val mapper = new ObjectMapper()
        partition.map(str => mapper.readValue(str, classOf[NucleotideContigFragment]))
      })

    val featuresMap = alignmentRecordsRDD.map(x => {
      Map(
        "seq" -> x.getFragmentSequence,
        "flag" -> x.getFragmentNumber.toString,
        "start" -> x.getFragmentStartPosition.toString,
        "seq_id" -> x.getContig.getContigName
      )
    }).collect().toList.sortBy(x => x("start"))
    Features(features = featuresMap)
  }

}

