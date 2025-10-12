ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) =>
    xs match {
      case "MANIFEST.MF" :: Nil => MergeStrategy.discard
      case "services" :: _ => MergeStrategy.concat
      case _ => MergeStrategy.discard
    }
  case "reference.conf" => MergeStrategy.concat
  case x if x.endsWith(".proto") => MergeStrategy.rename
  case x if x.contains("hadoop") => MergeStrategy.first
  case _ => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "Homework2",
    assembly / mainClass := Some("Main"),
    assembly / assemblyJarName := "LLMTraining.jar"
  )

fork := true
javaOptions in Global += "-Xms2G"
javaOptions in Global += "-Xmx8G"

// Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.3"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.3"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.5.3"

// Jtokkit
libraryDependencies += "com.knuddels" % "jtokkit" % "1.1.0"

// Deeplearning4j
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" % "dl4j-spark_2.12" % "1.0.0-M2.1"

// SLF4J
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.16"

// Logback
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"

// Typesafe Configuration Library
libraryDependencies += "com.typesafe" % "config" % "1.4.3"

// Scalatest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.6"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.12.765"
