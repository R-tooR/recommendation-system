package updater

import updater.queries.UnwindQuery

import scala.collection.GenTraversableOnce

class SetNewCompaniesForInvestors(coll: GenTraversableOnce[GenTraversableOnce[Any]]) extends UnwindQuery{
  override def get: String = "with " +
    s"${coll.toString.replaceAll("[A-Za-z]+\\(", "[").replaceAll("\\)", "]").replaceAll("[a-z]+\\s*", "\"$0\"")} " +
    "as invsNewCps unwind invsNewCps as invCps unwind invCps[1] as cps match (inv:Investor{id:invCps[0]}), (cp:Company {symbol: cps}) merge ((inv)-[:POSSESS]->(cp))"
}
