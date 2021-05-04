import Test.collection2DToRealMatrix
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Properties}

class EngineTest extends Properties("Engine") {

  val similarityMatrices = List(
    collection2DToRealMatrix(Seq(
      Set(1.0, 0.5, 0.25),
      Set(0.5, 1.0, 0.4),
      Set(0.25, 0.4, 1.0)
    )),
    collection2DToRealMatrix(Seq(
      Set(1.0, 0.9, 0.2),
      Set(0.9, 1.0, 0.3),
      Set(0.2, 0.3, 1.0)
    )),
    collection2DToRealMatrix(Seq(
      Set(1.0, 0.7, 0.5),
      Set(0.7, 1.0, 0.3),
      Set(0.5, 0.3, 1.0)
    ))
  )

  implicit val engine: Arbitrary[Engine] = Arbitrary {
    new Engine
  }

  property("find most similar investors") = forAll { (e: Engine) => {
      val result = e.getTopNEmbeddings(e.calculateConsensusEmbedding(e.createConsensusEmbedding(similarityMatrices)), similarityMatrices)
      println(result.mkString("Array(", ", ", ")"))
      result.length == 3 && result(0) > result(1) && result(1) > result(2)
    }
  }

//  property("concatenate") = forAll { (a: String, b: String) =>
//    (a + b).length > a.length && (a + b).length > b.length
//  }
//
//  property("substring") = forAll { (a: String, b: String, c: String) =>
//    (a + b + c).substring(a.length, a.length + b.length) == b
//  }

}
