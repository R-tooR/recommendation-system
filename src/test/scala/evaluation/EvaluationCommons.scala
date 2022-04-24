package evaluation

import properties.ParametersResolver
import recommendation.Recommender
import updater.DatabaseUpdater

import java.io.{FileInputStream, InputStreamReader}
import java.util.Properties
import scala.collection.immutable.ListMap

object EvaluationCommons  {
  def evaluation(input: (Int, Int), relevancy: (Double, Double) = (0.4, 6.0), runs: Int = 1) = {
    val resolver = new ParametersResolver(Array("-appconfig=.\\src\\test\\resources\\appConfiguration.properties"))
    val appProperties = new Properties
    appProperties.load(new InputStreamReader(new FileInputStream(resolver.paramsMap.get(ParametersResolver.applicationConfig).get)))
    def run(runs: Int, recommender: Recommender, params: (Int, Int, (Double, Double))) = {
      val db = new DatabaseUpdater(0.1, appProperties)
      db.initialize()
      for (i <- 0 until runs) {
        recommender.findStocksBaseOnSimilarity(params._1, params._2, params._3)
        db.update()
      }

      recommender.findStocksBaseOnSimilarity(params._1, params._2, params._3)
    }
    val recommender: Recommender = new Recommender(input._2)

    val resultsCommon = run(runs,recommender,(input._1, input._2, relevancy))

    val sortedResCommon = ListMap(resultsCommon._1.toSeq.sortWith(_._2 > _._2): _*).take(input._1)
    println(sortedResCommon)
    println(resultsCommon._2)
    val relevantAndRetrievedIntersectionSize = sortedResCommon.keySet.intersect(resultsCommon._2.keySet).size
    val retrievedSize = resultsCommon._1.take(input._1).size
    val relevantSize = resultsCommon._2.take(input._1).size

    println("Precision@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / retrievedSize))
    println("Recall@" + input._1 + " = " + (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))

    ((relevantAndRetrievedIntersectionSize.toDouble / retrievedSize), (relevantAndRetrievedIntersectionSize.toDouble / relevantSize))
  }
}
