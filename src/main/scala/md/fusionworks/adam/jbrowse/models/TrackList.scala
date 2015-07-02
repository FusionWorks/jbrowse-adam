package md.fusionworks.adam.jbrowse.models

import spray.json.DefaultJsonProtocol

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val qFormat = jsonFormat3(Querys)
  implicit val TrackFormat = jsonFormat6(Track1)
  implicit val PersonFormat = jsonFormat1(TrackList)
}

object OTrakList{
  def TraklistJosn() = {
    TrackList(tracks = List(Track1("mygene_track",
    "Genes",
    "JBrowse/View/Track/HTMLFeatures",
    "JBrowse/Store/SeqFeature/REST",
    "http://my.site.com/rest/api/base",
    List(Querys("tyrannosaurus", Some("gene")))
  ), Track1("my_sequence_track",
    "DNA",
    "JBrowse/View/Track/Sequence",
    "JBrowse/Store/SeqFeature/REST",
    "http://my.site.com/rest/api/base",
    List(Querys("tyrannosaurus", sequence = Some(true))))))
}
}
case class TrackList(tracks: List[Track1]
                      )

case class Track1(
                   label: String,
                   key: String,
                   `type`: String,
                   storeClass: String,
                   baseUrl: String,
                   query: List[Querys]

                   )

case class Querys(
                   organism: String,
                   soType: Option[String] = None,
                   sequence: Option[Boolean] = None
                   )
