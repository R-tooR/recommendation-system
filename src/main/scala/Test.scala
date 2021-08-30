import data.DataExtractor
import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import investors.InvestorsDataProcessor
import updater.DatabaseUpdater
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, EigenDecomposition, RealVector}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.{Matrix, SingularValueDecomposition, Vectors}
import org.apache.spark.mllib.linalg.distributed.{IndexedRowMatrix, RowMatrix}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.neo4j.driver.{AuthTokens, GraphDatabase}
import recommendation.Recommender

import java.util.Properties

object Test {
  // dokumentacja
  // https://spark.apache.org/docs/3.0.0/submitting-applications.html#master-urls
  // https://spark.apache.org/docs/3.0.0/rdd-programming-guide.html#overview
  def main(args: Array[String]): Unit = {
//    val extractor = new DataExtractor(new Properties())
//
//    val df = extractor.get(new GetInvestorsQuery(0))
//    val target = extractor.get(new GetTargetInvestorQuery(0))
//    println(df.show(50))
//
//    val data = new InvestorsDataProcessor().get(df.limit(50), target) map collection2DToRealMatrix
//
//    val e = new recommendation.Engine
//    val r = e.getTopNEmbeddings(e.calculateConsensusEmbedding(e.createConsensusEmbedding(data)), data)
//    println("Results: " + r.mkString("Array(", ", ", ")"))

    val rs = new Recommender(0)
    val db = new DatabaseUpdater(0.1)
    println("------ INIT ------")
    db.initialize()
    for( i <- 1 to 10) {
      println("------ UPDATE " + i + " ------")
      db.update()
    }
//    println("------ UPDATE 1 ------")
//    db.update()
//    println("------ UPDATE 2 ------")
//    db.update()
    db.close()

    print("ddddd")
//    val list = Seq(
//      Set("a", "b", "c"),
//      Set("a", "b", "d"),
//      Set("b", "e")
//    )
//
//    val list2 = Seq(
//      Set(1.0,2.0,3.0),
//      Set(12.0,3.0,4.0),
//      Set(1.0,2.0,4.0)
//    )
//
//    val list3 = Seq(
//      Set(2.0,2.5,3.0),
//      Set(22.0,3.0,4.0),
//      Set(47.0,2.5,4.0)
//    )
//
//    val list4 = Seq(
//      Set(3.0,2.0,3.5),
//      Set(32.0,3.0,4.0),
//      Set(3.0,2.0,4.0)
//    )
//    def collection2DToRealMatrix(nested: Iterable[Iterable[Double]]): Array2DRowRealMatrix = {
//      val doubleArray = nested map(iter => iter.toArray) toArray
//
//      new Array2DRowRealMatrix(doubleArray)
//    }
//
//    val v1 = new ArrayRealVector(Array[Double](1,2,3))
//
//    val v2 = new ArrayRealVector(Array[Double](17,27,37))
//
//    val v3 = new ArrayRealVector(Array[Double](11,12,13))

//    val engine = new recommendation.Engine
//    engine.updateEigenValuesAndCorrespondingVectors(
//      List(
//            collection2DToRealMatrix(list2),
//            collection2DToRealMatrix(list3),
//            collection2DToRealMatrix(list4)
//          ),
//      List(
//      collection2DToRealMatrix(list2),
//      collection2DToRealMatrix(list3),
//      collection2DToRealMatrix(list4)
//      ),
//      List(Array(1,2,3),Array(1,2,3),Array(1,2,3)),
//      List(Seq( v1, v2, v3), Seq( v1, v2, v3), Seq( v1, v2, v3))
//    )



//    val result = engine.getTopNEmbeddings(engine.calculateConsensusEmbedding(engine.createConsensusEmbedding(List(
//      collection2DToRealMatrixlMatrix(list2),
//      collection2DToRealMatrix(list3),
//      collection2DToRealMatrix(list4)
//    ))), List(
//      collection2DToRealMatrix(list2),
//      collection2DToRealMatrix(list3),
//      collection2DToRealMatrix(list4)
//    ))
//
//    println(result)
//    val euclides = (x: Iterable[Double], y: Iterable[Double]) => math.sqrt(((for((a, b) <- x zip y) yield (a-b) * (a-b)) sum))

  }



  def convertDataFrameToRowMatrix(df:DataFrame):RowMatrix = {
    val rows = df.count()
    val cols = df.columns.length
    val rdd:RDD[org.apache.spark.mllib.linalg.Vector] = df.rdd.map(
      row => org.apache.spark.mllib.linalg.Vectors.dense(row.getAs[Seq[Double]](1).toArray)
    )
    val row = new RowMatrix(rdd,rows,cols)
    row
  }

}
