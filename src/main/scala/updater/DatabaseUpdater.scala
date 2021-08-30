package updater

import data.DataExtractor
import org.neo4j.driver.internal.logging.JULogging
import org.neo4j.driver.{AuthTokens, Config, GraphDatabase, Logging}
import updater.queries.{GetCategoriesStocksQuery, GetInvestorsIdCategoriesQuery}

import java.util.Properties
import java.util.logging.Level

class DatabaseUpdater(changeRatio: Double) {

  assert(changeRatio > 0 && changeRatio <= 1)
  val log = Logging.none()
  val config = Config.builder().withLogging(Logging.none()).build()
//  val config = Config.builder().withLogging(Logging.javaUtilLogging(Level.WARNING)).build()
  val driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "inv"), config)
  val session = driver.session
  val extractor = new DataExtractor(new Properties())
  private var categoryStocksMap: Map[String, Seq[String]] = Map[String, Seq[String]]()
  private var investorsIdsCategories: Map[Long, Seq[String]] = Map[Long, Seq[String]]()
  val companiesMin = 4
  val companiesMax = 20

  def initialize() = {
    categoryStocksMap = extractor.get(new GetCategoriesStocksQuery()).collect().map(x => (x.getAs[String](0), x.getSeq[String](1))).toMap
    investorsIdsCategories = extractor.get(new GetInvestorsIdCategoriesQuery()).collect().map(x => (x.getAs[Long](0), x.getSeq[String](1))).toMap
  }

  def update() = {
    val selectedIds = investorsIdsCategories.keySet.filter(_ => math.random() <= changeRatio)
    val invCompanies = selectedIds zip (selectedIds map getRandomCompanies) map (x => Seq(x._1, x._2))
    val invCompaniesAmount = invCompanies map(x => Seq(x.head.asInstanceOf[Long], x(1).asInstanceOf[Seq[String]].size.toLong))

    println(new ClearInvestorStock(selectedIds).get)
    session.run(new ClearInvestorStock(selectedIds).get)
    println(new SetInvestmentAmount(invCompaniesAmount).get)
    session.run(new SetInvestmentAmount(invCompaniesAmount).get)
    println(new SetNewCompaniesForInvestors(invCompanies).get)
    session.run(new SetNewCompaniesForInvestors(invCompanies).get)
//    extractor.get(new SetInvestmentAmount(invCompaniesAmount))
//    extractor.get(new SetNewCompaniesForInvestors(invCompanies))

  }

  def close(): Unit = session.close()

  private def getRandomCompanies(invId: Long) = {
//    val companiesAmount = companiesMin + math.random()*(companiesMax-companiesMin)
    def randomCompanies(companies: Seq[String], categoryAndAmount: (String, Int)) = {
      Seq.fill(categoryAndAmount._2)(companies.length-1).map(index => companies((index*math.random()).toInt))
    }

    investorsIdsCategories(invId)
      .map(category => (category, 1 + (math.random() * 8).toInt))
      .flatMap(category => randomCompanies(categoryStocksMap(category._1), category))
  }
}
