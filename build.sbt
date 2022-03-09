name := "recommendation-system"

version := "0.1"

scalaVersion := "2.12.13"

resolvers += ("Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven").withAllowInsecureProtocol(true)
resolvers += ("MavenRepository" at "https://mvnrepository.com")

libraryDependencies += "neo4j-contrib" % "neo4j-connector-apache-spark_2.12" % "4.0.1_for_spark_3"
// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.0.0"
libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.0.0"
libraryDependencies += "org.apache.spark" % "spark-mllib_2.12" % "3.0.0"

//libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
//libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.0.2"

// do data-provider
libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "1.0.4"

//log
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"

val AkkaVersion = "2.6.18"
//libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
//libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion
