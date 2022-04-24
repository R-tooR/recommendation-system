package data

import data.queries.Query
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}
import properties.PropertiesNames.{appLogging, appMaster, appName, dbPassword, dbUrl, dbUsername}

import java.util.Properties

class DataExtractor(val props: Properties) {
  // http://spark.apache.org/docs/latest/configuration org.neo4j.html#configuring-logging
  // http://spark.apache.org/docs/latest/configuration.html
  private val config: SparkConf = new SparkConf().setAppName(props.getProperty(appName, "My App"))
    .setMaster(props.getProperty(appMaster, "local"))
    .set("spark.logConf", props.getProperty(appLogging, "true"))

  private val spark: SparkSession = SparkSession.builder().config(config).getOrCreate()

  private val connection: DataFrameReader = spark.read.format("org.neo4j.spark.DataSource")
    .option("url", props.getProperty(dbUrl, "bolt://localhost:7687"))
    .option("authentication.basic.username", props.getProperty(dbUsername, "neo4j"))
    .option("authentication.basic.password", props.getProperty(dbPassword, "inv"))


  def get(query: Query): DataFrame = {
    connection
      .option("query", query.get)
      .load()
  }
}
