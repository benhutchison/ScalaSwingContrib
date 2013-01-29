package scalaswingcontrib

import java.awt.Color
import scala.language.implicitConversions
import Utils._

class RichColor(color: Color) {

  def toHexString = "#" + Integer.toHexString(color.getRGB()).substring(2)

  def *(scale: Int) = mapRgbComponents(_ * scale)

  def +(delta: Int) = mapRgbComponents(_ + delta)

  def hue = Color.RGBtoHSB(color.getRed, color.getGreen, color.getBlue, null)(0).toDouble
  def saturation = Color.RGBtoHSB(color.getRed, color.getGreen, color.getBlue, null)(1).toDouble
  def brightness = Color.RGBtoHSB(color.getRed, color.getGreen, color.getBlue, null)(2).toDouble

  def withHue(newHue: Double) = {
    RichColor.fromHSB(newHue max 0 min 1.0, saturation, brightness)
  }
  def withSaturation(newSaturation: Double) = {
    RichColor.fromHSB(hue, newSaturation max 0 min 1.0, brightness)
  }
  def withBrightness(newBrightness: Double) = {
    RichColor.fromHSB(hue, saturation, newBrightness max 0 min 1.0)
  }

  /** Adjust Saturation by a percentage of the whole Saturation range 0 - 1.0 */
  def adjustSaturationPercent(percent: Int) = {
    ensurePercent(percent)
    withSaturation(saturation + percent/100.0)
  }

  /** Adjust hue by a percentage of the whole hue range 0 - 1.0 */
  def adjustHuePercent(percent: Int) = {
    ensurePercent(percent)
    withHue(hue + percent/100.0)
  }

  /** Adjust brightness by a percentage of the whole range 0 - 1.0 */
  def adjustBrightnessPercent(percent: Int) = {
    ensurePercent(percent)
    withBrightness(brightness + percent/100.0)
  }

  /** Adjust brightness by a percentage of the whole brightness range 0 - 1.0 */
  def brightenPercent(percent: Int) = {
    ensurePercent(percent)
    withBrightness(brightness + percent/100.0)
  }
  def darkenPercent(percent: Int) = {
    brightenPercent(-percent)
  }

  

  def mapRgbComponents(f: (Int)=>Int) = {
    val f2 = f.andThen(clamp _)
    new Color(f2(color.getRed()), f2(color.getGreen()), f2(color.getBlue()))
  }

  def rgbComponents = Seq(color.getRed(), color.getGreen(), color.getBlue())

  private def clamp(i: Int) = (0 max i) min 255

  private def ensureZeroToOne(value: Double) {assert(value >= 0 && value <= 1.0)}

  def toRichString = "RichColor(R: "+color.getRed+", G: "+color.getGreen+", B: "+color.getBlue+"/H: "+hue+", S: "+saturation+", B: "+brightness+")"
}
object RichColor {

  implicit def color2RichColor(c: Color) = new RichColor(c)

  def fromHSB(hue: Double, saturation: Double, brightness: Double): Color = {
    Color.getHSBColor(hue.toFloat, saturation.toFloat, brightness.toFloat)
  }

}
