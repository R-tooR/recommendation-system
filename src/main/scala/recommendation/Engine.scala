package recommendation

import org.apache.commons.math3.linear.{Array2DRowRealMatrix, DiagonalMatrix, EigenDecomposition, RealMatrix}
import org.apache.spark.sql.Row
import properties.PropertiesNames
import utils.UtilFunctions.{addIfNotExist, collection2DToRealMatrix, euclides, joinColumns, normalizeArray, parseToIterable, sech0_5, zip4}

import java.util.Properties
import scala.collection.mutable
import scala.util.Try

class Engine(appProp: Properties = new Properties()) {

  private var stateChanged: Boolean = true
  val embeddingCalculator: EmbeddingCalculator = new EmbeddingCalculator
//  val embeddingUpdater: EmbeddingUpdater = new EmbeddingUpdater
  val topNEmbeddingsRatio: Double = appProp.getOrDefault(PropertiesNames.calculationTopNAttr, "0.2").asInstanceOf[String].toDouble
//  val topNEmbeddingsRatio: Double = appProp.computeIfAbsent(PropertiesNames.calculationTopNAttr, (x: Any) => 0.2).asInstanceOf[Double]

  {
    if(topNEmbeddingsRatio < 0.0 && topNEmbeddingsRatio > 1.0)
      throw new IllegalArgumentException("Ratio of embedding values taken to further calculations should be in range [0.0, 1.0].\n Check property " + PropertiesNames.calculationTopNAttr)
  }

  def createConsensusEmbedding(data: List[Array2DRowRealMatrix]) = {
    embeddingCalculator.createConsensusEmbedding(data)
  }


  def getTopNEmbeddings(embeddings: RealMatrix, dflist: List[RealMatrix]) = {

    val size = dflist.head.getColumnDimension
    val numOfAttributes = embeddings.getRowDimension
    val topNAttributes = (embeddings.getRowDimension * topNEmbeddingsRatio).toInt //przyciąć wszystkie do takiej długości, i porównać je
    val p0 = embeddings.getSubMatrix(0, numOfAttributes - 1, 0, size - 1).transpose() //column to : ile wartości
    val p1 = embeddings.getSubMatrix(0, numOfAttributes - 1, size, 2 * size - 1).transpose()

    val finalEmbeddings = List(dflist(0).multiply(p0).add(dflist(1).multiply(p1)))
    val allSimilarities = finalEmbeddings map (_.getData.map(row => normalizeArray(row.take(topNAttributes).tail)))
    val resultsPerEmbedding = allSimilarities map (m => m.map(row => m.map(row2 => euclides(row, row2))).head)
    val joinedResult = resultsPerEmbedding.head.indices.toArray map (x => resultsPerEmbedding map (arr => arr(x) * arr(x)) sum) map math.sqrt map sech0_5

    joinedResult
  }

  def getTopNStocks(similarities: Seq[Double], theirCompanies: Iterable[Row], evaluation: (Double, Double) = (0.0, 0.0)) = { // , recommendedRelevantStocks: mutable.Map[String, Double] = mutable.Map[String, Double]()
    val recommendedStocks = mutable.Map[String, Double]()
    val recommendedStocksCount = mutable.Map[String, Int]()

    theirCompanies zip similarities.tail foreach (x => {
      parseToIterable(x._1.get(0)).foreach(cp => addIfNotExist(cp, x._2, recommendedStocks))
    })
//    println("Similar stocks: " + recommendedStocks)
//7,0,"[181, 78, 57, 43, 72, 26, 141, 51, 180, 138, 36]","[5, 3, 12, 0]"
    val recommendedRelevantStocks = mutable.Map[String, Double]()
    if(evaluation._1 > 0.0 && evaluation._2 > 0.0) {
      (similarities.tail zip similarities.indices.toList)
        .sorted(Ordering.by[(Double, Int), Double](_._1).reverse)
        .take((similarities.size.toDouble * evaluation._1).toInt)
        .foreach(inv => {
          parseToIterable(theirCompanies.toVector(inv._2).get(0)).foreach(cp => addIfNotExist(cp, 1.0, recommendedRelevantStocks))
        })
        println("Relevant stocks: " + recommendedRelevantStocks.filter(x => x._2 >= evaluation._2))
    }

    println("Similar count: " + recommendedStocksCount)
    (recommendedStocks, recommendedRelevantStocks.filter(x => x._2 >= evaluation._2))
  }
}
