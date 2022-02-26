package utils

import org.apache.commons.math3.linear.{Array2DRowRealMatrix, RealMatrix}

import scala.collection.mutable

object UtilFunctions {

  def jaccobi (s1: Iterable[Nothing], s2: Iterable[Nothing]) = s1.toSet.intersect(s2.toSet).size/s1.toSet.union(s2.toSet).size.toDouble
  def cosine (s1: Iterable[Double], s2: Iterable[Double]) = 2*(((for((a, b) <- s1 zip s2) yield a * b) sum) / (math.sqrt(s1 map(i => i*i) sum) + math.sqrt(s2 map(i => i*i) sum)))
  def sech0_5 (x: Double) = 2 / (Math.exp(2 * x) + Math.exp(-2 * x))
  def euclides (x: Iterable[Double], y: Iterable[Double]) = math.sqrt(((for ((a, b) <- x zip y) yield (a - b) * (a - b)) sum))

  def similarityMatrix[T](list: Iterable[Iterable[T]], metric: (Iterable[T], Iterable[T]) => Double) = list map(set => {list map (set2 => metric(set, set2))})
  def joinColumns = (list: List[RealMatrix]) => new Array2DRowRealMatrix(list.map(col => col.getColumn(0)).toArray)
  def zip4[T1, T2, T3, T4, T](t1: List[T1], t2: List[T2], t3: List[T3], t4: List[T4]) = List(t1, t2, t3, t4).min(Ordering.by[List[Any], Double](_.size)).indices.map(i => (t1(i), t2(i), t3(i), t4(i)))

  def collection2DToRealMatrix(nested: Iterable[Iterable[Double]]): Array2DRowRealMatrix = {
    val doubleArray = nested map(iter => iter.toArray) toArray

    new Array2DRowRealMatrix(doubleArray)
  }

  def normalizeArray(x: Array[Double]): Array[Double] = {
    val max = x.max
    x map (n => n/max)
  }

  def addIfNotExist(key: String, value: Double, recommendedStocks: mutable.Map[String, Double]) = {
    if (recommendedStocks contains key) {
      recommendedStocks(key) += value
    } else {
      recommendedStocks(key) = value
    }
  }

}
