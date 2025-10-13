import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.headers._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends App {
  // logging setup
  private final val logger = LoggerFactory.getLogger(getClass)
  // config setup
  private val config = ConfigFactory.load("local.conf")
//  private val config = ConfigFactory.load("aws.conf")

  // retrieve configuration keys
  private val host = config.getString("host")
  private val port = config.getString("port").toInt
  private val apiGatewayUrl = config.getString("apiGatewayUrl")

  // Akka setup
  implicit val system: ActorSystem = ActorSystem("akka-http-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Define the route "generate-text" as post
  val route =
    path("generate-text") {
      post {
        entity(as[String]) { query =>
          logger.info(s"Received query: $query")

          // Forward the query to the API Gateway, which calls Lambda which calls Bedrock
          val responseFuture: Future[HttpResponse] = callApiGateway(query)
          logger.info("Query to API Gateway")

          onComplete(responseFuture) {
            case Success(response) =>
              // Response is successful, return response
              logger.info("Response success")
              complete(response)
            case Failure(exception) =>
              // Something went wrong, return an error
              logger.info("Response error")
              complete(StatusCodes.InternalServerError, s"Error: ${exception.getMessage}")
          }
        }
      }
    }

  // Start the Akka HTTP server
  //  Http().newServerAt("localhost", 8080).bind(route)
  Http().newServerAt(host, port).bind(route)
  logger.info("Server running")

  // Helper function to make the HTTP request to the API Gateway
  private def callApiGateway(query: String): Future[HttpResponse] = {

    // Create the request payload
    // * "body" required
    val requestEntity = HttpEntity(
      ContentTypes.`application/json`,
      s"""{"body": "$query"}"""
    )

    // Create the HTTP request
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = apiGatewayUrl,
      entity = requestEntity,
      headers = List(
        `Content-Type`(ContentTypes.`application/json`)
      )
    )

    // Send the request and return the response as future
    Http().singleRequest(request)
  }
}