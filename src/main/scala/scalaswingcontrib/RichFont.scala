package scalaswingcontrib
import java.awt.Font
import scala.language.implicitConversions
import Utils._

class RichFont(font: Font) {

  def adjustSizePercent(percent: Int): Font = {
    val size = (font.getSize() * percent) / 100
    new Font(font.getName(), font.getStyle(), size)
  }
  
}
object RichFont {
  
  implicit def font2RichFont(f: Font): RichFont = new RichFont(f)
}