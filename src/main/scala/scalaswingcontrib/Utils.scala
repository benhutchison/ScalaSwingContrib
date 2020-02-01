package scalaswingcontrib

/** Put shared util functions here if they don't have any more appropriate place to go.*/
object Utils {

  def ensurePercent(percent: Int): Unit = {require(percent >= -100 && percent <= 100)}
}