package recommendation

import org.apache.commons.math3.linear.{Array2DRowRealMatrix, DiagonalMatrix, EigenDecomposition, RealMatrix}
import utils.UtilFunctions.collection2DToRealMatrix

import scala.util.Try

class EmbeddingCalculator {

  var previousDfList = List.empty[RealMatrix]
  var previousDiagonal = List.empty[RealMatrix]
  var previousLaplacian = List.empty[RealMatrix]
  private var initialization = true
  private var eigenValues: List[Array[Double]] = List.empty[Array[Double]]
  private var eigenVectors: List[RealMatrix] = List.empty[RealMatrix]
  private var consensusEmbeddingResult: RealMatrix = new Array2DRowRealMatrix()

  val embeddingUpdater: EmbeddingUpdater = new EmbeddingUpdater

  def createConsensusEmbedding(dflist: List[RealMatrix]): Either[String, RealMatrix] = {
    val listSize = dflist.size
    val isQuadraticMatrix: PartialFunction[RealMatrix, Int] = {
      case x if x.getColumnDimension == x.getRowDimension => x.getRowDimension
    }

    val matrixSize = Try.apply(dflist collectFirst isQuadraticMatrix getOrElse 0).get
    val consensusEmbedding = new Array2DRowRealMatrix(listSize * matrixSize, listSize * matrixSize)

    if(dflist.nonEmpty && dflist.head.getRowDimension > 1) {
      val diagonal = for (m <- dflist) yield new DiagonalMatrix(m.getData.map(row => row.sum))
      val laplacian = for ((diag, df) <- diagonal zip dflist) yield df.subtract(diag).scalarMultiply(-1) //diag.subtract(df)
      val intermediateEmbeddings = createMatrixOfIntermediateEmbeddings(dflist, diagonal, laplacian)
      var i = 0
      var j = 0
      for (m1 <- intermediateEmbeddings) {
        j = 0
        for (m2 <- intermediateEmbeddings) {
          consensusEmbedding.setSubMatrix(m2.multiply(m1.transpose()).getData, i * matrixSize, j * matrixSize)
          j = j + 1
        }
        i = i + 1
      }

      previousDfList = dflist
      previousDiagonal = diagonal
      previousLaplacian = laplacian
      consensusEmbeddingResult = calculateConsensusEmbedding(consensusEmbedding)._1

      Right(consensusEmbeddingResult)
    } else {
      Left("Cannot create recommendation due to lack of neighbours.")
    }
  }

  def createMatrixOfIntermediateEmbeddings(dflist: List[RealMatrix], diagonal: List[DiagonalMatrix], laplacian: List[RealMatrix]) = {
    //      if (initialization) {

    def normalize(diagonal: DiagonalMatrix, laplacian: RealMatrix): RealMatrix = {
      new Array2DRowRealMatrix(
        laplacian.getData zip Seq.range(0, laplacian.getRowDimension) map (row => row._1.map(d => d/diagonal.getEntry(row._2, row._2)))
      )
    }

    val intermediate = diagonal zip laplacian map(x => normalize(x._1, x._2)) map calculateConsensusEmbedding
    val intermediateEmbeddings = intermediate map (x => x._1)
    eigenValues = intermediate map (x => x._2)
    eigenVectors = intermediateEmbeddings

    initialization = false
    intermediateEmbeddings
    //      } else {
    //        val result = updateEigenValuesAndCorrespondingVectors(
    //          for ((prev, curr) <- previousDiagonal zip diagonal) yield curr.subtract(prev),
    //          for ((prev, curr) <- previousLaplacian zip laplacian) yield curr.subtract(prev),
    //          eigenValues,
    //          eigenVectors
    //        )
    //
    //        stateChanged = eigenValues.zip(result map (x => x._1)) exists (x => (x._1 zip x._2 map (y => y._1 - y._2) sum) > 0.0001)
    //
    //        eigenValues = result map (x => x._1)
    //        eigenVectors = result map (x => x._2)
    //
    //        eigenVectors
    //      }
  }

  def calculateConsensusEmbedding(consensusEmbedding: RealMatrix) = {

    val result = new EigenDecomposition(consensusEmbedding)
    val eigenValues = result.getRealEigenvalues
    val eigenVectors = Seq.range(0, eigenValues.length) map (x => result.getEigenvector(x))

    (collection2DToRealMatrix(eigenVectors map (row => row.toArray.toIterable) toIterable), eigenValues)
  }

  def updateEigenValuesAndCorrespondingVectors(diagonalSubtract: List[RealMatrix], laplacianSubtract: List[RealMatrix], eigenValues: List[Array[Double]], eigenVectors: List[RealMatrix]) = {
    embeddingUpdater.updateEigenValuesAndCorrespondingVectors(diagonalSubtract, laplacianSubtract, eigenValues, eigenVectors)
  }
}
