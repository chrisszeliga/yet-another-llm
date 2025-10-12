ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

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
    name := "Homework1",
    assembly / mainClass := Some("main"),
    assembly / assemblyJarName := "mapreduce.jar"
  )

fork := true
javaOptions in Global += "-Xms2G"
javaOptions in Global += "-Xmx8G"

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.6"
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-mapreduce-client-core
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6"
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.6"
// Jtokkit
libraryDependencies += "com.knuddels" % "jtokkit" % "1.1.0"
// Deeplearning4j
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1"
// SLF4J
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.16"
// Logback
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"
// Typesafe Configuration Library
libraryDependencies += "com.typesafe" % "config" % "1.4.3"
// Scalatest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"