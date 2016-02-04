package md.fusionworks.adam.jbrowse.spark

import md.fusionworks.adam.jbrowse.ConfigLoader.trackConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


object SparkContextFactory {
  val configPath = s"spark.master"

  val masterUrl: Option[String] =
    if (trackConf.hasPath(configPath)) {
      Some(trackConf.getConfig(configPath).getString("url"))
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
