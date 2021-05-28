package evaluation.queries

import data.queries.Query

class GetAllStocksFromUserInterestQuery(targetId: Int) extends Query {

  override def get: String = "match (i:Investor)-[_2]-(c: Category)-[_3]-(ind:Industry) " +
    "match (ind)-[_1]-(cp: Company) " +
    s"where i.id=$targetId" +
    "return collect(distinct cp.symbol)"
}
