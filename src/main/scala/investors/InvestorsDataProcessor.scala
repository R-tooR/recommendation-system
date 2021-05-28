package investors

import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.spark.sql
import org.apache.spark.sql.{Column, Row}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

class InvestorsDataProcessor {
  def get(df: sql.DataFrame, target: sql.DataFrame): List[Iterable[Iterable[Double]]] = {
    val ids = df.select(GetInvestorsQuery.id).collectAsList()
    val companiesList = df.select(GetInvestorsQuery.companiesNamesList).collectAsList()
    val commonCompanies = df.select(GetInvestorsQuery.commonCompaniesCount).collectAsList()
    val theirCompanies = df.select(GetInvestorsQuery.theirCompaniesCount).collectAsList()
    val theirCompaniesMap = df.select(GetInvestorsQuery.theirCompaniesMap).collectAsList()
    val theirCategories = df.select(GetInvestorsQuery.allTheirCategories).collectAsList()

    val coll2Norm = (v: Iterable[Int]) => Math.sqrt(v map(vv => vv*vv) sum)
    val targetCompaniesMap = target.select(GetTargetInvestorQuery.targetInvestorCompanies).collectAsList()
    val targetCategories = target.select(GetTargetInvestorQuery.targetInvestorCategories).collectAsList()
    val targetAttributes = List(targetCompaniesMap.size(), targetCompaniesMap.size())
    val normalizedTargetAttributes = targetAttributes map(v => v/coll2Norm(targetAttributes))

    val reduceTuple = (x: (Row, Row)) => {
      print("reduceTuple: x._1: " + x._1 + " x._2: " + x._2)
      x._1.getLong(0) + x._2.getLong(0)
    } // rozszerzyć do wielu parametrów (dodatkowe*)
    val tuple2Norm = (v: (Long, Long)) => Math.sqrt(v._1*v._1 + v._2*v._2)

    val stocksPossessed = commonCompanies zip theirCompanies map reduceTuple
    val investorsAttributesMatrix = (commonCompanies map(_.getLong(0)) zip stocksPossessed) map(v => List(v._1, v._2).map(vv => vv/tuple2Norm(v))) // rozważ euklidesowski dystans

    val allTargetCompanies = (targetCompaniesMap map(r => r.getList(0).toIterable))
    val allTheirCompanies = (theirCompaniesMap zip companiesList map(r => r._1.getList(0).toIterable ++ r._2.getList(0).toIterable))
    val allCompanies = allTargetCompanies ++ allTheirCompanies

    val allCategories = targetCategories ++ theirCategories
    val allInvestorsAttributesMatrix = Iterable(normalizedTargetAttributes) ++ investorsAttributesMatrix

    val allInvStocksSetList = (allCompanies map(r => r.toSet)).toList
    val invCategoriesSetList = (allCategories map(r => r.getList(0).toSet)).toList
    val jaccobi = (s1: Iterable[Nothing], s2: Iterable[Nothing]) => s1.toSet.intersect(s2.toSet).size/s1.toSet.union(s2.toSet).size.toDouble
    val cosine = (s1: Iterable[Double], s2: Iterable[Double]) => 2*(((for((a, b) <- s1 zip s2) yield a * b) sum) / (math.sqrt(s1 map(i => i*i) sum) + math.sqrt(s2 map(i => i*i) sum)))
    def similarityMatrix[T](list: Iterable[Iterable[T]], metric: (Iterable[T], Iterable[T]) => Double) = list map(set => {list map (set2 => metric(set, set2))})


    val investorsStocksMatrix = similarityMatrix(allInvStocksSetList, jaccobi)
    val investorsCategoriesMatrix = similarityMatrix(invCategoriesSetList, jaccobi)
    val investorsAttributesSimilarityMatrix = similarityMatrix(allInvestorsAttributesMatrix, cosine)

    println(investorsStocksMatrix)
    println(investorsCategoriesMatrix)
    println(investorsAttributesSimilarityMatrix)

    List(investorsStocksMatrix, investorsCategoriesMatrix)
//    List(investorsStocksMatrix, investorsCategoriesMatrix, investorsAttributesSimilarityMatrix)
  }

  def collection2DToRealMatrix(nested: Iterable[Iterable[Double]]): Array2DRowRealMatrix = {
    val doubleArray = nested map(iter => iter.toArray) toArray

    new Array2DRowRealMatrix(doubleArray)
  }


}
// https://stackoverflow.com/questions/57530010/spark-scala-cosine-similarity-matrix <- konwersja do rowmatrix
