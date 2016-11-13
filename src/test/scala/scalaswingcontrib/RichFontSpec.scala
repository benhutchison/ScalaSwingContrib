package scalaswingcontrib

import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import scala.swing.Label
import java.awt.Font

@RunWith(classOf[JUnitRunner])
class RichFontSpec extends Specification {
  
  import RichFont._
  
  val font = new Font("Dialog", Font.PLAIN, 12)
  val largerFont = new Font("Dialog", Font.PLAIN, 18)
  val smallerFont = new Font("Dialog", Font.PLAIN, 10)

  eg {font.adjustSizePercent(150) mustEqual largerFont}
  
  eg {font.adjustSizePercent(90) mustEqual smallerFont}
}