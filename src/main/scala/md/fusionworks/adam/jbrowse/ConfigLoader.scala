package md.fusionworks.adam.jbrowse

import com.typesafe.config.ConfigFactory

object ConfigLoader {
  final val conf = ConfigFactory.load()

  var path = conf.getString("config.path")

  def setPath(argPath: String) = path = argPath
}