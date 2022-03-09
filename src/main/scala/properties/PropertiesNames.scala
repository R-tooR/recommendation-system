package properties

object PropertiesNames {
  val recommenderTopN    = "application.investors_top_n"
  val targetInvestor     = "application.target_investor"
  val calculationTopNAttr     = "application.embeddings_top_n_ratio"

  val updaterEnabled     = "application.updater.enabled"
  val updaterEpsilon     = "application.updater.eps"
  val updaterChangeRatio = "application.updater.change_ratio"

  val dbUsername = "database.username"
  val dbPassword = "database.password"
  val dbUrl      = "database.url"

  val appName    = "application.name"
  val appMaster  = "application.master"
  val appLogging = "application.logging"

  val appPort    = "application.port"

}
