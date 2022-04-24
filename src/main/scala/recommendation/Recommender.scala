package recommendation

import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import investors.InvestorsDataProcessor
import properties.PropertiesNames.{recommenderTopN}
import org.apache.commons.math3.linear.RealMatrix
import utils.UtilFunctions.collection2DToRealMatrix

import java.util
import java.util.Properties
import scala.util.Try

class Recommender(targetInvestor: Int, appConf: Properties = new Properties()) {
  private val recommenderTopNDefault : Int = 50
  private val dataExtractor = new DataExtractor(appConf)
  private val investorsDataProcessor = new InvestorsDataProcessor
  private val engine = new Engine(appConf)


  def step(stockRecommendations: util.Map[String, Double]): List[(String, Double)] = {
    val topN = Try {
      Integer.parseInt(appConf.getProperty(recommenderTopN))
    }

    val stocksByInvestors = findStocksBaseOnSimilarity(topN.getOrElse(recommenderTopNDefault), targetInvestor)

    stocksByInvestors._1.map(x => (x._1, stockRecommendations.getOrDefault(x._1, 0.0) + x._2)).toList
      .sorted(Ordering.by[(String, Double), Double](_._2).reverse)
  }

  def findStocksBaseOnSimilarity(investorsN: Int, baseInvestor: Int, evaluation: (Double, Double) = (0.0, 0.0)) = {
    val normalize = (d: Double) => 1 - (1/((0.1*d) + 1))
    val surroundingInvestors = dataExtractor.get(new GetInvestorsQuery(baseInvestor))
    val targetInvestor = dataExtractor.get(new GetTargetInvestorQuery(baseInvestor))

    val data = investorsDataProcessor.get(surroundingInvestors.limit(investorsN), targetInvestor) map collection2DToRealMatrix

    val stocks = (matrix: RealMatrix) => {
      val similarities = engine.getTopNEmbeddings(matrix, data)
      val similarStocks = engine.getTopNStocks(similarities, surroundingInvestors.select(GetInvestorsQuery.theirCompaniesMap).take(investorsN), evaluation)

      (similarStocks._1.map(entry => (entry._1, normalize(entry._2))), similarStocks._2)
    }

    engine.createConsensusEmbedding(data) match {
      case Right(matrix) => stocks(matrix)
      case Left(error) =>
        println(error)
        (Map[String, Double](), Map[String, Double]())
    }
  }

}
