package investors

import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import org.apache.spark.sql
import utils.UtilFunctions.{jaccobi, similarityMatrix}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.util.{Failure, Success, Try}

class InvestorsDataProcessor {
  def get(df: sql.DataFrame, target: sql.DataFrame): List[Iterable[Iterable[Double]]] = {
    val investorsStocksMatrix = similarityMatrix(tryInAttempts(getInvestorStocks(df, target)), jaccobi)
    val investorsCategoriesMatrix = similarityMatrix(tryInAttempts(getInvestorCategories(df, target)), jaccobi)

    List(investorsStocksMatrix, investorsCategoriesMatrix)
  }

  def getInvestorStocks(df: sql.DataFrame, target: sql.DataFrame): Try[List[Set[Nothing]]] = Try {
    val companiesList = df.select(GetInvestorsQuery.companiesNamesList).collectAsList()
    val theirCompaniesMap = df.select(GetInvestorsQuery.theirCompaniesMap).collectAsList()
    val targetCompaniesMap = target.select(GetTargetInvestorQuery.targetInvestorCompanies).collectAsList()
    val allTheirCompanies = (theirCompaniesMap zip companiesList map (r => r._1.getList(0).toIterable ++ r._2.getList(0).toIterable))
    val allTargetCompanies = (targetCompaniesMap map (r => r.getList(0).toIterable))
    val allCompanies = allTargetCompanies ++ allTheirCompanies
    (allCompanies map (r => r.toSet)).toList
  }

  def getInvestorCategories(df: sql.DataFrame, target: sql.DataFrame): Try[List[Set[Nothing]]] = Try {
    val theirCategories = df.select(GetInvestorsQuery.allTheirCategories).collectAsList()
    val targetCategories = target.select(GetTargetInvestorQuery.targetInvestorCategories).collectAsList()
    val allCategories = targetCategories ++ theirCategories
    (allCategories map (r => r.getList(0).toSet)).toList
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
