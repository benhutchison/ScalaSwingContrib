package scalaswingcontrib

import scala.collection.mutable

trait MutableSet[A] extends mutable.Set[A] {
  override def clear(): Nothing = throw new NotImplementedError // required by 2.13, but we are not using it

}
