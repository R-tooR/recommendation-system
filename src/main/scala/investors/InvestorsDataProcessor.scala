package investors

import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.spark.sql
import org.apache.spark.sql.{Column, Row}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.util.{Failure, Success, Try}

class InvestorsDataProcessor {
  def get(df: sql.DataFrame, target: sql.DataFrame): List[Iterable[Iterable[Double]]] = {
//    val ids = df.select(GetInvestorsQuery.id).collectAsList()

//    val reduceTuple = (x: (Row, Row)) => {
//      println("reduceTuple: x._1: " + x._1 + " x._2: " + x._2)
//      println("reduceTuple: x._1: " + x._1.get(0).isInstanceOf[Long] + " x._2: " + x._2.get(0).isInstanceOf[Long])
//      x._1.getLong(0) + x._2.getLong(0)
//    } // rozszerzyć do wielu parametrów (dodatkowe*)
//    val tuple2Norm = (v: (Long, Long)) => Math.sqrt(v._1*v._1 + v._2*v._2)

//    val commonCompanies = df.select(GetInvestorsQuery.commonCompaniesCount).collectAsList()
//    val theirCompanies = df.select(GetInvestorsQuery.theirCompaniesCount).collectAsList()
//    val stocksPossessed = commonCompanies zip theirCompanies map reduceTuple
//    val investorsAttributesMatrix = (commonCompanies map(_.getLong(0)) zip stocksPossessed) map(v => List(v._1, v._2).map(vv => vv/tuple2Norm(v))) // rozważ euklidesowski dystans

//    val coll2Norm = (v: Iterable[Int]) => Math.sqrt(v map(vv => vv*vv) sum)
//    val targetAttributes = List(targetCompaniesMap.size(), targetCompaniesMap.size())
//    val normalizedTargetAttributes = targetAttributes map(v => v/coll2Norm(targetAttributes))
//    val allInvestorsAttributesMatrix = Iterable(normalizedTargetAttributes) ++ investorsAttributesMatrix

    def getInvestorStocks(df: sql.DataFrame, target: sql.DataFrame) = Try {
      val companiesList = df.select(GetInvestorsQuery.companiesNamesList).collectAsList()
      val theirCompaniesMap = df.select(GetInvestorsQuery.theirCompaniesMap).collectAsList()
      val targetCompaniesMap = target.select(GetTargetInvestorQuery.targetInvestorCompanies).collectAsList()
      val allTheirCompanies = (theirCompaniesMap zip companiesList map(r => r._1.getList(0).toIterable ++ r._2.getList(0).toIterable))
      val allTargetCompanies = (targetCompaniesMap map(r => r.getList(0).toIterable))
      val allCompanies = allTargetCompanies ++ allTheirCompanies
      (allCompanies map(r => r.toSet)).toList
    }

    def getInvestorCategories(df: sql.DataFrame, target: sql.DataFrame) = Try {
      val theirCategories = df.select(GetInvestorsQuery.allTheirCategories).collectAsList()
      val targetCategories = target.select(GetTargetInvestorQuery.targetInvestorCategories).collectAsList()
      val allCategories = targetCategories ++ theirCategories
      (allCategories map(r => r.getList(0).toSet)).toList
    }


    val jaccobi = (s1: Iterable[Nothing], s2: Iterable[Nothing]) => s1.toSet.intersect(s2.toSet).size/s1.toSet.union(s2.toSet).size.toDouble
//    val cosine = (s1: Iterable[Double], s2: Iterable[Double]) => 2*(((for((a, b) <- s1 zip s2) yield a * b) sum) / (math.sqrt(s1 map(i => i*i) sum) + math.sqrt(s2 map(i => i*i) sum)))
    def similarityMatrix[T](list: Iterable[Iterable[T]], metric: (Iterable[T], Iterable[T]) => Double) = list map(set => {list map (set2 => metric(set, set2))})


    val investorsStocksMatrix = similarityMatrix(tryInAttempts(getInvestorStocks(df, target)), jaccobi)
    val investorsCategoriesMatrix = similarityMatrix(tryInAttempts(getInvestorCategories(df, target)), jaccobi)
//    val investorsAttributesSimilarityMatrix = similarityMatrix(allInvestorsAttributesMatrix, cosine)

//    println(investorsStocksMatrix)
//    println(investorsCategoriesMatrix)
//    println(investorsAttributesSimilarityMatrix)

    List(investorsStocksMatrix, investorsCategoriesMatrix)
//    List(investorsStocksMatrix, investorsCategoriesMatrix, investorsAttributesSimilarityMatrix)
  }

  def collection2DToRealMatrix(nested: Iterable[Iterable[Double]]): Array2DRowRealMatrix = {
    val doubleArray = nested map(iter => iter.toArray) toArray

    new Array2DRowRealMatrix(doubleArray)
  }

  def tryInAttempts(tryCode: Try[List[Set[Nothing]]], attempts: Int = 5): List[Set[Nothing]] = {
      tryCode match {
        case Success(value) => value
        case Failure(exception) => {
          println("Parsing failed. Retrying... ")
          exception.printStackTrace()
          if (attempts > 0)
            tryInAttempts(tryCode, attempts - 1)
          else throw exception
        }
      }
  }


}
// https://stackoverflow.com/questions/57530010/spark-scala-cosine-similarity-matrix <- konwersja do rowmatrix
