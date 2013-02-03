package scalaswingcontrib

import swing.{Label, Component, Publisher}
import javax.{swing => js}
import language.higherKinds

/**
* Describes the structure of a component's companion object where pluggable cell renderers must be supported.
* @author Ken Scambler
*/
trait RenderableCellsCompanion {
  type Renderer[-A] <: CellRenderer[A]
  protected type Owner <: Component with CellView[_]
  
  val Renderer: CellRendererCompanion
  
  /**
  * A generic renderer that uses Swing's built-in renderers. If there is no 
  * specific renderer for a type, this renderer falls back to a renderer 
  * that renders the string returned from an item's <code>toString</code>.
  */
  implicit val GenericRenderer: Renderer[Any] = Renderer.default

  /**
  * A default renderer implementation based on a Label.  
  */
  type DefaultRenderer[-A] <: Label with Renderer[A]
  
  
  trait CellRendererCompanion {
    type Peer // eg. javax.swing.table.TableCellRenderer, javax.swing.tree.TreeCellRenderer
    type CellInfo
    
    val emptyCellInfo: CellInfo
    def wrap[A](r: Peer): Renderer[A]
    def apply[A, B: Renderer](f: A => B): Renderer[A]

    def default[A]: DefaultRenderer[A]
    
    /**
    * Convenient default display of a cell node, which provides an Icon and label text for each item.
    */
    def labeled[A](f: A => (js.Icon, String)): DefaultRenderer[A]
    
    protected trait LabelRenderer[-A] extends CellRenderer[A] {
      this: DefaultRenderer[A] =>
      val convert: A => (js.Icon, String)
      
      override abstract def componentFor(owner: Owner, a: A, info: companion.CellInfo): Component = {
        val c = super.componentFor(owner, a, info)
        val (labelIcon, labelText) = convert(a)
        icon = labelIcon
        text = labelText
        c
      }
    }
  }

  trait CellRenderer[-A] extends Publisher  {
    val companion: CellRendererCompanion
    def peer: companion.Peer
    def componentFor(owner: Owner, value: A, cellInfo: companion.CellInfo): Component
  }
}

