package scalaswingcontrib
import java.awt.Font

import Utils._

class RichFont(font: Font) {

  def adjustSizePercent(percent: Int): Font = {
    val size = (font.getSize() * percent) / 100
    new Font(font.getName(), font.getStyle(), size)
  }
  
}
object RichFont {
  
  implicit def font2RichFont(f: Font) = new RichFont(f)
}