import org.json.JSONObject
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object BedrockClientService {
  // logging setup
  private final val logger = LoggerFactory.getLogger(getClass)
  // config setup
  private val config = ConfigFactory.load("bedrock.conf")
  // retrieve configuration keys
  private val accessKey = config.getString("accessKey")
  private val secretKey = config.getString("secretKey")
  private val modelId = config.getString("modelId")

  // Set up AWS SDK client for Bedrock Runtime
  private def createClient(): BedrockRuntimeClient = {
    logger.info("Creating client")
    val credentials = AwsBasicCredentials.create(accessKey, secretKey)
    BedrockRuntimeClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .build()
  }

  // Invoke the model with a given input
  private def invokeModel(inputText: String): String = {
    val client = createClient()

    val jsonBody = new JSONObject()
      .put("message", inputText)

    // Convert the JSON body to a UTF-8 byte array and wrap it in SdkBytes
    val body = SdkBytes.fromUtf8String(jsonBody.toString())

    logger.info("Creating request")
    val request = InvokeModelRequest.builder()
      .modelId(modelId)
      .body(body)
      .contentType("application/json")
      .accept("*/*")
      .build()

    logger.info("Invoking model")
    val response = client.invokeModel(request)
    val responseJSON = new JSONObject(response.body().asUtf8String()) // Convert the byte array to a String

    responseJSON.getString("text")
  }

  // Lambda handler
  def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): String = {
    logger.info(s"Received event: $event")

    // Extract the body from the request
    val requestBody = event.getBody
    if (requestBody == null || requestBody.isEmpty) {
      logger.info("Received empty body")
      return "Error: Empty request body"
    }

    // There is a body in the request
    // invoke the model on the request body as query.
    val response = invokeModel(requestBody)
    logger.info("response returning")
    response
  }
}
