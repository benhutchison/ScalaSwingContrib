package scalaswingcontrib.event
import scala.swing.event.Event
import java.awt.Color

case class ColorChangeEvent(c: Color) extends Event
