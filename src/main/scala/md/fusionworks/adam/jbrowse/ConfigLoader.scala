package md.fusionworks.adam.jbrowse

import com.typesafe.config.ConfigFactory
import md.fusionworks.adam.jbrowse.models.{FileType, TrackConfig}
import org.bdgenomics.adam.rdd.ADAMContext._

object ConfigLoader {

  private val ConfigPath = "spark.master"

  // Load configuration from application.conf
  val conf = ConfigFactory.load()

  // Load configuration from local or cluster.conf
  var trackConf = ConfigFactory.load(conf.getString("config.path"))

  def getSparkMasterUrl =
    if (trackConf.hasPath(ConfigPath)) {
      Some(trackConf.getString(s"$ConfigPath.url"))
    } else None


  def loadTrackConf(path: String) = {
    trackConf = ConfigFactory.load(s"$path.conf")
  }

  def getTrackConfig: List[TrackConfig] = {
    trackConf.getList("jbrowse.tracks").map { cv =>
      val config = cv.unwrapped().asInstanceOf[java.util.HashMap[String, String]]
      TrackConfig(config.get("filePath"), FileType.withName(config.get("fileType")), config.get("trackType"))
    }
  }

  def getBaseUrl: String = trackConf.getString("jbrowse.track.base.url")
}