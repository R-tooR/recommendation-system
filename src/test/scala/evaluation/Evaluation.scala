package evaluation

import com.fasterxml.jackson.databind.ObjectMapper
import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import evaluation.queries.GetAllStocksFromUserInterestQuery
import evaluation.EvaluationCommons.evaluation
import investors.InvestorsDataProcessor
import updater.DatabaseUpdater
import org.apache.spark
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.Inspectors.{forAll, forEvery}
import org.scalatest.matchers.should
import org.scalatest.propspec.AnyPropSpec
import recommendation.Recommender
import utils.UtilFunctions

import java.io.File
import java.util
import java.util.Properties
import scala.beans.BeanProperty
import scala.collection.immutable.ListMap
import scala.util.parsing.json

class Evaluation  extends AnyPropSpec with should.Matchers {

  property("Evaluation @25 init") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forAll(Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5))) { input =>
      val res = evaluation(input, relevancy = (0.45, 5.0))
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@25 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    precisions = precisions.filter(res => res != Double.NaN)
    recalls = recalls.filter(res => res != Double.NaN)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @25 update") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forAll(Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5))) { input =>
//    forEvery(Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5))) { input =>
      val res = evaluation(input, runs = 3, relevancy = (0.5, 4.0))
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
//      Thread.sleep(100)
    }

    println("----- Top@25 update -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @50 init") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((50, 0), (50, 1), (50, 2), (50, 3), (50, 4), (50, 5))) { input =>
      val res = evaluation(input)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@50 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @50 update") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((50, 0), (50, 1), (50, 2), (50, 3), (50, 4), (50, 5))) { input =>
      val res = evaluation(input, runs = 3)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@50 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @100 init") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((100, 0), (100, 1), (100, 2), (100, 3), (100, 4), (100, 5))) { input =>
      val res = evaluation(input)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@100 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @100 update") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((100, 0), (100, 1), (100, 2), (100, 3), (100, 4), (100, 5))) { input =>
      val res = evaluation(input, runs = 3)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@100 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }




  def evaluationFromFile(path1: String, path2: String, relevancy: (Double, Double) = (0.4, 6.0)) = {
    val config: SparkConf = new SparkConf().setAppName("My App")
      .setMaster("local")
      .set("spark.logConf", "true")
    val ses = SparkSession.builder().config(config).getOrCreate()
    val embeddings = ses.read.json(path1)
    val companiesData = ses.read.csv(path2)

    println(embeddings)
    println(companiesData)
  }

  property("test") {
    evaluationFromFile("C:\\Users\\artur\\Desktop\\exportCorrect.csv", "C:\\Users\\artur\\Desktop\\deepwalk.json")
  }
}
