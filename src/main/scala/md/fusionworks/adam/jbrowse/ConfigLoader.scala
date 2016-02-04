package md.fusionworks.adam.jbrowse

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  val conf = ConfigFactory.load()

  var trackConf: Config = ConfigFactory.load(conf.getString("config.path"))

  def loadTrackConf(path: String) = {
    trackConf = ConfigFactory.load(s"$path.conf")
  }
}