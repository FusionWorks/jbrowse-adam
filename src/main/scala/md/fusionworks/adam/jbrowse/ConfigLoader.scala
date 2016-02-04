package md.fusionworks.adam.jbrowse

import com.typesafe.config.ConfigFactory
import md.fusionworks.adam.jbrowse.models.{FileType, TrackConfig}
import org.bdgenomics.adam.rdd.ADAMContext._

object ConfigLoader {

  // Load configuration from application.conf
  val conf = ConfigFactory.load()

  // Load configuration from local or cluster.conf
  var trackConf = ConfigFactory.load(conf.getString("config.path"))

  // JBrowse configuration
  val jBrowseConf = trackConf.getConfig("jbrowse")

  def loadTrackConf(path: String) = {
    trackConf = ConfigFactory.load(s"$path.conf")
  }

  def getTrackConfig: List[TrackConfig] = {
    jBrowseConf.getList("tracks").map { cv =>
      val config = cv.unwrapped().asInstanceOf[java.util.HashMap[String, String]]
      TrackConfig(config.get("filePath"), FileType.withName(config.get("fileType")), config.get("trackType"))
    }
  }

  def getBaseUrl: String = jBrowseConf.getString("track.base.url")
}