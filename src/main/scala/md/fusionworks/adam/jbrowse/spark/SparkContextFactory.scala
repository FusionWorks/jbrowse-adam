package md.fusionworks.adam.jbrowse.spark

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by gganebnyi on 2/24/15.
 */
object SparkContextFactory {

  val conf = ConfigFactory.load()

  private var sparkContext: Option[SparkContext] = None
  private lazy val sparkSqlContext = new SQLContext(getSparkContext)

  private val masterPath = conf.getString("spark.master.path")

  def getSparkContext: SparkContext = {
    sparkContext match {
      case Some(context) => context
      case None =>
        val sparkConf = new SparkConf()
          .setMaster(masterPath)
          .setAppName("spark-context")
        sparkContext = Some(new SparkContext(sparkConf))
        sparkContext.get
    }
  }

  def getSparkSqlContext = sparkSqlContext

  def stopSparkContext() = getSparkContext.stop()

}