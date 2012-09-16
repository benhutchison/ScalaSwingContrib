/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.swing

import scala.swing._
import scala.swing.{ListView => _}
import scala.swing.event._
import javax.swing._
import javax.swing.event._

object ListView extends RenderableCellsCompanion {
  type Owner = ListView[_]

  /**
   * The supported modes of user selections.
   */
  object IntervalMode extends Enumeration {
    val Single = Value(ListSelectionModel.SINGLE_SELECTION)
    val SingleInterval = Value(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
    val MultiInterval = Value(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
  }
  
  def wrap[A](c: JList) = new ListView[A] {
    override lazy val peer = c
  }
  
  object Renderer extends CellRendererCompanion {
    type Peer = ListCellRenderer
    case class CellInfo(isSelected: Boolean, focused: Boolean, index: Int)
    object emptyCellInfo extends CellInfo(false, false, 0)
    def wrap[A](r: ListCellRenderer): Renderer[A] = new Wrapped[A](r)
    
    /**
     * Wrapper for <code>javax.swing.ListCellRenderer<code>s
     */
  	class Wrapped[A](override val peer: ListCellRenderer) extends Renderer[A] {
  	  override def componentFor(list: ListView[_], a: A, cellInfo: CellInfo) = {
        Component.wrap(peer.getListCellRendererComponent(list.peer, a, cellInfo.index, 
            cellInfo.isSelected, cellInfo.focused).asInstanceOf[JComponent])
      }
  	}
   
    /**
     * Returns a renderer for items of type <code>A</code>. The given function 
     * converts items of type <code>A</code> to items of type <code>B</code> 
     * for which a renderer is implicitly given. This allows chaining of 
     * renderers, e.g.:
     * 
     * <code>
     * case class Person(name: String, email: String)
     * val persons = List(Person("John", "j.doe@a.com"), Person("Mary", "m.jane@b.com"))
     * new ListView(persons) {
     *   renderer = ListView.Renderer(_.name)
     * }
     * </code>
     */
    def apply[A,B](f: A => B)(implicit renderer: Renderer[B]): Renderer[A] = new Renderer[A] {
      override def componentFor(list: ListView[_], a: A, cellInfo: CellInfo): Component = 
        renderer.componentFor(list, f(a), cellInfo)                                  
    }
    
    def default[A] = new DefaultRenderer[A]

    
    def labelled[A](f: A => (Icon, String)) = new DefaultRenderer[A] with LabelRenderer[A] {val convert = f}
  }
  
  import Renderer.CellInfo
  
  /**
   * Item renderer for a list view. This is contravariant on the type of the 
   * items, so a more general renderer can be used in place of a more specific 
   * one. For instance, an <code>Any</code> renderer can be used for a list view 
   * of strings.
   * 
   * @see javax.swing.ListCellRenderer
   */
  trait Renderer[-A] extends CellRenderer[A] {
    val companion = Renderer
    
    def dispatchToScalaRenderer(list: JList, a: Any, index: Int, isSelected: Boolean, focused: Boolean): JComponent = {
      val wrappedList = ListView.wrap[A](list)
      // Deal with deprecated method if necessary.
      val comp = componentFor(wrappedList, isSelected, focused, a.asInstanceOf[A], index)
      comp match {
        case null => componentFor(wrappedList, a.asInstanceOf[A], CellInfo(isSelected, focused, index)).peer
        case c => c.peer
      }
    }

    def peer: ListCellRenderer = new ListCellRenderer {
      def getListCellRendererComponent(list: JList, a: Any, index: Int, isSelected: Boolean, focused: Boolean) = {
        dispatchToScalaRenderer(list, a, index, isSelected, focused)
      }
    }
    
    @deprecated("Override componentFor(list: List[_], a: A, cellInfo: CellInfo) instead.")
    def componentFor(list: ListView[_], isSelected: Boolean, focused: Boolean, a: A, index: Int): Component = null
    
    override def componentFor(list: ListView[_], a: A, cellInfo: CellInfo): Component = 
        componentFor(list, cellInfo.isSelected, cellInfo.focused, a, cellInfo.index)
  }
  
  /**
   * A default renderer that maintains a single component for item rendering 
   * and preconfigures it to sensible defaults. It is polymorphic on the 
   * component's type so clients can easily use component specific attributes 
   * during configuration.
   */
  abstract class AbstractRenderer[-A, C<:Component](protected val component: C) extends Renderer[A] {
    // The renderer component is responsible for painting selection 
    // backgrounds. Hence, make sure it is opaque to let it draw 
    // the background.
    component.opaque = true

    /**
     * Standard preconfiguration that is commonly done for any component. 
     * This includes foreground and background colors, as well as colors 
     * of item selections.
     */
    def preConfigure(list: ListView[_], isSelected: Boolean, focused: Boolean, a: A, index: Int) {
      if (isSelected) {
        component.background = list.selectionBackground
        component.foreground = list.selectionForeground
      } else {
        component.background = list.background
        component.foreground = list.foreground
      }
    }
    /**
     * Configuration that is specific to the component and this renderer.
     */
    def configure(list: ListView[_], isSelected: Boolean, focused: Boolean, a: A, index: Int)

    /**
     * Configures the component before returning it.
     */
    override def componentFor(list: ListView[_], a: A, cellInfo: CellInfo): Component = {
      preConfigure(list, cellInfo.isSelected, cellInfo.focused, a, cellInfo.index)
      configure(list, cellInfo.isSelected, cellInfo.focused, a, cellInfo.index)
      component
    }
  }
  
  /**
  * Default label-based renderer for a ListView.
  */
  class DefaultRenderer[-A] extends Label with Renderer[A] { 
    override lazy val peer = new javax.swing.DefaultListCellRenderer with SuperMixin { peerThis =>
      override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, isSelected: Boolean, focused: Boolean): JComponent = {
        dispatchToScalaRenderer(list, value, index, isSelected, focused)
        peerThis
      }
      
      def defaultRendererComponent(list: JList, value: AnyRef, index: Int, isSelected: Boolean, focused: Boolean) {
        super.getListCellRendererComponent(list, value, index, isSelected, focused)
      }
    }

    override def componentFor(list: ListView[_], value: A, info: Renderer.CellInfo): Component = {
      peer.defaultRendererComponent(list.peer, value.asInstanceOf[AnyRef], info.index, info.isSelected, info.focused)
      this
    }
  }
}

/**
 * A component that displays a number of elements in a list. A list view does 
 * not support inline editing of items. If you need it, use a table view instead.
 * 
 * Named <code>ListView</code> to avoid a clash with the frequently used 
 * <code>scala.List</code>
 * 
 * @see javax.swing.JList
 */
class ListView[A] extends Component with CellView[A] with RenderableCells[A] {
  import ListView._
  override lazy val peer: JList = new JList with SuperMixin
  override val companion = ListView
  
  
  def this(items: Seq[A]) = {
    this()
    listData = items
  }
  
  protected class ModelWrapper(val items: Seq[A]) extends AbstractListModel {
    def getElementAt(n: Int) = items(n).asInstanceOf[AnyRef]
    def getSize = items.size
  }
  
  def cellValues = listData.iterator
  
  def editable = false
  
  def listData: Seq[A] = peer.getModel match {
    case model: ModelWrapper => model.items
    case model @ _ => new Seq[A] { selfSeq =>
     def length = model.getSize
     def iterator = new Iterator[A] {
       var idx = 0
       def next = { idx += 1; apply(idx-1) }
       def hasNext = idx < selfSeq.length
     }
     def apply(n: Int) = model.getElementAt(n).asInstanceOf[A]
    }
  }
  
  def listData_=(items: Seq[A]) {
    peer.setModel(new AbstractListModel {
      def getElementAt(n: Int) = items(n).asInstanceOf[AnyRef]
      def getSize = items.size
    })
  } 
  
  /**
   * The current item selection.
   */
  object selection extends CellSelection {
  
    @deprecated("Use SelectionSet[A] instead")
    protected type Indices[A] = SelectionSet[A]

    def leadIndex: Int = peer.getSelectionModel.getLeadSelectionIndex
    def anchorIndex: Int = peer.getSelectionModel.getAnchorSelectionIndex
  
    /**
     * The indices of the currently selected items.
     */
    object indices extends SelectionSet(peer.getSelectedIndices) {
      def -=(n: Int): this.type = { peer.removeSelectionInterval(n,n); this }
      def +=(n: Int): this.type = { peer.addSelectionInterval(n,n); this }
      def --=(nn: Seq[Int]) = { nn foreach -=; this }
      def ++=(nn: Seq[Int]) = { nn foreach +=; this }
      @deprecated("Use ListView.selection.leadIndex")
      def leadIndex: Int = peer.getSelectionModel.getLeadSelectionIndex
      @deprecated("Use ListView.selection.anchorIndex")
      def anchorIndex: Int = peer.getSelectionModel.getAnchorSelectionIndex
    }
    
    @deprecated("Use ListView.selectIndices") 
    def selectIndices(ind: Int*) = peer.setSelectedIndices(ind.toArray)
    
    /**
     * The currently selected items.
     */
    object items extends scala.collection.SeqProxy[A] {
      def self = peer.getSelectedValues.map(_.asInstanceOf[A])
      @deprecated("Use ListView.selection.leadIndex")
      def leadIndex: Int = peer.getSelectionModel.getLeadSelectionIndex
      @deprecated("Use ListView.selection.anchorIndex")
      def anchorIndex: Int = peer.getSelectionModel.getAnchorSelectionIndex
    }
    
    def intervalMode: IntervalMode.Value = IntervalMode(peer.getSelectionModel.getSelectionMode)
    def intervalMode_=(m: IntervalMode.Value) { peer.getSelectionModel.setSelectionMode(m.id) }

    peer.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: javax.swing.event.ListSelectionEvent) {
        publish(new ListSelectionChanged(ListView.this, e.getFirstIndex to e.getLastIndex, e.getValueIsAdjusting))
      }
    })

    def cellValues = items.iterator
    def count = items.size
    def empty = items.isEmpty
    
    def adjusting = peer.getSelectionModel.getValueIsAdjusting
  }
  
  def renderer: ListView.Renderer[A] = ListView.Renderer.wrap(peer.getCellRenderer)
  def renderer_=(r: ListView.Renderer[A]) { peer.setCellRenderer(r.peer) }
  
  def fixedCellWidth = peer.getFixedCellWidth
  def fixedCellWidth_=(x: Int) = peer.setFixedCellWidth(x)
  
  def fixedCellHeight = peer.getFixedCellHeight
  def fixedCellHeight_=(x: Int) = peer.setFixedCellHeight(x)
  
  def prototypeCellValue: A = peer.getPrototypeCellValue.asInstanceOf[A]
  def prototypeCellValue_=(a: A) { peer.setPrototypeCellValue(a) }
  
  def selectionForeground: Color = peer.getSelectionForeground
  def selectionForeground_=(c: Color) = peer.setSelectionForeground(c)
  def selectionBackground: Color = peer.getSelectionBackground
  def selectionBackground_=(c: Color) = peer.setSelectionBackground(c)
  
  def selectIndices(ind: Int*) = peer.setSelectedIndices(ind.toArray)
  
  peer.getModel.addListDataListener(new ListDataListener {
    def contentsChanged(e: ListDataEvent) { publish(ListChanged(ListView.this)) }
    def intervalRemoved(e: ListDataEvent) { publish(ListElementsRemoved(ListView.this, e.getIndex0 to e.getIndex1)) }
    def intervalAdded(e: ListDataEvent) { publish(ListElementsAdded(ListView.this, e.getIndex0 to e.getIndex1)) } 
  })
}
