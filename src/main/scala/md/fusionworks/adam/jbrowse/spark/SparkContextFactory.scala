package md.fusionworks.adam.jbrowse.spark

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by gganebnyi on 2/24/15.
  */
object SparkContextFactory {

  val conf = ConfigFactory.load()
  var masterUrl: Option[String] = None

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
    println("staring SparkContext...")
    getSparkContext
  }

  def getSparkSqlContext = sparkSqlContext

  def stopSparkContext() = getSparkContext.stop()

}
