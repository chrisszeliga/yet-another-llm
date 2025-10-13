import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFlatSpec with Matchers {

  private val localConfig = ConfigFactory.load("local.conf")
  private val awsConfig = ConfigFactory.load("aws.conf")
  private val bedrockConfig = ConfigFactory.load("bedrock.conf")

  // local config tests
  it should "test host in local config" in {
    localConfig.getString("host") should be ("localhost")
  }

  it should "test port in local config" in {
    localConfig.getString("port") should be ("8080")
  }

  it should "test apiGatewayUrl in local config" in {
    localConfig.getString("apiGatewayUrl") should be ("https://8z0askzw72.execute-api.us-east-1.amazonaws.com/dev/generate-text")
  }

  // aws config tests
  it should "test host in aws config" in {
    awsConfig.getString("host") should be ("0.0.0.0")
  }

  it should "test port in aws config" in {
    awsConfig.getString("port") should be ("8080")
  }

  it should "test apiGatewayUrl in aws config" in {
    awsConfig.getString("apiGatewayUrl") should be ("https://8z0askzw72.execute-api.us-east-1.amazonaws.com/dev/generate-text")
  }

  // bedrock config tests
  it should "test accessKey in bedrock config" in {
    bedrockConfig.getString("accessKey") should be ("AKIAST6S63WNU6ECTLOO")
  }

  it should "test modelId in bedrock config" in {
    bedrockConfig.getString("modelId") should be ("cohere.command-r-v1:0")
  }

}