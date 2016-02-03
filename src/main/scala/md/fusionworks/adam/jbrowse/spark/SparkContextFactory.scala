package md.fusionworks.adam.jbrowse.spark

import md.fusionworks.adam.jbrowse.ConfigLoader.{conf, path}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


object SparkContextFactory {
  final val configPath = s"spark.$path"

  final val masterUrl: Option[String] =
    if (conf.hasPath(configPath)) {
      Some(conf.getConfig(configPath).getString("master.url"))
    } else None

  private var sparkContext: Option[SparkContext] = None
  private lazy val sparkSqlContext = new SQLContext(getSparkContext)

  def getSparkContext: SparkContext = {
    sparkContext match {
      case Some(context) => context
      case None =>
        val sparkConf = new SparkConf()
          .setAppName("spark-context")
        masterUrl match {
          case Some(master) =>
            sparkConf.setMaster(master)
          case None =>
        }
        sparkContext = Some(new SparkContext(sparkConf))
        sparkContext.get
    }
  }

  def startSparkContext(): Unit = {
    println("Starting SparkContext...")
    getSparkContext
  }

  def getSparkSqlContext = sparkSqlContext

  def stopSparkContext() = getSparkContext.stop()

}
