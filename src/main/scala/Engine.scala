import Test.collection2DToRealMatrix
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, EigenDecomposition, RealMatrix, RealVector}
import org.apache.spark.sql.{DataFrame, Row}

import scala.collection.mutable
import scala.util.Try

class Engine {
  def createConsensusEmbedding(dflist: List[RealMatrix]): RealMatrix = {
    val listSize = dflist.size
    val isQuadraticMatrix: PartialFunction[RealMatrix, Int] = {
      case x if x.getColumnDimension == x.getRowDimension => x.getRowDimension
    }

    val matrixSize = Try.apply(dflist collectFirst isQuadraticMatrix getOrElse 0).get
    val consensusEmbedding = new Array2DRowRealMatrix(listSize*matrixSize, listSize*matrixSize)

    var i = 0
    var j = 0
    for ( m1 <- dflist){
      j=0
      for (m2 <- dflist){
        consensusEmbedding.setSubMatrix(m2.multiply(m1.transpose()).getData, i*matrixSize, j*matrixSize)
        j=j+1
      }
      i=i+1
    }

    consensusEmbedding
  }

  def calculateConsensusEmbedding(consensusEmbedding: RealMatrix): Array2DRowRealMatrix = {
    val result = new EigenDecomposition(consensusEmbedding)
    val eigenValues = result.getRealEigenvalues filter(x => x > 0) //czy można wykluczyć ujemne?
    val eigenVectors = Seq.range(0, eigenValues.length) map(x => result.getEigenvector(x))

    collection2DToRealMatrix(eigenVectors map(row => row.toArray.toIterable) toIterable)
  }


  def getTopNEmbeddings(embeddings: Array2DRowRealMatrix, dflist: List[RealMatrix]) = {
//    val distance: PartialFunction[RealVector, Seq[Double]] = {
//      case x => embeddings map(row2 => x.getDistance(row2))
//    }
//    val p = embeddings map(col => List(col.getSubVector(0, size), col.getSubVector(size, 2*size), col.getSubVector(2*size, 3*size)))
//    val p0 = embeddings map(col => col.getSubVector(0, size))
//    val p1 = embeddings map(col => col.getSubVector(size, 2*size))
//    val p2 = embeddings map(col => col.getSubVector(2*size, 3*size))
//    val targetUserDistances = embeddings collectFirst distance get
//    val t = targetUserDistances map sech
//    val similarities = embeddings map(row => embeddings map(row2 => row.getDistance(row2)))
//    println(targetUserDistances)
//    println(t)
//    t // wszystko zwraca 1.4142 pierwuastek z 2 dlaczego ??!!

//    assert(dflist.size == 3)
//    assert(embeddings.getColumnDimension == dflist.head.getColumnDimension)
//    assert(embeddings.getRowDimension == 3*dflist.head.getRowDimension)

    val size = dflist.head.getColumnDimension
    val numOfAttributes = embeddings.getRowDimension
    val p0 = embeddings.getSubMatrix(0, numOfAttributes-1, 0, size-1).transpose() //column to : ile wartości
    val p1 = embeddings.getSubMatrix(0, numOfAttributes-1, size, 2*size-1).transpose()
    val p2 = embeddings.getSubMatrix(0, numOfAttributes-1, 2*size, 3*size-1).transpose()
    val sech0_5 = (x: Double) => 2/(Math.exp(0.5*x) + Math.exp(-0.5*x))

    val euclides = (x: Iterable[Double], y: Iterable[Double]) => math.sqrt(((for((a, b) <- x zip y) yield (a-b) * (a-b)) sum))

    val finalEmbeddings = List(dflist(1).multiply(p2).subtract(dflist(2).multiply(p1)),
      dflist(2).multiply(p0).subtract(dflist(0).multiply(p2)),
      dflist(0).multiply(p1).subtract(dflist(1).multiply(p0)),
    )

    val allSimilarities = finalEmbeddings map(_.getData)
    val resultsPerEmbedding = allSimilarities map(m => m.map(row => m.map(row2 => euclides(row, row2))).head)
//    resultsPerEmbedding foreach (x => x foreach( y => print(y + " ")))
    val joinedResult = (0 until resultsPerEmbedding(0).size).toArray map(x => resultsPerEmbedding map(arr => arr(x)*arr(x)) sum) map math.sqrt map sech0_5

//    println(joinedResult)
    joinedResult
  }

  def getTopNStocks(similarities: Seq[Double], theirCompanies: List[Row]) = {
    val recommendedStocks = mutable.Map[String, Double]()
    def addIfNotExist(key: String, value: Double) = {
      if(recommendedStocks contains key){
        recommendedStocks(key) += value
      } else {
        recommendedStocks(key) = value
      }
    }
    theirCompanies zip similarities foreach( x => x._1.getList(0).forEach(cp => addIfNotExist(cp, x._2)))

    recommendedStocks
  }
}
