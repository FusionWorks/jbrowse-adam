package md.fusionworks.adam.jbrowse

import akka.actor.Actor
import md.fusionworks.adam.jbrowse.models.JsonProtocol._
import md.fusionworks.adam.jbrowse.models._
import spray.httpx.SprayJsonSupport._
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ServiceActor extends Actor with Service {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}


// this trait defines our service behavior independently from the service actor

trait Service extends HttpService {

  val route = {
    get {
      path("data" / "trackList.json") {
        complete {
          JBrowseUtil.getTrackList
        }
      }
    } ~
      get {
        path("data" / "seq" / "refSeqs.json") {
          complete {
            JBrowseUtil.getRefSeqs
          }
        }
      } ~
      get {
        path("data" / "stats" / "global") {
          complete {
            JBrowseUtil.getGlobal
          }
        }
      } ~
      path("data" / "features" / Rest) { pathRest =>
        parameters('start, 'end,'reference_sequences_only.as[Boolean]?) {
          (start, end, reference_sequences_only) =>
            if(reference_sequences_only == Some(true))
          complete(JBrowseUtil.getReferenceFeatures(start.toLong, end.toLong, pathRest))
            else
              complete(JBrowseUtil.getAlignmentFeatures(start.toLong, end.toLong, pathRest))
        }
      } ~
      path("") {
        getFromResource("jbrowse/index.html")
      } ~ {
      getFromResourceDirectory("jbrowse/")
    }
  }

}
