ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "Homework3"
  )

// Akka
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.6"

// AWS Lambda Java SDK
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"

// HTTP client for sending requests
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.14"

// AWS
libraryDependencies += "software.amazon.awssdk" % "bedrock" % "2.26.22"
libraryDependencies += "software.amazon.awssdk" % "core" % "2.26.25"
libraryDependencies += "software.amazon.awssdk" % "auth" % "2.25.27"
libraryDependencies += "software.amazon.awssdk" % "bedrockruntime" % "2.25.52"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "3.12.0"

// JSON
libraryDependencies += "org.json" % "json" % "20240303"

// SLF4J
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.16"

// Logback
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"

// Typesafe Configuration Library
libraryDependencies += "com.typesafe" % "config" % "1.4.3"

// Scalatest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

// gRPC / protobuf
libraryDependencies += "io.grpc" % "grpc-netty" % "1.65.1"
libraryDependencies += "io.grpc" % "grpc-netty" % "1.65.1"
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.65.1"
libraryDependencies += "io.grpc" % "grpc-stub" % "1.64.0"
libraryDependencies += "com.google.protobuf" % "protobuf-java" % "4.27.1"
