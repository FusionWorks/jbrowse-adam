package md.fusionworks.adam.jbrowse.models

import spray.json.DefaultJsonProtocol


object JsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat=jsonFormat3(RefSeqs)
  implicit val tracksConfFormat = jsonFormat5(TracksConf)
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


