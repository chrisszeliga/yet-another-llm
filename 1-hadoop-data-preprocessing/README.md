# Hadoop Data Preprocessing

## Overview

This project utilizes Hadoop MapReduce to process large text corpus in .txt format. 
The main objective is to shard the data into smaller segments, analyze these segments to extract tokens and the frequency of these tokens, 
and generate word embeddings using DeepLearning4j's Word2Vec.

## Project Structure
**1. Input Data:** The input data is a ```.txt``` file containing text from a book.

**2. Sharding:** The input file is divided into smaller ```.txt``` shards for efficient processing. 

**3. Tokenization and Frequency Calculation:**
- The first MapReduce job tokenizes the text to identify all words, punctuation, and symbols, calculating their frequency in the input data.

**4. Embedding Generation:**
- The second MapReduce job once again tokenizes the text and then feeds them into DeepLearning4j's Word2Vec to generate embeddings for each token.

## Sharding Process

The sharding process involves reading the input text file (```data.txt```) and splitting it into smaller chunks, called shards. 
This is crucial for parallel processing in Hadoop, enabling more efficient data handling. 
Each shard is stored as a separate ```.txt``` file in the ```input/shards``` directory.

### Implementation

* **FileSharder.scala:**
  This scala file handles the reading of the input file and the creation of the shards.
```
val lines = file.getLines()
// Iterate for total number of lines, calculating which shard the line belongs in
for (i <- 1 to totalLines) {
  val shardNum = Math.floor(i.toDouble / linesPerShard).toInt
  writers(shardNum).println(lines.next())
}
```

## MapReduce Job: Vocabulary Calculation

In this job, we utilize a mapper and a reducer to calculate the frequency of tokens. 
The resulting parts of the job are outputted to ```output/vocabulary```.

### Mapper

* **Tokenization:** 
  The mapper reads lines from the shard, tokenizes the words, punctuation, and symbols, and emits each token with a count of 1.
``` 
line.split("(?<=[,.:;?!--_\"'()“”‘’—])\\s*|(?=[,.:;?!--_\"'()“”‘’—])|\\s+")
        .filter(_.nonEmpty)     // Can be empty string because of Lookbehind and Lookahead overlap
        .foreach { token =>
          word.set(token + " " + encoding.encode(token).toString)
          output.collect(word, one)
        }
``` 

### Reducer

* **Frequency Calculation:**
  The reducer aggregates the counts emitted by the mapper, resulting in a total frequency for each unique token.
```
val sum = values.asScala.reduce((valueOne, valueTwo) => new IntWritable(valueOne.get() + valueTwo.get()))
output.collect(key, new IntWritable(sum.get()))
``` 

## MapReduce Job: Word2Vec Embedding Generation

The second MapReduce job involves generating embeddings for the tokens using DeepLearning4j's Word2Vec.
The resulting parts of the job are outputted to ```output/embeddings```.

### Mapper

* **Tokenization and Embedding:**
  The mapper tokenizes the text again and retrieves the token embeddings for each token.
```
// Go through each vocab and get the token and its embedding
vocab.forEach { token =>
  val embedding = word2Vec.getWordVector(token.getWord)
  output.collect(new Text(token.getWord), new Text(util.Arrays.toString(embedding)))
}
```

### Reducer

* **Averaging Embeddings:**
  The reducer takes the embeddings from the mapper and averages them to create a single representation for each token.
```
val avgEmbedding = values.asScala.reduce { (valueOne, valueTwo) =>
  // Convert values into array of doubles
  val v1 = valueOne.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)
  val v2 = valueTwo.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)

  // Get avg of the arrays
  val avg = v1.zip(v2).map { case (x, y) => (x + y) / 2 }
  new Text(util.Arrays.toString(avg))
}
output.collect(key, avgEmbedding)
```

