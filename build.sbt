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

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % Test