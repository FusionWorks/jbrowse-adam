package md.fusionworks.adam.jbrowse.config

import com.typesafe.config.ConfigFactory
import md.fusionworks.adam.jbrowse.config.TrackType.TrackType
import org.bdgenomics.adam.rdd.ADAMContext._

object ConfigLoader {

  private var jBrowseConf = getDefaultJBrowseConf

  def getJBrowseConf = jBrowseConf

  def getDefaultJBrowseConf = {
    val defaultConfigPath = ConfigFactory.load().getString("config.path")
    ConfigFactory.load(defaultConfigPath)
  }

  def loadJBrowseConfFromPath(path: String) = {
    jBrowseConf = ConfigFactory.load(s"$path.conf")
  }

  def getSparkMasterUrl = {
    if (jBrowseConf.hasPath("spark.masterUrl"))
      Some(jBrowseConf.getString("spark.masterUrl"))
    else None
  }

  def getTracksConfig: List[TrackConfig] = {
    jBrowseConf.getList("jbrowse.tracks").map { cv =>
      val config = cv.unwrapped().asInstanceOf[java.util.HashMap[String, String]]
      TrackConfig(
        config.get("filePath"),
        TrackType.withName(config.get("fileType")),
        config.get("trackType"),
        java.util.UUID.randomUUID.toString
      )
    }
  }
}


case class TrackConfig(filePath: String, fileType: TrackType, trackType: String, id: String)


object TrackType extends Enumeration {
  type TrackType = Value
  val Alignment, Reference, Variants = Value
}
