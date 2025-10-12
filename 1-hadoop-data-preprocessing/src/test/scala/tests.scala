import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source


class tests extends AnyFlatSpec with Matchers {

  it should "Correctly gets data.txt total number of lines" in {
    val file = Source.fromFile("src/test/scala/testdata.txt")
    val totalLines = file.getLines().size
    file.close()
    totalLines should be (812)
  }

  it should "create the correct number of shards (203)" in {

    val getLinesFile = Source.fromFile("src/test/scala/testdata.txt")
    val totalLines = getLinesFile.getLines().size
    val linesPerShard = Math.ceil(totalLines.toDouble / 4).toInt
    getLinesFile.close()

    linesPerShard should be (203)
  }

  it should "create the correct number of shards (5)" in {

    val getLinesFile = Source.fromFile("src/test/scala/test2.txt")
    val totalLines = getLinesFile.getLines().size
    val linesPerShard = Math.ceil(totalLines.toDouble / 5).toInt
    getLinesFile.close()

    linesPerShard should be (1)
  }

  it should "should handle empty file" in {
    val getLinesFile = Source.fromFile("src/test/scala/test3.txt")
    val totalLines = getLinesFile.getLines().size
    val linesPerShard = Math.ceil(totalLines.toDouble / 5).toInt
    getLinesFile.close()

    linesPerShard should be (0)
  }

  it should "split a sentence correctly" in {
    val line = "This is a sentence, I am testing. This is another."
    val result = line.split("(?<=[,.:;?!--_\"'()“”‘’—])\\s*|(?=[,.:;?!--_\"'()“”‘’—])|\\s+")

    result.length should be (13)
  }

  it should "split sentences correctly" in {
    val line = "Hello there. How are you? I'm fine!"
    val result = line.split("(?<=[a-z][.?!])\\s+")
    result.length should be (3)
  }

  it should "average embedding correctly" in {
    val v1 = "[14, 5, 8]".stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)
    val v2 = "[2, 5, 4]".stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)

    // Get avg of the arrays
    val avg = v1.zip(v2).map { case (x, y) => (x + y) / 2 }
    avg should be (Array(8.0, 5.0, 6.0))
  }
}
