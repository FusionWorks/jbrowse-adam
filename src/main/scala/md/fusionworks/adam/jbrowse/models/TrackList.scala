package md.fusionworks.adam.jbrowse.models

import spray.json.DefaultJsonProtocol

object jsonProtocol extends DefaultJsonProtocol {
  implicit val queryFormat = jsonFormat3(Query)
  implicit val trackFormat = jsonFormat6(Track)
  implicit val trakListFormat = jsonFormat1(TrackList)
}

object jbrowseUtil{
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
