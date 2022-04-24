package updater.queries

import scala.collection.GenTraversableOnce

class ClearInvestorStock(coll: GenTraversableOnce[Long]) extends UnwindQuery {
  override def get: String = "with " +
    s"${coll.toString.replaceAll("[A-Za-z]+\\(", "[").replaceAll("\\)", "]")} " +
    "as ids unwind ids as invId match (inv:Investor{id:invId})-[rel1]-(cp:Company) delete rel1"
}

// set property: with [[0, 11], [1, 15]] as invCpAmount unwind invCpAmount as pair match (inv:Investor {id:pair[0]}) set inv.inv_count=pair[1]
// delete old companies: with [0, 1, 3, 5, 7] as ids unwind ids as invId match (inv:Investor{id:invId})-[rel1]-(cp:Company) delete rel1
// create new ones: with [[0, ["ecom"]]] as invsNewCps unwind invsNewCps as invCps unwind invCps[1] as cps match (inv:Investor{id:invCps[0]}) merge (inv)-[:POSSESS]->(cp:Company {symbol: cps})