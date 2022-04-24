package updater.queries

import data.queries.Query
import updater.queries.GetInvestorsIdCategoriesQuery.{categories, ids}

class GetInvestorsIdCategoriesQuery extends Query {
  override def get: String = "match (c:Category)-[rel]-(i:Investor) " +
    s"return i.id as $ids, " +
    s"collect(distinct c.category) as $categories";
}

object GetInvestorsIdCategoriesQuery {
  val ids = "ids"
  val categories = "categories"
}
