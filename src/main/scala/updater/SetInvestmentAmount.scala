package updater

import updater.queries.UnwindQuery

import scala.collection.GenTraversableOnce

class SetInvestmentAmount(coll: GenTraversableOnce[GenTraversableOnce[Long]]) extends UnwindQuery{
  override def get: String = "with " +
    s"${coll.toString.replaceAll("[A-Za-z]+\\(", "[").replaceAll("\\)", "]")} " +
    "as invCpAmount unwind invCpAmount as pair match (inv:Investor {id:pair[0]}) set inv.inv_count=pair[1]"
}
