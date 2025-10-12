import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFlatSpec with Matchers {

  // Test 1
  it should "Test Basic Spark Job" in {
    val conf = new SparkConf().setAppName("Simple Word Count").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val wordCount = sc.parallelize(Seq("Hello Spark", "Hello World"))
      .flatMap(_.split(" "))
      .countByValue()

    wordCount("Hello") should be (2)
    sc.stop()
  }

  // Test 2
  it should "Test sliding window method 1" in {
    // Sample input data (tokens)
    val tokens = Array(1, 2, 3, 4, 5)
    val windowSize = 4
    val expectedWindows = Array(
      Array(1, 2, 3, 4),
    )

    val result = (0 to (tokens.length - (windowSize + 1))).map { i =>
      tokens.slice(i, i + windowSize)
    }.toArray
    result should be(expectedWindows)
  }

  // Test 3
  it should "Test sliding window method 2" in {
    // Sample input data (tokens)
    val tokens = Array(1, 2, 3, 4, 5, 6)
    val windowSize = 4
    val expectedWindows = Array(
      Array(1, 2, 3, 4),
      Array(2, 3, 4, 5),
    )

    val result = (0 to (tokens.length - (windowSize + 1))).map { i =>
      tokens.slice(i, i + windowSize)
    }.toArray
    result should be(expectedWindows)
  }

  // Test 4
  it should "Test sliding window method 3" in {
    // Sample input data (tokens)
    val tokens = Array(1, 2, 3, 4)
    val windowSize = 4
    val expectedWindows = Array()

    val result = (0 to (tokens.length - (windowSize + 1))).map { i =>
      tokens.slice(i, i + windowSize)
    }.toArray
    result should be(expectedWindows)
  }

  // Test 5
  it should "Test sliding window method 4" in {
    // Sample input data (tokens)
    val tokens = Array(1, 2, 3, 4, 5, 6)
    val windowSize = 4
    val expectedLabels = Array(5, 6)

    val result = (0 to (tokens.length - (windowSize + 1))).map { i =>
      tokens(i + windowSize)
    }.toArray
    result should be(expectedLabels)
  }

  // Test 6
  it should "Test sliding window method 5" in {
    // Sample input data (tokens)
    val tokens = Array(1, 2, 3, 4)
    val windowSize = 4
    val expectedLabels = Array()

    val result = (0 to (tokens.length - (windowSize + 1))).map { i =>
      tokens(i + windowSize)
    }.toArray
    result should be(expectedLabels)
  }


}
