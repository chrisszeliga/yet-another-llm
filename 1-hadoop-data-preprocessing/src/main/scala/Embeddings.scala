import java.io.IOException
import java.util
import scala.jdk.CollectionConverters.*
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.*
import org.apache.hadoop.mapred.*
import com.knuddels.jtokkit.api
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType}
import com.knuddels.jtokkit.Encodings
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator
import org.slf4j.LoggerFactory


object Embeddings:
  private final val logger = LoggerFactory.getLogger(getClass)

  class Map extends MapReduceBase with Mapper[LongWritable, Text, Text, Text]:
    // Jtokkit tokenization setup
    val registry: EncodingRegistry = Encodings.newLazyEncodingRegistry()
    val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    @throws[IOException]
    override def map(key: LongWritable, value: Text, output: OutputCollector[Text, Text], reporter: Reporter): Unit =
      // Get input
      val line: String = value.toString

      logger.info("Embedding M: Tokenizing sentences in mapper")

      // Split input into sentences
      val tokenizedSentences = line.split("(?<=[a-z])(?<=[.?!])\\s+")
        .map { sentence =>
          // Split each word/punctuation/symbol
            sentence.split("(?<=[,.:;?!--_\"'()“”‘’—])\\s*|(?=[,.:;?!--_\"'()“”‘’—])|\\s+")
              .filter(_.nonEmpty) // Can be empty string because of Lookbehind and Lookahead
              .map { token =>
                encoding.encode(token).toArray.mkString(" ") // Convert tokenized word into a string separated by " "
              }
              .mkString(" ") // Convert tokenized words into a string separated by " "
        }.toList

      // Convert tokenizedSentences because Deeplearning4j expects a Java Collection Type.
      val iter = new CollectionSentenceIterator(tokenizedSentences.asJava)

      logger.info("Embedding M: Starting Word2Vec")

      // Word2Vec configuration
      val embeddingDim = 2
      val word2Vec = new Word2Vec.Builder()
        .minWordFrequency(1)
        .layerSize(embeddingDim)
        .seed(42)
        .windowSize(5)
        .iterate(iter)
        .build();

      word2Vec.fit()

      logger.info("Embedding M: Getting embeddings")
      val vocab = word2Vec.getVocab.vocabWords()

      // Go through each vocab and get the token and its embedding
      vocab.forEach { token =>
        val embedding = word2Vec.getWordVector(token.getWord)
        output.collect(new Text(token.getWord), new Text(util.Arrays.toString(embedding)))
      }
      logger.info("Embedding M: End of Mapper")


  class Reduce extends MapReduceBase with Reducer[Text, Text, Text, Text]:
    override def reduce(key: Text, values: util.Iterator[Text], output: OutputCollector[Text, Text], reporter: Reporter): Unit =
      logger.info("Embedding R: Reducing embeddings")
      val avgEmbedding = values.asScala.reduce { (valueOne, valueTwo) =>
        // Convert values into array of doubles
        val v1 = valueOne.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)
        val v2 = valueTwo.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)

        // Get avg of the arrays
        val avg = v1.zip(v2).map { case (x, y) => (x + y) / 2 }
        new Text(util.Arrays.toString(avg))
      }
      output.collect(key, avgEmbedding)
      logger.info("Embedding R: End of Reducer")

  def runEmbeddingMapReduce(inputPath: String, outputPath: String) =
    logger.info("Starting Embedding Map/Reduce")
    val conf: JobConf = new JobConf(this.getClass)
    conf.setJobName("Embeddings")
    conf.set("fs.defaultFS", "file:///")
//    conf.set("fs.defaultFS", "s3://chrisszeliga-cs441-hw1-data/")
//    conf.set("mapreduce.framework.name", "yarn") // Set framework to YARN
    conf.set("mapreduce.job.maps", "5")
    conf.set("mapreduce.job.reduces", "2")
    conf.setJarByClass(this.getClass)
    conf.setOutputKeyClass(classOf[Text])
    conf.setOutputValueClass(classOf[Text])
    conf.setMapperClass(classOf[Map])
    conf.setCombinerClass(classOf[Reduce])
    conf.setReducerClass(classOf[Reduce])
    conf.setInputFormat(classOf[TextInputFormat])
    conf.setOutputFormat(classOf[TextOutputFormat[Text, Text]])
    FileInputFormat.setInputPaths(conf, new Path(inputPath))
    FileOutputFormat.setOutputPath(conf, new Path(outputPath))
    JobClient.runJob(conf)
    logger.info("Finished Embedding Map/Reduce")