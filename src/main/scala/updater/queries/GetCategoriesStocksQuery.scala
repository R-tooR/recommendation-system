package updater.queries

import data.queries.Query
import updater.queries.GetCategoriesStocksQuery.{category, stocks}

class GetCategoriesStocksQuery extends Query {
  override def get: String = "match (cp:Company)-[rel1]-(i:Industry)-[rel2]-(c:Category) " +
    s"return c.category as $category, " +
    s"collect(distinct cp.symbol) as $stocks";
}

object GetCategoriesStocksQuery{
  val category = "category"
  val stocks = "stocks"
}