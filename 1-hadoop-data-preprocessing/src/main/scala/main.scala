import com.typesafe.config.ConfigFactory


@main
def main(): Unit = {
  val config = ConfigFactory.load("local.conf")
//  val config = ConfigFactory.load("aws.conf")

  // Retrieve the output directory from the config
  val inputPath = config.getString("inputPath")
  val outputPath = config.getString("outputPath")

  // Generate Shards for input file (locally)
//  FileSharder.sharderMain(inputPath, outputPath, 5)

  // Get tokens and their frequency
  Vocabulary.runVocabularyMapReduce(s"$inputPath/shards/", s"$outputPath/vocabulary/")

  // Get embeddings for tokens
  Embeddings.runEmbeddingMapReduce(s"$inputPath/shards/", s"$outputPath/embeddings/")

}

