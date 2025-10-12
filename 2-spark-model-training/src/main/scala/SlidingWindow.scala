import scala.collection.mutable
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType}
import com.knuddels.jtokkit.Encodings
import org.apache.spark.broadcast.Broadcast
import org.slf4j.LoggerFactory


//
// WindowData
//
// Container for storing a window and its label
//
// params: window: An Array of embeddings (Array of Doubles) representing a window
//         label: An Array of Doubles (a single embedding) representing the label for a window
//
case class WindowData(window: Array[Array[Double]], label: Array[Double])


object SlidingWindow {

  // Logging Setup
  private final val logger = LoggerFactory.getLogger(getClass)

  // Map to go from token to its embedding
  // Key: Token ID
  // Value: Embedding
  private val token2embedding: mutable.Map[Int, Array[Double]] = mutable.Map()


  //
  // readEmbeddings
  //
  // Reads in the text file embeddings.txt, and stores every token and its embedding in
  // token2embedding, so that when sliding window is performed, token's can be translated
  // to their embedding during the computation.
  // Returns a broadcast of the token2embedding map.
  //
  // Params: inputPath - A string to represent the path where the embeddings are read from
  //
  private def readEmbeddings(sc: SparkContext, inputPath: String): Broadcast[mutable.Map[Int, Array[Double]]] = {
    logger.info("Spark Read Embeddings Job: Start")

    // Spark RDD for parallelization
    val dataRDD: RDD[String] = sc.textFile(inputPath)

    // Process each line and collect mappings as a sequence of pairs
    // (flatMap instead of map here to get rid of the Option[] wrapping)
    val data = dataRDD.flatMap { line =>
      // For each line split by "\t" to get id (left) and embedding (right)
      val parts = line.split("\t")
      if (parts.length == 2) {
        // Parse data
        val id = parts(0).trim.toInt
        val embedding = parts(1).trim.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)

        // Log token and embeddings
        logger.info(s"Spark Read Embeddings: Processing $id -> ${embedding.mkString("Array(", ", ", ")")}")
        Some(id -> embedding)  // Return as tuple (token id, embedding)
      } else {
        None  // Error check: Possible invalid / empty lines
      }
    }.collect() // Collect the data on the driver node so token2embedding can be populated

    // Populate token2embeddings
    data.foreach { case (id, embedding) =>
      token2embedding(id) = embedding
    }

    logger.info("Spark Read Embeddings Job: Finished")

    // Create broadcast of map to ensure executors have access token2embedding
    val bcToken2Embedding = sc.broadcast(token2embedding)
    bcToken2Embedding
  }


  // Object to wrap Jtokkit's encoding so that it is serializable for Spark
  private object SerializableEncoding {
    // Jtokkit tokenization setup
    private val registry: EncodingRegistry = Encodings.newLazyEncodingRegistry()
    private val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    // Tokenize word, and convert to array
    def encodeToken(token: String): Array[Int] = encoding.encode(token).toArray
  }


  //
  // performSlidingWindow
  //
  // Reads in the text file data.txt, tokenizes each word/punctuation/symbol, performs
  // sliding window on the tokenizes, generating windows and a target per window,
  // then converts the tokens to their embeddings and stores them in an Array of type WindowData
  // Returns an Array of WindowData (Array of tuple (window: Array[Array[Double]], label: Array[Double])
  //
  // Params: inputPath - A string to represent the path where the embeddings are read from
  //
  private def performSlidingWindow(sc: SparkContext, inputPath: String, bcToken2Embedding: Broadcast[mutable.Map[Int, Array[Double]]]): Array[WindowData] = {
    logger.info("Spark Sliding Window Job: Start")

    // Convert token2embedding back into a map
    val embeddings = bcToken2Embedding.value

    val windowSize = 4

    // Sliding Window
    val dataRDD: RDD[WindowData] = sc.textFile(inputPath).flatMap { line =>
      // Split by word/punctuation/symbol and tokenize
      val tokens = line.split("(?<=[,.:;?!--_\"'()“”‘’—])\\s*|(?=[,.:;?!--_\"'()“”‘’—])|\\s+")
        .filter(_.nonEmpty)
        .flatMap(SerializableEncoding.encodeToken)

      // Get windows and labels if tokens has at least windowSize + 1 elements, to ensure
      // at least one window and label is able to be produced.
      if (tokens.length >= windowSize + 1) {
        // Go from 0 to length of tokens - windowSize + 1 for label,
        // so we do not go out of bounds when getting last window and label
        (0 to (tokens.length - (windowSize + 1))).map { i =>
          logger.info("Spark Sliding Window: Performing Sliding Window")

          // Get window and label
          val window = tokens.slice(i, i + windowSize)
          val labelToken = tokens(i + windowSize)

          // Retrieve embeddings for each token in the window
          val windowEmbeddings = window.map { token =>
            embeddings.getOrElse(token, throw new Error(s"Error retrieving embedding for token $token"))
          }
          // Retrieve embedding for label token
          val labelEmbedding = embeddings.getOrElse(labelToken, throw new Error(s"Error retrieving embedding for token $labelToken"))

          logger.info("Spark Sliding Window: Sliding Window Finished")
          WindowData(windowEmbeddings, labelEmbedding)
        }
      } else {
        Seq.empty // Not enough tokens, return empty sequence
      }
    }

    // Collect results
    val slidingWindowData = dataRDD.collect()

    logger.info("Spark Sliding Window Job: Finish")

    slidingWindowData // Return windows and labels
  }

  // Main method to get embeddings for each token and
  // then compute and return sliding window on the data.
  def main(sc: SparkContext, inputPath: String): Array[WindowData] = {
    val bcToken2Embedding = readEmbeddings(sc, inputPath + "embeddings.txt")
    val result = performSlidingWindow(sc, inputPath + "data.txt", bcToken2Embedding)
    result
  }
}
