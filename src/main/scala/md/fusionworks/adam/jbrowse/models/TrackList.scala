package md.fusionworks.adam.jbrowse.models

import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat6(RefSeqs)
  implicit val tracksConfFormat = jsonFormat5(TracksConf)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val subFeaturesFormat = jsonFormat6(SubFeatures)
  implicit val featureFormat = jsonFormat9(Feature)
  implicit val featuresFormat = jsonFormat1(Features)
}

object JbrowseUtil {
  def getTrackList = {
    TrackList(tracks = List(Track("mygene_track",
      "Genes",
      "JBrowse/View/Track/HTMLFeatures",
      "JBrowse/Store/SeqFeature/REST",
      "http://localhost:8080/data",
      List(Query("tyrannosaurus", Some("gene")))
    ), Track("my_sequence_track",
      "DNA",
      "JBrowse/View/Track/Sequence",
      "JBrowse/Store/SeqFeature/REST",
      "http://localhost:8080/data",
      List(Query("tyrannosaurus", sequence = Some(true))))))
  }

  def getRefSeqs = {
    List(RefSeqs(50001, "ctgA", "seq/ctgA", 20000, 50001, 0), RefSeqs(66, "ctgB", "seq/ctgB", 20000, 66, 0))
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

  def getFeatures = {
    Features(features = List(
      Feature(None, None, 123, 456, None, None, None, None, None),
      Feature(None, None, 123, 456, Some(42), None, None, None, None),
      Feature(Some("gattacagattaca"), None, 0, 14, None, None, None, None),
      Feature(None, Some("mRNA"), 5975, 9744, Some(0.84), Some(1), Some("au9.g1002.t1"), Some("globallyUniqueString3"),
        subfeatures = Some(List(SubFeatures(
          "five_prime_UTR",
          5975,
          6109,
          Some(0.98),
          1,
          None
        ),
          SubFeatures(
            "start_codon",
            6110,
            6112,
            None,
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            6110,
            6148,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            6615,
            6683,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            6758,
            7040,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            7142,
            7319,
            Some(1),
            1,
            Some(2)
          ),
          SubFeatures(
            "CDS",
            7411,
            7686,
            Some(1),
            1,
            Some(1)
          ),
          SubFeatures(
            "CDS",
            7748,
            7850,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            7953,
            8098,
            Some(1),
            1,
            Some(2)
          ),
          SubFeatures(
            "CDS",
            8166,
            8320,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            8419,
            8614,
            Some(1),
            1,
            Some(1)
          ),
          SubFeatures(
            "CDS",
            8708,
            8811,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "CDS",
            8927,
            9273,
            Some(1),
            1,
            Some(1)
          ),
          SubFeatures(
            "CDS",
            9414,
            9494,
            Some(1),
            1,
            Some(0)
          ),
          SubFeatures(
            "stop_codon",
            9492,
            9494,
            None,
            1,
            Some(0)
          ),
          SubFeatures(
            "three_prime_UTR",
            9495,
            9744,
            Some(0.86),
            1,
            None
          )
        )
        )
      ))
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
                    length: Int,
                    name: String,
                    seqDi: String,
                    seqChunkSize: Int,
                    end: Int,
                    start: Int
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
                    seq: Option[String] = None,
                    `type`: Option[String] = None,
                    start: Int,
                    end: Int,
                    score: Option[Double] = None,
                    strand: Option[Int] = None,
                    name: Option[String] = None,
                    uniqueID: Option[String] = None,
                    subfeatures: Option[List[SubFeatures]] = None
                    )

case class SubFeatures(
                        `type`: String,
                        start: Int,
                        end: Int,
                        score: Option[Double] = None,
                        strand: Int,
                        phase: Option[Int] = None
                        )

