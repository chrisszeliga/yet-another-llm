import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Adam
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import java.io.{BufferedOutputStream, File, PrintWriter}
import java.net.URI

object Training {

  // Logging Setup
  private final val logger = LoggerFactory.getLogger(getClass)


  //
  // trainModel
  //
  // Takes in the sliding window data, trains a model on the slidingWindowData, and saves model.
  //
  // Params: outputPath - A string to represent the path where the model is saved to
  //         slidingWindow - An Array of WindowData (Array of tuple (window: Array[Array[Double]], label: Array[Double])
  //
  def trainModel(sc : SparkContext, outputPath: String, slidingWindowData: Array[WindowData]): Unit = {
    logger.info("Spark Training LLM: Start")

    // Setup file output
//    val writer = new PrintWriter(new File(outputPath, "statistics.txt"))

    // Print to statistics
//    writer.println("Total executors: " + sc.getExecutorMemoryStatus.size)
    logger.info("Spark Training LLM: Total executors: " + sc.getExecutorMemoryStatus.size)

    // Convert Array to RDD
    val rdd: RDD[WindowData] = sc.parallelize(slidingWindowData)

    // Convert RDD of WindowData to RDD of DataSet
    // Note: Can change MultiLayerConfiguration to LSTMs, CNNs, or other layers that can handle 3D data.
    // Dense Layer only handles 2D data.
    val dataSetRdd: RDD[DataSet] = rdd.map { case WindowData(window, label) =>
      // Ensure features have the correct shape for the model
      val features = Nd4j.create(window).reshape(1, 8) // Reshape to [1, 8]
      val labels = Nd4j.create(label).reshape(1, 2) // Reshape to [1, 2]
      new DataSet(features, labels)
    }

    // Model Configuration
    val modelConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .updater(new Adam(0.001))
      .list()
      .layer(new DenseLayer.Builder().nIn(8).nOut(64).activation(Activation.RELU).build())
      .layer(new DenseLayer.Builder().nIn(64).nOut(32).activation(Activation.RELU).build())
      .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .activation(Activation.IDENTITY)
        .nIn(32).nOut(2)
        .build())
      .build()

    val trainingMaster = new ParameterAveragingTrainingMaster.Builder(4)
      .batchSizePerWorker(32)  // Batch size on each Spark worker
      .averagingFrequency(5)   // Frequency of parameter averaging
      .workerPrefetchNumBatches(2)
      .build();

    val sparkModel = new SparkDl4jMultiLayer(sc, modelConf, trainingMaster)

    sparkModel.setListeners(
      new ScoreIterationListener(10), // Log every 10 iterations
    )

    // Train the model
    val numEpochs = 3
    for (epoch <- 0 until numEpochs) {
      val start = System.currentTimeMillis()
      sparkModel.fit(dataSetRdd)
      val end = System.currentTimeMillis()

      // Print epoch time to statistics
//      writer.println(s"Epoch $epoch time: ${end - start} ms")
      logger.info(s"Spark Training LLM: Epoch $epoch time: ${end - start} ms")
    }


    // Save the model
    val fileSystem: FileSystem = FileSystem.get(new URI(outputPath), sc.hadoopConfiguration)
    val modelPath = outputPath + "model.zip"
    val target = new BufferedOutputStream(fileSystem.create(new Path(modelPath)))
    ModelSerializer.writeModel(sparkModel.getNetwork, target, true)

//    writer.close()
    logger.info("Spark Training LLM: Finish")
  }
}
