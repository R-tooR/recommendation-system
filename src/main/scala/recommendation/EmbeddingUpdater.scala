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
    val eps = 0.000001

    //      println()
    //      println("Values before : " + values.mkString("Array(", ", ", ")"))
    //      println("Vectors before : " + vectors)
    //      println("Vectors max: " + vectors.getData.max(Ordering.by[Array[Double], Double](_.max)).mkString("Array(", ", ", ")"))
    //      println("Vectors min: " + vectors.getData.min(Ordering.by[Array[Double], Double](_.min)).mkString("Array(", ", ", ")"))
    //
    //      println()

    //      val updVals = values.indices.toList.map(i => multiplyWithVVT(vectorsMatrix(i), laplacianSubtract)
    //        .subtract(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).scalarMultiply(values(i))))

    //omijamy pierwszą wartość własną, zawsze równą 0
    val updatedValues = values.indices.toList.map(i => {
      //        println("(" + i + ") a L a: " +  multiplyWithVVT(vectorsMatrix(i), laplacianSubtract))
      //        println("(" + i + ") a D a: " + multiplyWithVVT(vectorsMatrix(i), diagonalSubtract))
      //        println("(" + i + ") lambda a D a: " + multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).scalarMultiply(values(i)))
      //        println("(" + i + ") a L a - lambda a D a: " + multiplyWithVVT(vectorsMatrix(i), laplacianSubtract)
      //          .subtract(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).scalarMultiply(values(i))))
      multiplyWithVVT(vectorsMatrix(i), laplacianSubtract)
        .subtract(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).scalarMultiply(values(i))).scalarAdd(values(i))}) map (_.getEntry(0,0))

    val uniqueEigenValuesIndexes = (updatedValues zip updatedValues.indices) filter(x => math.abs(x._1 - 1.0) > eps) map(_._2)
    //      println("Unique eigen values indexes: " + uniqueEigenValuesIndexes.mkString("Array(", ", ", ")"))

    //      for (v <- vectorsMatrix) {
    //        println(" Norm: " + v.getNorm)
    //      }
    val updatedVectors = vectorsMatrix.indices.toList.map(i => vectorsMatrix(i).add(
      //      val updatedVectors = uniqueEigenValuesIndexes.map(i => vectorsMatrix(i).add(
      vectorsMatrix(i)
        .scalarMultiply(-0.5)
        .scalarMultiply(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).getEntry(0, 0))
        .add(
          vectorsMatrix.indices.toList.filter(j => j != i).map(j =>
            //            uniqueEigenValuesIndexes.filter(j => j != i).map(j =>
          {
            //                println("values(" + j + ")" + values(j))
            //                println("values(j)" + values(j))

            //                println("(updatedValues(" + i + ") - updatedValues(" + j + ")" + (updatedValues(i) - updatedValues(j)))
            //
            //                println("a_j " + vectorsMatrix(i))
            //                println("a_i " + vectorsMatrix(j))
            //                println("L_A " + laplacianSubtract)
            //                println("D_A " + diagonalSubtract)
            //
            //                println("a_j L_A a_i " + multiplyWithV1V2(vectorsMatrix(i), laplacianSubtract, vectorsMatrix(j)))
            //                println("a_j D_A a_i " + multiplyWithV1V2(vectorsMatrix(i), diagonalSubtract, vectorsMatrix(j)))

            vectorsMatrix(j).multiply(multiplyWithV1V2(vectorsMatrix(j), laplacianSubtract, vectorsMatrix(i)) //było multiply
              .subtract(multiplyWithV1V2(vectorsMatrix(i), diagonalSubtract, vectorsMatrix(j))
                //                  .scalarMultiply(values(i))).scalarMultiply(1 / (values(i) - values(j)))
                .scalarMultiply(values(i))).scalarMultiply(1 / (updatedValues(i) - updatedValues(j)))
            )}).reduce((m1, m2) => m1.add(m2))
        )
    )
    )

    //      val updVecs = vectorsMatrix.indices.toList.map(i =>
    //        vectorsMatrix(i)
    //          .scalarMultiply(-0.5)
    //          .scalarMultiply(multiplyWithVVT(vectorsMatrix(i), diagonalSubtract).getEntry(0, 0))
    //          .add(
    //            vectorsMatrix.indices.toList.filter(j => j != i).map(j =>
    //            {
    //              //                println("values(i)" + values(i))
    //              //                println("values(j)" + values(j))
    //              //
    //              //                println("a_j " + vectorsMatrix(i))
    //              //                println("a_i " + vectorsMatrix(j))
    //              //                println("L_A " + laplacianSubtract)
    //              //                println("D_A " + diagonalSubtract)
    //              //
    //              //                println("a_j L_A a_i " + multiplyWithV1V2(vectorsMatrix(i), laplacianSubtract, vectorsMatrix(j)))
    //              //                println("a_j D_A a_i " + multiplyWithV1V2(vectorsMatrix(i), diagonalSubtract, vectorsMatrix(j)))
    //
    //              vectorsMatrix(j).multiply(multiplyWithV1V2(vectorsMatrix(j), laplacianSubtract, vectorsMatrix(i)) //było multiply
    //                .subtract(multiplyWithV1V2(vectorsMatrix(i), diagonalSubtract, vectorsMatrix(j))
    //                  .scalarMultiply(values(i))).scalarMultiply(1 / (values(i) - values(j)))
    //              )}).reduce((m1, m2) => m1.add(m2))
    //          )
    //      )

    //      println()
    //      println("Values after : " + updatedValues.mkString("Array(", ", ", ")"))
    //      print("Vectors after : ")
    //      for(v <- updatedVectors) {
    //        println(v)
    //      }
    //      println()
    //
    //      println()
    //      println("Values difference : " + updVals.mkString("Array(", ", ", ")"))
    //      println("Vectors difference : " + updVecs)
    //      println()

    (updatedValues.toArray, joinColumns(updatedVectors))
  }

}
