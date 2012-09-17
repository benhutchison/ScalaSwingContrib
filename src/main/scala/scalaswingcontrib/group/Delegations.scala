package scalaswingcontrib.group

import scala.swing.Component
import javax.{swing => js}

/** Several methods that delegate directly to the underlying `GroupLayout`.
  *  
  * @author Andreas Flierl
  */
trait Delegations {
  val layout: js.GroupLayout
  
  /**
   * The component will not take up any space when it's invisible (default).
   */
  def honorVisibilityOf(comp: Component) =
    layout.setHonorsVisibility(comp.peer, true)
    
  /**
   * The component will still take up its space even when invisible.
   */
  def ignoreVisibilityOf(comp: Component) =
    layout.setHonorsVisibility(comp.peer, false)
  
  /**
   * Links the sizes (horizontal and vertical) of several components.
   * 
   * @param comps the components to link
   */
  def linkSize(comps: Component*) = layout.linkSize(comps.map(_.peer): _*)
  
  /**
   * Links the sizes of several components horizontally.
   * 
   * @param comps the components to link
   */
  def linkHorizontalSize(comps: Component*) =
    layout.linkSize(js.SwingConstants.HORIZONTAL, comps.map(_.peer): _*)
    
  /**
   * Links the sizes of several components vertically.
   * 
   * @param comps the components to link
   */
  def linkVerticalSize(comps: Component*) =
    layout.linkSize(js.SwingConstants.VERTICAL, comps.map(_.peer): _*)
  
  /**
   * Replaces one component with another. Great for dynamic layouts.
   * 
   * @param existing the component to be replaced
   * @param replacement the component replacing the existing one
   */
  def replace(existing: Component, replacement: Component) =
    layout.replace(existing.peer, replacement.peer)
}
