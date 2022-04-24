package evaluation

import com.fasterxml.jackson.databind.ObjectMapper
import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import investors.InvestorsDataProcessor
import org.scalatest.Inspectors.forEvery
import org.scalatest.matchers.should
import org.scalatest.propspec.AnyPropSpec
import recommendation.Recommender
import updater.DatabaseUpdater

import java.io.File
import java.util
import java.util.Properties
import scala.beans.BeanProperty
import scala.collection.immutable.ListMap

class GenericEvaluationFileGenerator extends AnyPropSpec with should.Matchers {
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


  case class EvaluationCase(
                             @BeanProperty val amount: String,
                             @BeanProperty val precisions: Array[Double],
                             @BeanProperty val recalls: Array[Double],
                             @BeanProperty val avgPrecision: Double,
                             @BeanProperty val avgRecall: Double
                           )

  val om = new ObjectMapper()
  val evaluationResults = new util.ArrayList[EvaluationCase]()
  property("Evaluation ALL") {

    val seqInvs = Seq(
      Seq((10, 0), (10, 1), (10, 2), (10, 3), (10, 4), (10, 5)),
      Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5)),
      Seq((50, 0), (50, 1), (50, 2), (50, 3), (50, 4), (50, 5)),
    )
    for(seqInv <- seqInvs) {
      var allAvgPrecisions = Vector[Double]()
      var allAvgRecalls = Vector[Double]()
      forEvery(Seq((0.3, 7.0), (0.4, 7.0), (0.5, 7.0), (0.3, 6.0), (0.4, 6.0), (0.5, 6.0), (0.3, 5.0), (0.4, 5.0), (0.5, 5.0), (0.3, 4.0), (0.4, 4.0), (0.5, 4.0), (0.3, 3.0), (0.4, 3.0), (0.5, 3.0))) { input =>
        var precisions = Vector[Double]()
        var recalls = Vector[Double]()
        for (in <- seqInv) {
          val res = evaluation(in, input)
          if (res._1 != 0.0) {
            precisions = precisions :+ res._1
            recalls = recalls :+ res._2
          }
        }
        precisions = precisions.filter(res => res != Double.NaN)
        recalls = recalls.filter(res => res != Double.NaN)
        allAvgPrecisions = allAvgPrecisions :+ precisions.sum / precisions.size
        allAvgRecalls = allAvgRecalls :+ recalls.sum / recalls.size
      }

      val evalRes = EvaluationCase("Top@"+seqInv.head._1, allAvgPrecisions.toArray, allAvgRecalls.toArray,
        avgPrecision = allAvgPrecisions.sum / allAvgPrecisions.size, avgRecall = allAvgRecalls.sum / allAvgRecalls.size)
      evaluationResults.add(evalRes)
      om.writeValue(new File("evaluationResults.json"), evaluationResults)

    }
  }

  case class InputData(
                        @BeanProperty topN: String,
                        @BeanProperty data: Array[Array[Array[Double]]]
                      )
  property("create cases for comaprision"){
    val seqInvs = Seq(
      Seq((10, 0), (10, 1), (10, 2), (10, 3), (10, 4), (10, 5)),
      Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5)),
      Seq((50, 0), (50, 1), (50, 2), (50, 3), (50, 4), (50, 5)),
    )
    var all = new util.ArrayList[InputData]()
    for(seqInv <- seqInvs) {
      var dataList = Array[Array[Array[Double]]]()
      forEvery(seqInv) { input =>
        val dataExtractor = new DataExtractor(new Properties())
        val surroundingInvestors = dataExtractor.get(new GetInvestorsQuery(input._2))
        val targetInvestor = dataExtractor.get(new GetTargetInvestorQuery(input._2))
        val investorsDataProcessor = new InvestorsDataProcessor

        val data = investorsDataProcessor.get(surroundingInvestors.limit(input._1), targetInvestor) map (n => n.map(iter => iter.toArray).toArray)
        val concatenated = Array.concat(data(0), data(1))
        dataList = dataList :+ concatenated
      }
      val cases = InputData("@" + seqInv.head._1, dataList)
      all.add(cases)
    }

    om.writeValue(new File("testCases.json"), all)

  }

  case class CaseStockList(
                            @BeanProperty baseId: Int,
                            @BeanProperty stocks: Array[Array[String]]
                          )

  case class StocksList(
                         @BeanProperty topN: String,
                         @BeanProperty lists: util.ArrayList[CaseStockList]
                       )
  property("stocks list"){
    val seqInvs = Seq(
      Seq((10, 0), (10, 1), (10, 2), (10, 3), (10, 4), (10, 5)),
      Seq((25, 0), (25, 1), (25, 2), (25, 3), (25, 4), (25, 5)),
      Seq((50, 0), (50, 1), (50, 2), (50, 3), (50, 4), (50, 5)),
    )
    var all = new util.ArrayList[StocksList]()
    for(seqInv <- seqInvs) {
      var dataList = new util.ArrayList[CaseStockList]()
      forEvery(seqInv) { input =>
        val dataExtractor = new DataExtractor(new Properties())
        val surroundingInvestors = dataExtractor.get(new GetInvestorsQuery(input._2))
        val targetInvestor = dataExtractor.get(new GetTargetInvestorQuery(input._2))
        val investorsDataProcessor = new InvestorsDataProcessor

        val stocks = investorsDataProcessor.getInvestorStocks(surroundingInvestors.limit(input._1), targetInvestor)
        val e = CaseStockList(input._2, stocks.get.map(e => e.toArray).toArray)
        dataList.add(e)
      }
      all.add(StocksList("@" + seqInv.head._1, dataList))
    }
    om.writeValue(new File("stockList.json"), all)
  }


}
