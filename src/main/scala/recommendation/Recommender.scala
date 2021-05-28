package recommendation

import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import investors.InvestorsDataProcessor
import org.apache.commons.math3.linear.Array2DRowRealMatrix

import java.util
import java.util.Properties

class Recommender(targetInvestor: Int) {
  private val dataExtractor = new DataExtractor(new Properties)
  private val investorsDataProcessor = new InvestorsDataProcessor
  private val engine = new Engine


  def step(stockRecommendations: util.Map[String, Double]): List[(String, Double)] = {
    val stocksByInvestors = findStocksBaseOnSimilarity(50, targetInvestor)
    val res = stocksByInvestors._1.map(x => (x._1, stockRecommendations.getOrDefault(x._1, 0.0) + x._2)).toList
      .sorted(Ordering.by[(String, Double), Double](_._2).reverse)

    return res
  }

  def findStocksBaseOnSimilarity(investorsN: Int, baseInvestor: Int, evaluation: (Double, Double) = (0.0, 0.0)) = {
    val normalize = (d: Double) => 1 - (1/((0.1*d) + 1))
    val surroundingInvestors = dataExtractor.get(new GetInvestorsQuery(baseInvestor))
    val targetInvestor = dataExtractor.get(new GetTargetInvestorQuery(baseInvestor))

    val data = investorsDataProcessor.get(surroundingInvestors.limit(investorsN), targetInvestor) map collection2DToRealMatrix

    val similarities = engine.getTopNEmbeddings(engine.createConsensusEmbedding(data), data)

    val similarStocks = engine.getTopNStocks(similarities, surroundingInvestors.select(GetInvestorsQuery.theirCompaniesMap).take(investorsN), evaluation)

    (similarStocks._1.map(entry => (entry._1, normalize(entry._2))), similarStocks._2)
  }

  def collection2DToRealMatrix(nested: Iterable[Iterable[Double]]): Array2DRowRealMatrix = {
    val doubleArray = nested map(iter => iter.toArray) toArray

    new Array2DRowRealMatrix(doubleArray)
  }
}
