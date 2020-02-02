package scalaswingcontrib

import scalaswingcontrib.event.ColorChangeEvent
import java.awt.Color
import scala.swing.Component
import javax.{swing => js}
import js.{event => jse}


/**
 * Wrapper for JColorChooser.
 * 
 * @author andy@hicks.net
 * @see javax.swing.JColorChooser
 */
object ColorChooser {
  def showDialog(parent: Component, title: String, color: Color ): Option[Color] = {
    Option(js.JColorChooser.showDialog(parent.peer, title, color))
  }
}

class ColorChooser(initialColor: Color) extends Component {
  
  def this() = this(Color.white)
  
  override lazy val peer: js.JColorChooser =  new js.JColorChooser(initialColor) with SuperMixin

  peer.getSelectionModel().addChangeListener(new jse.ChangeListener {
    def stateChanged(e: jse.ChangeEvent): Unit = {
      publish( new ColorChangeEvent(peer.getColor)) 
    }
  })
  
  def color: Color = peer.getColor
  def color_=(c: Color) = peer.setColor(c)

  def dragEnabled: Boolean = peer.getDragEnabled
  def dragEnabled_=(b: Boolean) = peer.setDragEnabled(b)
  
}
