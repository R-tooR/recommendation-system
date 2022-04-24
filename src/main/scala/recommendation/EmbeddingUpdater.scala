package recommendation

import org.apache.commons.math3.linear.{Array2DRowRealMatrix, RealMatrix}
import utils.UtilFunctions.{joinColumns, zip4}

class EmbeddingUpdater {
  def updateEigenValuesAndCorrespondingVectors(diagonalSubtract: List[RealMatrix], laplacianSubtract: List[RealMatrix], eigenValues: List[Array[Double]], eigenVectors: List[RealMatrix]) = {

    val correspondingValuesVectors = zip4(diagonalSubtract, laplacianSubtract, eigenValues, eigenVectors)

    val updatedEigenValuesVectors = correspondingValuesVectors.map(x => update(x._1, x._2, x._3, x._4))
    updatedEigenValuesVectors toList
  }

  def update(diagonalSubtract: RealMatrix, laplacianSubtract: RealMatrix, values: Array[Double], vectors: RealMatrix): (Array[Double], RealMatrix) = {
    val vectorsMatrixNonNormalized = for(v <- Seq.range(0, vectors.getColumnDimension)) yield new Array2DRowRealMatrix(vectors.getColumn(v))
    val vectorsMatrix = vectorsMatrixNonNormalized.map(vec => vec.scalarMultiply(1/vec.getNorm))
    val multiplyWithV1V2 = (a1: RealMatrix, LD: RealMatrix, a2: RealMatrix) => a1.transpose().multiply(LD).multiply(a2)
    val multiplyWithVVT = (a: RealMatrix, LD: RealMatrix) => multiplyWithV1V2(a, LD, a)

    val updatedValues = values.indices.toList.map(i => {
      multiplyWithVVT(vectorsMatrix(i), laplacianSubtract)
        .subtract(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).scalarMultiply(values(i))).scalarAdd(values(i))}) map (_.getEntry(0,0))

    val updatedVectors = vectorsMatrix.indices.toList.map(i => vectorsMatrix(i).add(
      vectorsMatrix(i)
        .scalarMultiply(-0.5)
        .scalarMultiply(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).getEntry(0, 0))
        .add(
          vectorsMatrix.indices.toList.filter(j => j != i).map(j =>
          {

            vectorsMatrix(j).multiply(multiplyWithV1V2(vectorsMatrix(j), laplacianSubtract, vectorsMatrix(i))
              .subtract(multiplyWithV1V2(vectorsMatrix(i), diagonalSubtract, vectorsMatrix(j))
                .scalarMultiply(values(i))).scalarMultiply(1 / (updatedValues(i) - updatedValues(j)))
            )}).reduce((m1, m2) => m1.add(m2))
        )
      )
    )

    (updatedValues.toArray, joinColumns(updatedVectors))
  }

}
