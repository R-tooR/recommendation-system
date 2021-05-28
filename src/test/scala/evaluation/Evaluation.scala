package evaluation

import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import evaluation.queries.GetAllStocksFromUserInterestQuery
import investors.InvestorsDataProcessor
import moveItToDataServer.DatabaseUpdater
import org.scalatest.Inspectors.{forAll, forEvery}
import org.scalatest.matchers.should
import org.scalatest.propspec.AnyPropSpec
import recommendation.Recommender

import java.util.Properties
import scala.collection.immutable.ListMap

class Evaluation  extends AnyPropSpec with should.Matchers {

  property("Evaluation @10") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((10, 0), (10, 1), (10, 2), (10, 3), (10, 4), (10, 5))) { input =>
//      val extractor = new DataExtractor(new Properties())
//      val commonStocks = extractor.get(new GetAllStocksFromUserInterestQuery(0))
//        .collectAsList().get(0).getList(0).toArray.map(x => x.asInstanceOf[String])
//      val recommender: Recommender = new Recommender
//
//      val resultsCommon = recommender.findStocksBaseOnSimilarity(50, input._2, (0.4, 6.0))
//
//      val sortedResCommon = ListMap(resultsCommon._1.toSeq.sortWith(_._2 > _._2): _*).take(input._1)
//      println(sortedResCommon)
//      println(resultsCommon._2)
//      val relevantAndRetrievedIntersectionSize = sortedResCommon.keySet.intersect(resultsCommon._2.keySet).size
//      val retrievedSize = resultsCommon._1.take(input._1).size
//      val relevantSize = resultsCommon._2.take(input._1).size
//
//      println("Precision@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / retrievedSize))
//      println("Recall@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))
      val res = evaluation(input)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@10 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @25 init") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forAll(Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5))) { input =>
      val res = evaluation(input, relevancy = (0.5, 4.0))
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@25 -----")
    println("All precisions: " + precisions)
    println("All recalls: " + recalls)
    println("Average precisions: " + precisions.sum/precisions.size)
    println("Average recalls: " + recalls.sum/recalls.size)

  }

  property("Evaluation @25 update") {
    var precisions = Vector[Double]()
    var recalls = Vector[Double]()
    forEvery(Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5))) { input =>
      val res = evaluation(input, runs = 3)
      precisions = precisions :+ res._1
      recalls = recalls :+ res._2
    }

    println("----- Top@25 -----")
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

  def evaluation(input: (Int, Int), relevancy: (Double, Double) = (0.4, 6.0), runs: Int = 1) = {
    def run(runs: Int, recommender: Recommender, params: (Int, Int, (Double, Double))) = {
      val db = new DatabaseUpdater(0.1)
      db.initialize()
      for (i <- 0 until runs) {
        recommender.findStocksBaseOnSimilarity(params._1, params._2, params._3)
        db.update()
      }

      recommender.findStocksBaseOnSimilarity(params._1, params._2, params._3)
    }
    val recommender: Recommender = new Recommender(0)

    val resultsCommon = run(runs,recommender,(50, input._2, relevancy))
//    val resultsCommon = recommender.findStocksBaseOnSimilarity(50, input._2, relevancy)

    val sortedResCommon = ListMap(resultsCommon._1.toSeq.sortWith(_._2 > _._2): _*).take(input._1)
    println(sortedResCommon)
    println(resultsCommon._2)
    val relevantAndRetrievedIntersectionSize = sortedResCommon.keySet.intersect(resultsCommon._2.keySet).size
    val retrievedSize = resultsCommon._1.take(input._1).size
    val relevantSize = resultsCommon._2.take(input._1).size

    println("Precision@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / retrievedSize))
    println("Recall@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))

    ((relevantAndRetrievedIntersectionSize.toDouble / retrievedSize), (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))
  }

//  property("Evaluation 2") {
//    forEvery(EvaluationCases.top10) { input =>
////      val extractor = new DataExtractor(new Properties())
//      //      val commonStocks = extractor.get(new GetAllStocksFromUserInterestQuery(input.targetInvId))
//      //        .collectAsList().get(0).getList(0).toArray.map(x => x.asInstanceOf[String])
//      val recommender: Recommender = new Recommender
//
//      val resultsCommon = recommender.findStocksBaseOnSimilarity(input.numberOfInv, input.relevantParams)
//
//      val sortedResCommon = ListMap(resultsCommon._1.toSeq.sortWith(_._2 > _._2): _*).take(input.topN)
//      //      println(sortedResCommon)
//      //      println(resultsCommon._2)
//      val relevantAndRetrievedIntersectionSize = sortedResCommon.keySet.intersect(resultsCommon._2.keySet).size
//      val retrievedSize = resultsCommon._1.take(input.topN).size
//      val relevantSize = resultsCommon._2.take(input.topN).size
//
//      println("Precision@" + input.topN + " = " + (relevantAndRetrievedIntersectionSize.toDouble / retrievedSize))
//      println("Recall@" + input.topN + " = " + (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))
//    }
//  }
}
