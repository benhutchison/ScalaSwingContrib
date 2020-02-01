package scalaswingcontrib

import scala.collection.mutable

trait MutableSet[S] extends mutable.Set[S] {
  // use 2.13 style interface to define 2.12 collections
  def subtractOne(s: S): this.type
  def addOne(s: S): this.type

  final def -=(s: S): this.type = subtractOne(s)
  final def +=(s: S): this.type = addOne(s)
}
