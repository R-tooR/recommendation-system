package investors

import data.queries.{GetInvestorsQuery, GetTargetInvestorQuery}
import org.apache.spark.sql
import utils.UtilFunctions.{jaccobi, parseToIterable, similarityMatrix}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.util.{Failure, Success, Try}

class InvestorsDataProcessor {
  def get(df: sql.DataFrame, target: sql.DataFrame): List[Iterable[Iterable[Double]]] = {
    val investorsStocksMatrix = similarityMatrix(tryInAttempts(getInvestorStocks(df, target)), jaccobi)
    val investorsCategoriesMatrix = similarityMatrix(tryInAttempts(getInvestorCategories(df, target)), jaccobi)

    List(investorsStocksMatrix, investorsCategoriesMatrix)
  }

  def getInvestorStocks(df: sql.DataFrame, target: sql.DataFrame): Try[List[Set[String]]] = Try {
    val companiesList = df.select(GetInvestorsQuery.companiesNamesList).collectAsList()
    val theirCompaniesMap = df.select(GetInvestorsQuery.theirCompaniesMap).collectAsList()
    val targetCompaniesMap = target.select(GetTargetInvestorQuery.targetInvestorCompanies).collectAsList()
    val allTheirCompanies = (theirCompaniesMap zip companiesList map (r => {
      parseToIterable(r._1.get(0)) ++ parseToIterable(r._2.get(0))
    }))
    val allTargetCompanies = (targetCompaniesMap map (r => parseToIterable(r.get(0)).toIterable))
    val allCompanies = allTargetCompanies ++ allTheirCompanies
    (allCompanies map (r => r.toSet)).toList
  }

  def getInvestorCategories(df: sql.DataFrame, target: sql.DataFrame): Try[List[Set[String]]] = Try {
    val theirCategories = df.select(GetInvestorsQuery.allTheirCategories).collectAsList()
    val targetCategories = target.select(GetTargetInvestorQuery.targetInvestorCategories).collectAsList()
    val allCategories = targetCategories ++ theirCategories
    (allCategories map (r => (parseToIterable(r.get(0)) ++ Seq()).toSet)).toList
  }

  def tryInAttempts(tryCode: Try[List[Set[String]]], attempts: Int = 5): List[Set[String]] = {
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
