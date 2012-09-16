/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.swing

case class ColorChangeEvent( c: Color) extends event.Event

/**
 * Wrapper for JColorChooser.
 * 
 * @author andy@hicks.net
 * @see javax.swing.JColorChooser
 */
object ColorChooser {
  def showDialog(parent:Component, title:String, color:Color ): scala.Option[Color] = {
    toOption[Color](javax.swing.JColorChooser.showDialog(parent.peer, title, color))
  }
  
}

class ColorChooser( color0: java.awt.Color ) extends Component  {
  import javax.swing.JColorChooser
  
  def this() = this( java.awt.Color.white )
  
  override lazy val peer:JColorChooser =  new javax.swing.JColorChooser(color0) with SuperMixin

  peer.getSelectionModel().addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e: javax.swing.event.ChangeEvent) { 
      publish( new ColorChangeEvent(peer.getColor)) 
    }
  })

  
  
  def color: Color = peer.getColor
  def color_=(c: Color) = peer.setColor(c)

  def dragEnabled: Boolean = peer.getDragEnabled
  def dragEnabled_=(b: Boolean) = peer.setDragEnabled(b)
  
}