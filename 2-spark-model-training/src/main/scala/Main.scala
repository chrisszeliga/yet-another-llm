import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}

object Main {
  def main(args: Array[String]): Unit = {
    //val config = ConfigFactory.load("local.conf")
    val config = ConfigFactory.load("aws.conf")

    // Retrieve the output directory from the config
    val inputPath = config.getString("inputPath")
    val outputPath = config.getString("outputPath")

    // Set up Spark configuration and context
    val conf = new SparkConf()
      .setAppName("DL4J Spark LLMTraining")
    //  .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val slidingWindowData = SlidingWindow.main(sc, inputPath)
    Training.trainModel(sc, outputPath, slidingWindowData)

    sc.stop()
  }
}