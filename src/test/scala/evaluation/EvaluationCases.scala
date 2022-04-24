package evaluation

import org.scalatest.prop.TableDrivenPropertyChecks

object EvaluationCases extends TableDrivenPropertyChecks {

  case class EvaluationRecord(targetInvId: Int, numberOfInv: Int, topN: Int, relevantParams: (Double, Double))

  val top10 = Table(
    "top 10",
    EvaluationRecord(0, 25, 10, (0.4, 6.0)),
    EvaluationRecord(1, 25, 10, (0.4, 6.0)),
    EvaluationRecord(2, 25, 10, (0.4, 6.0)),
    EvaluationRecord(3, 25, 10, (0.4, 6.0)),
    EvaluationRecord(4, 25, 10, (0.4, 6.0)),
    EvaluationRecord(5, 25, 10, (0.4, 6.0)),
  )

  val top25 = Table(
    "top 25",
    EvaluationRecord(0, 50, 25, (0.4, 6.0)),
    EvaluationRecord(1, 50, 25, (0.4, 6.0)),
    EvaluationRecord(2, 50, 25, (0.4, 6.0)),
    EvaluationRecord(3, 50, 25, (0.4, 6.0)),
    EvaluationRecord(4, 50, 25, (0.4, 6.0)),
    EvaluationRecord(5, 50, 25, (0.4, 6.0)),
  )

  val top50 = Table(
    "top 50",
    EvaluationRecord(0, 100, 50, (0.4, 6.0)),
    EvaluationRecord(1, 100, 50, (0.4, 6.0)),
    EvaluationRecord(2, 100, 50, (0.4, 6.0)),
    EvaluationRecord(3, 100, 50, (0.4, 6.0)),
    EvaluationRecord(4, 100, 50, (0.4, 6.0)),
    EvaluationRecord(5, 100, 50, (0.4, 6.0)),
  )

  val top10restrictive = Table(
    "top 10",
    EvaluationRecord(0, 25, 10, (0.35, 7.0)),
    EvaluationRecord(1, 25, 10, (0.35, 7.0)),
    EvaluationRecord(2, 25, 10, (0.35, 7.0)),
    EvaluationRecord(3, 25, 10, (0.35, 7.0)),
    EvaluationRecord(4, 25, 10, (0.35, 7.0)),
    EvaluationRecord(5, 25, 10, (0.35, 7.0)),
  )

  val top25restrictive = Table(
    "top 25",
    EvaluationRecord(0, 50, 25, (0.35, 7.0)),
    EvaluationRecord(1, 50, 25, (0.35, 7.0)),
    EvaluationRecord(2, 50, 25, (0.35, 7.0)),
    EvaluationRecord(3, 50, 25, (0.35, 7.0)),
    EvaluationRecord(4, 50, 25, (0.35, 7.0)),
    EvaluationRecord(5, 50, 25, (0.35, 7.0)),
  )

  val top50restrictive = Table(
    "top 50",
    EvaluationRecord(0, 100, 50, (0.35, 7.0)),
    EvaluationRecord(1, 100, 50, (0.35, 7.0)),
    EvaluationRecord(2, 100, 50, (0.35, 7.0)),
    EvaluationRecord(3, 100, 50, (0.35, 7.0)),
    EvaluationRecord(4, 100, 50, (0.35, 7.0)),
    EvaluationRecord(5, 100, 50, (0.35, 7.0)),
  )
}
