package md.fusionworks.adam.jbrowse.model

import spray.json.DefaultJsonProtocol

trait JBrowseModel

case class TrackList(tracks: List[Track])

case class Track(
                  label: String,
                  key: String,
                  `type`: String,
                  storeClass: String,
                  baseUrl: String
                )

case class RefSeqs(
                    name: String,
                    start: Long,
                    end: Long
                  )

case class Global(
                   featureDensity: Double,
                   featureCount: Int,
                   scoreMin: Int,
                   scoreMax: Int,
                   scoreMean: Int,
                   scoreStdDev: Double
                 )

case class Features(features: List[Map[String, String]])

object TrackType extends Enumeration {
  type TrackType = Value
  val Alignment, Reference, Variants = Value
}


object JsonProtocol extends DefaultJsonProtocol {
  implicit val trackFormat = jsonFormat5(Track)
  implicit val trackListFormat = jsonFormat1(TrackList)
  implicit val refSeqsFormat = jsonFormat3(RefSeqs)
  implicit val globalFormat = jsonFormat6(Global)
  implicit val featuresFormat = jsonFormat1(Features)
}