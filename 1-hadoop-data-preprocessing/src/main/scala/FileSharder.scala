import java.io.{File, PrintWriter}
import scala.io.Source
import org.slf4j.LoggerFactory

object FileSharder {
  private final val logger = LoggerFactory.getLogger(getClass)

  //
  // reformatFile
  //
  // Reads file and outputs new file without blank lines
  //
  // params: String inputFile - The path of the file
  //         String outputPath - The path where the output will write to
  //
  def reformatFile(inputPath: String): Unit =
//    val inputStream = getClass.getResourceAsStream("/input/data.txt")
//    val file = Source.fromInputStream(inputStream)

    val file = Source.fromFile(inputPath)
    val writer = new PrintWriter(new File(inputPath, "reformattedData.txt"))

    val lines = file.getLines()
    lines.foreach { line =>
      // Check if the line is not empty
      if (line.trim.nonEmpty) {
        writer.println(line) // Write the non-empty line to the output file
      }
    }
    file.close()
    writer.close()


  //
  // shardFile
  //
  // Splits the input file into smaller shards
  //
  // params: String inputFile - The path of the file
  //         Int numShards - amount of shards desired
  //
  def shardFile(inputPath: String, numShards: Int): Unit =
    // Total input lines and lines per shard calculation
    // Open file just to get total lines, this exhausts the iterator,
    // so a fresh new instance of the inputFile will have to be loaded in
//    val inputStream = getClass.getResourceAsStream("/Input/data.txt")
//    val getLinesFile = Source.fromInputStream(inputStream, "UTF-8")
    val getLinesFile = Source.fromFile(s"$inputPath/data.txt")
    val totalLines = getLinesFile.getLines().size
    val linesPerShard = Math.ceil(totalLines.toDouble / numShards).toInt
    getLinesFile.close()

    // Input and output file setup
//    val freshInputStream = getClass.getResourceAsStream("/Input/data.txt")
//    val file = Source.fromInputStream(freshInputStream, "UTF-8")   // Fresh file
    val file = Source.fromFile(s"$inputPath/data.txt")
    val outputFolder = s"$inputPath/shards"
    val writers = (1 to numShards).map(num => new PrintWriter(new File(outputFolder, s"shard_$num.txt")))
    logger.info("Input file read in")

    val lines = file.getLines()
    // Iterate for total number of lines, calculating which shard the line belongs in
    for (i <- 1 to totalLines) {
      val shardNum = Math.floor(i.toDouble / linesPerShard).toInt
      writers(shardNum).println(lines.next())
    }

    // Clean-up
    writers.foreach(_.close())
    file.close()


  def sharderMain(inputPath: String, outputPath: String, numShards: Int): Unit = {
    // Reformat file so it does not have blank lines (needed for embeddings)
//    logger.info("Starting Reformatting Process")
//    reformatFile(inputPath)
//    logger.info("Finished Reformatting Process")

    // Generate shards
    logger.info("Starting Sharding Process")
    shardFile(inputPath, numShards)
    logger.info("Finished Sharding Process")
  }
}
