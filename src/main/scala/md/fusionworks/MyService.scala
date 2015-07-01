package md.fusionworks

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpCharsets._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.util._




// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)


}


// this trait defines our service behavior independently from the service actor

import spray.json.DefaultJsonProtocol


trait MyService extends HttpService{





  //source.toJson
  //implicit val colorFormat = jsonFormat4(source)



  val myRoute = {

    path("data/trackList.json") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
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
              List(Querys("tyrannosaurus", sequence = Some(true))))))//.toJson
            jsonWriter[TrackList]
          }
        }
      }
    }
      path("") {

      getFromResource("jbrowse/index.html")
    } ~ {
      getFromResourceDirectory("jbrowse/")
    }
  }

}


case class TrackList(tracks: List[Track1]
          /*tracks:String=List(Map("label"->"mygene_track",
                            "key"->"Genes",
                            "type"->"JBrowse/View/Track/HTMLFeatures",
                            "storeClass"->"JBrowse/Store/SeqFeature/REST",
                            "baseUrl"->"http://my.site.com/rest/api/base",
                            "query"->Map(
                                          "organism"->"tyrannosaurus",
                                            "soType"-> "gene"
                                          )
                            ),
            Map(
            "label"-> "my_sequence_track",
            "key"->"DNA",
            "type"->"JBrowse/View/Track/Sequence",
            "storeClass"-> "JBrowse/Store/SeqFeature/REST",
            "baseUrl"-> "http://my.site.com/rest/api/base",
            "query"->Map( "organism"-> "tyrannosaurus", "sequence"-> true)
          )
          )*/
             )

case class Track1(
                   label: String,
                  key:String,
                 `type`:String,
                  storeClass: String,
                 baseUrl:String,
                 query:List[Querys]

)

case class Querys(
                   organism:String,
                   soType:Option[String]=None,
                   sequence:Option[Boolean]=None
                   )

