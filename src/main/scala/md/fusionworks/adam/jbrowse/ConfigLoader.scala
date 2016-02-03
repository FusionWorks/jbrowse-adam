package md.fusionworks.adam.jbrowse

import com.typesafe.config.ConfigFactory

object ConfigLoader {
  val conf = ConfigFactory.load()

  var path = conf.getString("config.path")

  def setPath(argPath: String) = path = argPath
}