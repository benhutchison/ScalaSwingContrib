package scalaswingcontrib

import scala.swing._
import Swing._

/** A Panel where children are absolutely positioned.

 Optionally, can be set to automatically adjust added child component location constraints to account
 for the border/inset (if any). If adjustment is not performed, then child components can overlap the border
 area.
 * 
 * The preferredSize of the panel is auto-derived from the greatest bounds of child components, plus border if any. 
 * */
class AbsoluteLayoutPanel(val adjustForBorder: Boolean) extends Panel with Container.Wrapper with LayoutContainer {
  
  def this() = this(false)
  
  override lazy val peer = new javax.swing.JPanel(null) with SuperMixin 

  type Constraints = (Int, Int)

  protected def constraintsFor(comp: Component) = toTuple(comp.peer.getLocation)
  
  protected def areValid(c: Constraints) = (true, "")
  
  protected def add(comp: Component, c: Constraints) {
    def adjusted(c: Constraints) = {
      if (adjustForBorder) {
        (c._1 + insets.left, c._2 + insets.top)
      } else {c}
    }
    
    comp.peer.setLocation(adjusted(c))
    if (comp.size == null || comp.size == new Dimension(0,0)) {
      comp.peer.setSize(comp.preferredSize)
    }
    peer.add(comp.peer)
    val (w, h) = largestChildSize
    
    preferredSize = (w + insets.left + insets.right , h + insets.top + insets.bottom)
  }
  
  def insets = border.getBorderInsets(peer)
  
  def largestChildSize: (Int, Int) = ((0,0) /: contents) {(size, next) => 
    import math.max
    (max(size._1, next.location.x + next.size.width), 
     max(size._2, next.location.y + next.size.height))
  }
}
