package md.fusionworks.adam.jbrowse.models

import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat=jsonFormat3(RefSeqs)
  implicit val tracksConfFormat = jsonFormat5(TracksConf)
  implicit val globalFormat = jsonFormat6(Global)
}

object JbrowseUtil {
  def getTrackList = {
    TrackList(tracks = List(Track("mygene_track",
    "Genes",
    "JBrowse/View/Track/HTMLFeatures",
    "JBrowse/Store/SeqFeature/REST",
    "http://my.site.com/rest/api/base",
    List(Query("tyrannosaurus", Some("gene")))
  ), Track("my_sequence_track",
    "DNA",
    "JBrowse/View/Track/Sequence",
    "JBrowse/Store/SeqFeature/REST",
    "http://my.site.com/rest/api/base",
    List(Query("tyrannosaurus", sequence = Some(true))))))
    }
  def getRefSeqs={
    RefSeqs("chr1",0,12345678)
  }
  def getTracksConf={
    TracksConf(
      "JBrowse/Store/SeqFeature/BigWig",
      "../../my-bigwig-file.bw",
      "Quantitative",
      "JBrowse/View/Track/Wiggle/XYPlot",
      "Coverage plot of NGS alignments from XYZ"
    )
  }
  def getGlobal={
    Global(0.02,234235,87,87,42,2.1)
  }

  def getFeatures={
    Features(List(Feature(123,456),
                  Feature2(123,456,42),
                  Feature3("seq",0,14,42),
                  Feature4(
                    "mRNA",
                    "gattacagattaca",
                    5975,
                    9744,
                    0.84,
                    1,
                    "au9.g1002.t1",
                    "globallyUniqueString3",
                    List(
                      SubFeature(
                        "five_prime_UTR",
                        5975,
                        6109,
                        Some(0.98),
                        1,
                        None
                      ),
                      SubFeature(
                      "start_codon",
                        6110,
                        6112,
                        None,
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        6110,
                        6148,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        6615,
                        6683,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        6758,
                        7040,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        7142,
                        7319,
                        Some(1),
                        1,
                        Some(2)
                      ),
                      SubFeature(
                        "CDS",
                        7411,
                        7686,
                        Some(1),
                        1,
                        Some(1)
                      ),
                      SubFeature(
                        "CDS",
                        7748,
                        7850,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        7953,
                        8098,
                        Some(1),
                        1,
                        Some(2)
                      ),
                      SubFeature(
                        "CDS",
                        8166,
                        8320,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        8419,
                        8614,
                        Some(1),
                        1,
                        Some(1)
                      ),
                      SubFeature(
                        "CDS",
                        8708,
                        8811,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "CDS",
                        8927,
                        9273,
                        Some(1),
                        1,
                        Some(1)
                      ),
                      SubFeature(
                        "CDS",
                        9414,
                        9494,
                        Some(1),
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "stop_codon",
                        9492,
                        9494,
                        None,
                        1,
                        Some(0)
                      ),
                      SubFeature(
                        "three_prime_UTR",
                        9495,
                        9744,
                        Some(0.86),
                        1,
                        None
                      )
                    )
                  )
    )
    )
  }
}

case class TrackList(tracks: List[Track]
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
                  name:String,
                  start:Int,
                  end:Int
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


case class Feature(
                    start:Int,
                    end: Int
                    )

case class Feature2(
                     override val start: Int,
                     override val end: Int,
                     score: Double
                     ) extends Feature(start,end)



case class Feature3(
                    seq: String,
                    override val start: Int,
                    override val end: Int,
                    override val score: Double
                      ) extends Feature2(start,end,score)

case class Feature4(
                   `type`: String,
                   override val seq: String,
                   override val start: Int,
                   override val end: Int,
                   override val score: Double,
                   strand: Int,
                   name: String,
                   uniqueID: String,
                   subfeatures:List[SubFeature]
                     ) extends Feature3(seq,start,end,score)

case class SubFeature(
                      `type`: String,
                      start: Int,
                      end: Int,
                      score: Option[Double]=None,
                      strand: Int,
                      phase: Option[Int]=None
                        )
