import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.*
import org.apache.hadoop.mapred.*
import com.knuddels.jtokkit.api
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType}
import com.knuddels.jtokkit.Encodings
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util
import scala.jdk.CollectionConverters.*

object Vocabulary:
  private final val logger = LoggerFactory.getLogger(getClass)

  class Map extends MapReduceBase with Mapper[LongWritable, Text, Text, IntWritable]:
    private final val one = new IntWritable(1)
    private val word = new Text()

    // Jtokkit tokenization setup
    val registry: EncodingRegistry = Encodings.newLazyEncodingRegistry()
    val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    @throws[IOException]
    override def map(key: LongWritable, value: Text, output: OutputCollector[Text, IntWritable], reporter: Reporter): Unit =
      val line: String = value.toString

      logger.info("Vocabulary M: tokenizing in mapper")

      // Regex to split words, punctuation, and symbols
      // Lookbehind: (?<=[,.:;?!--_"'()“”—])\\s*
      //    Split if preceded by either one of the specified punctuation characters and any number of whitespace characters.
      // Lookahead: (?=[,.:;?!--_"'()“”—])
      //    Split if followed by either one of the specified punctuation characters
      // Or split by spaces
      line.split("(?<=[,.:;?!--_\"'()“”‘’—])\\s*|(?=[,.:;?!--_\"'()“”‘’—])|\\s+")
        .filter(_.nonEmpty)     // Can be empty string because of Lookbehind and Lookahead overlap
        .foreach { token =>
          word.set(token + " " + encoding.encode(token).toString)
          output.collect(word, one)
        }
      logger.info("Vocabulary M: End of Mapper")

  class Reduce extends MapReduceBase with Reducer[Text, IntWritable, Text, IntWritable]:
    override def reduce(key: Text, values: util.Iterator[IntWritable], output: OutputCollector[Text, IntWritable], reporter: Reporter): Unit =
      logger.info("Vocabulary R: Reducing values")
      // Reduce values by summing (summing their counts)
      val sum = values.asScala.reduce((valueOne, valueTwo) => new IntWritable(valueOne.get() + valueTwo.get()))
      output.collect(key, new IntWritable(sum.get()))
      logger.info("Vocabulary R: End of reducer")

  def runVocabularyMapReduce(inputPath: String, outputPath: String) =
    logger.info("Starting Vocabulary Map/Reduce")
    val conf: JobConf = new JobConf(this.getClass)
    conf.setJobName("Vocabulary")
    conf.set("fs.defaultFS", "file:///")
//    conf.set("fs.defaultFS", "s3://chrisszeliga-cs441-hw1-data/")
//    conf.set("mapreduce.framework.name", "yarn") // Set framework to YARN
    conf.set("mapreduce.job.maps", "5")
    conf.set("mapreduce.job.reduces", "2")
    conf.setJarByClass(this.getClass)
    conf.setOutputKeyClass(classOf[Text])
    conf.setOutputValueClass(classOf[IntWritable])
    conf.setMapperClass(classOf[Map])
    conf.setCombinerClass(classOf[Reduce])
    conf.setReducerClass(classOf[Reduce])
    conf.setInputFormat(classOf[TextInputFormat])
    conf.setOutputFormat(classOf[TextOutputFormat[Text, IntWritable]])
    FileInputFormat.setInputPaths(conf, new Path(inputPath))
    FileOutputFormat.setOutputPath(conf, new Path(outputPath))
    JobClient.runJob(conf)
    logger.info("Finished Vocabulary Map/Reduce")