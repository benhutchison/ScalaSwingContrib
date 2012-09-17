package scalaswingcontrib.group

import javax.swing.{LayoutStyle => jsl}

/** Provides placement constants for a `GroupPanel`.
  * 
  * @author Andreas Flierl
  */
trait Placements {
  /**
   * Specifies how two components are placed relative to each other.
   * 
   * @see javax.swing.LayoutStyle.ComponentPlacement
   */
  protected[Placements] sealed class Placement(
      private[group] val wrapped: jsl.ComponentPlacement)
  
  /**
   * Specifies if two components are related or not.
   * 
   * @see javax.swing.LayoutStyle.ComponentPlacement
   */    
  protected[Placements] final class RelatedOrUnrelated(
      cp: jsl.ComponentPlacement) extends Placement(cp)
  
  /** Used to request the distance between two visually related components. */
  final val Related = new RelatedOrUnrelated(jsl.ComponentPlacement.RELATED)
  
  /** Used to request the distance between two visually unrelated components. */
  final val Unrelated = new RelatedOrUnrelated(jsl.ComponentPlacement.UNRELATED)
  
  /**
   * Used to request the (horizontal) indentation of a component that is 
   * positioned underneath another component.
   */
  final val Indent = new Placement(jsl.ComponentPlacement.INDENT)
}
