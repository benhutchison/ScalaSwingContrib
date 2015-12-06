package scalaswingcontrib
package tree

import scalaswingcontrib.event.{TreeNodesInserted, TreeNodesRemoved, TreeStructureChanged, TreeNodesChanged, TreePathSelected}
import scala.swing.{Color, Component, Label, Scrollable}
import java.{util => ju}
import javax.{swing => js}
import js.{tree => jst}
import js.{event => jse}
import scala.collection.breakOut
import scala.language.{implicitConversions, reflectiveCalls}

sealed trait TreeEditors extends EditableCellsCompanion {
  this: Tree.type =>

  protected override type Owner = Tree[_]

  object Editor extends CellEditorCompanion {
    final case class CellInfo(isSelected: Boolean = false, isExpanded: Boolean = false,
                              isLeaf: Boolean = false, row: Int = 0)
    override val emptyCellInfo = CellInfo()

    override type Peer = jst.TreeCellEditor
  
    def wrap[A](e: jst.TreeCellEditor): Editor[A] = new Wrapped[A](e)
   
    /**
     * Wrapper for <code>javax.swing.tree.TreeCellEditor<code>s
     */
    final class Wrapped[A](override val peer: jst.TreeCellEditor) extends Editor[A] {
      override def componentFor(tree: Tree[_], a: A, cellInfo: CellInfo): Component = {
        Component.wrap(peer.getTreeCellEditorComponent(tree.peer, a, cellInfo.isSelected, 
            cellInfo.isExpanded, cellInfo.isLeaf, cellInfo.row).asInstanceOf[js.JComponent])
      }
      def value = peer.getCellEditorValue.asInstanceOf[A]
    }
   
    /**
     * Returns an editor for items of type <code>A</code>. The given function
     * converts items of type <code>A</code> to items of type <code>B</code>
     * for which an editor is implicitly given. This allows chaining of
     * editors, e.g.:
     *
     * <code>
     * case class Person(name: String, subordinates: List[Person])
     * val persons = Person("John", List(Person("Jack", Nil), Person("Jill", List(Person("Betty", Nil)))))
     * val tree = new Tree[Person](persons, _.subordinates) {
     *   editor = Editor(_.name, s => Person(s, Nil))
     * }
     * </code>
     */
    def apply[A, B](toB: A => B, toA: B => A)(implicit editor: Editor[B]): Editor[A] = new Editor[A] {
    
      override lazy val peer = new jst.TreeCellEditor {

        override def getTreeCellEditorComponent(tree: js.JTree, value: Any, isSelected: Boolean, 
                                                isExpanded: Boolean, isLeaf: Boolean, row: Int) = {
          val treeWrapper = getTreeWrapper(tree)
          val a = treeWrapper.model unpackNode value
          editor.peer.getTreeCellEditorComponent(tree, toB(a), isSelected, isExpanded, isLeaf, row)
        }
        def addCellEditorListener(cel: jse.CellEditorListener) { editor.peer.addCellEditorListener(cel) }
        def cancelCellEditing() { editor.peer.cancelCellEditing() }
        def getCellEditorValue: AnyRef = toA(editor.peer.getCellEditorValue.asInstanceOf[B]).asInstanceOf[AnyRef]
        def isCellEditable(e: ju.EventObject) = editor.peer.isCellEditable(e)
        def removeCellEditorListener(cel: jse.CellEditorListener) { editor.peer.removeCellEditorListener(cel) }
        def shouldSelectCell(e: ju.EventObject) = { editor.peer.shouldSelectCell(e) }
        def stopCellEditing() = editor.peer.stopCellEditing()
      }
      
      listenToPeer(this.peer)
      
      override def componentFor(tree: Tree[_], a: A, info: CellInfo): Component = {
        editor.componentFor(tree, toB(a), info)
      }
      
      override def value = peer.getCellEditorValue.asInstanceOf[A]
    }
  }

  /**
   * A tree cell editor.
   * @see javax.swing.tree.TreeCellEditor
   */
  abstract class Editor[A] extends CellEditor[A] {
    import Editor._
    val companion = Editor
    
    protected[tree] def getTreeWrapper(peerTree: js.JTree) = peerTree match {
      case t: JTreeMixin[A] => t.treeWrapper
      case _ => throw new IllegalArgumentException(
          "This javax.swing.JTree does not mix in JTreeMixin, and so cannot be used by scala.swing.Tree#Editor")
    }
    
    protected class TreeEditorPeer extends EditorPeer with jst.TreeCellEditor {
      override def getTreeCellEditorComponent(tree: js.JTree, value: Any, selected: Boolean, expanded: Boolean,
                                              leaf: Boolean, rowIndex: Int) = {
        val treeWrapper = getTreeWrapper(tree)
        val a = treeWrapper.model unpackNode value
        componentFor(treeWrapper, a,
          CellInfo(isSelected = selected, isExpanded = expanded, isLeaf = leaf, row = rowIndex)).peer
      }
    }

    private[this] lazy val lazyPeer: jst.TreeCellEditor = new TreeEditorPeer
    def peer = lazyPeer // We can't use a lazy val directly, as Wrapped wouldn't be able to override with a non-lazy val.
  }
}


sealed trait TreeRenderers extends RenderableCellsCompanion {
  _: Tree.type =>

  protected override type Owner = Tree[_]
  
  object Renderer extends CellRendererCompanion {
      
    final case class CellInfo(isSelected: Boolean = false, isExpanded: Boolean = false,
                              isLeaf: Boolean = false, row: Int = 0, hasFocus: Boolean = false)
    override val emptyCellInfo = CellInfo()
    
    type Peer = jst.TreeCellRenderer

            
    def wrap[A](r: Peer): Renderer[A] = new Wrapped[A](r)
   
    /**
     * Wrapper for <code>javax.swing.tree.TreeCellRenderer<code>s
     */
    class Wrapped[-A](override val peer: Peer) extends Renderer[A] {
      override def componentFor(tree: Tree[_], value: A, info: CellInfo): Component = {
        Component.wrap(peer.getTreeCellRendererComponent(tree.peer, value, info.isSelected, 
            info.isExpanded, info.isLeaf, info.row, info.hasFocus).asInstanceOf[js.JComponent])
      }
    }

    def apply[A,B](f: A => B)(implicit renderer: Renderer[B]): Renderer[A] = new Renderer[A] {
      def componentFor(tree: Tree[_], value: A, info: CellInfo): Component = {
        renderer.componentFor(tree, f(value), info)
      }
    }
    
    override def default[A] = new DefaultRenderer[A]
    
    override def labeled[A](f: A => (js.Icon, String)) = new DefaultRenderer[A] with LabelRenderer[A] { val convert = f }
  }
  
  /**
   * Base trait of Tree cell renderers, in which the user provides the rendering component 
   * by overriding the componentFor() method.
   * @see javax.swing.tree.TreeCellRenderer
   */
  trait Renderer[-A] extends CellRenderer[A] {
    import Renderer._
    val companion = Renderer
    
    protected def dispatchToScalaRenderer(tree: js.JTree, value: AnyRef, selected: Boolean, expanded: Boolean, 
                                       leaf: Boolean, rowIndex: Int, focus: Boolean): js.JComponent = {
      
      def treeWrapper = tree match {
        case t: JTreeMixin[A] => t.treeWrapper
        case _ => throw new IllegalArgumentException(
          "This javax.swing.JTree does not mix in JTreeMixin, and so cannot be used by scala.swing.Tree#Renderer")
      }

      if (treeWrapper.model isRootNode value) {
        new js.JTextField
      } else {
        val a = treeWrapper.model unpackNode value
        componentFor(treeWrapper, a, CellInfo(isSelected=selected, isExpanded=expanded, isLeaf=leaf, row=rowIndex, hasFocus=focus)).peer
      }
    }
    
    /**
    *  By default, the peer cell renderer defers to the user's implementation of componentFor(), although this can be 
    * overridden with other TreeCellRenderer implementations.
    */
    def peer: Peer = new jst.TreeCellRenderer {
      override def getTreeCellRendererComponent(tree: js.JTree, value: AnyRef, isSelected: Boolean, isExpanded: Boolean, 
                                       isLeaf: Boolean, row: Int, hasFocus: Boolean): js.JComponent = {
        dispatchToScalaRenderer(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus)
      }
    }
  }
 
  /**
  * Renderer implementation where the component used in rendering is provided by the user. The preconfigure() and configure() methods
  * can be overridden to provide additional configuration.
  */
  abstract class AbstractRenderer[-A, C<:Component](val component: C) extends Renderer[A] {
    import Renderer._
  
    // The renderer component is responsible for painting selection
    // backgrounds. Hence, make sure it is opaque to let it draw
    // the background.
    component.opaque = true

    /**
     * Standard preconfiguration that is commonly done for any component.
     */
    def preConfigure(tree: Tree[_], value: A, info: CellInfo) {
        
    }
    /**
     * Configuration that is specific to the component and this renderer.
     */
    def configure(tree: Tree[_], value: A, info: CellInfo)

    /**
     * Configures the component before returning it.
     */
    def componentFor(tree: Tree[_], value: A, info: CellInfo): Component = {
      preConfigure(tree, value, info)
      configure(tree, value, info)
      component
    }
  }

  /**
  * Default renderer for a tree, with many configurable settings.
  */
  class DefaultRenderer[-A] extends Label with Renderer[A] { 
    override lazy val peer = new jst.DefaultTreeCellRenderer with SuperMixin { peerThis =>
      override def getTreeCellRendererComponent(tree: js.JTree, value: AnyRef, isSelected: Boolean, isExpanded: Boolean, 
                                             isLeaf: Boolean, row: Int, hasFocus: Boolean): js.JComponent = {
        dispatchToScalaRenderer(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus)
        peerThis
      }
      
      def defaultRendererComponent(tree: js.JTree, value: AnyRef, isSelected: Boolean, isExpanded: Boolean, 
                                             isLeaf: Boolean, row: Int, hasFocus: Boolean) {
        super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus)
      }
    }
    
    def closedIcon = peer.getClosedIcon
    def closedIcon_=(icon: js.Icon) {peer.setClosedIcon(icon)}
    def leafIcon = peer.getLeafIcon
    def leafIcon_=(icon: js.Icon) {peer.setLeafIcon(icon)}
    def openIcon = peer.getOpenIcon
    def openIcon_=(icon: js.Icon) {peer.setOpenIcon(icon)}
    def backgroundNonSelectionColor = peer.getBackgroundNonSelectionColor
    def backgroundNonSelectionColor_=(c: Color) {peer.setBackgroundNonSelectionColor(c)}
    def backgroundSelectionColor = peer.getBackgroundSelectionColor
    def backgroundSelectionColor_=(c: Color) {peer.setBackgroundSelectionColor(c)}
    def borderSelectionColor = peer.getBorderSelectionColor
    def borderSelectionColor_=(c: Color) {peer.setBorderSelectionColor(c)}
    def textNonSelectionColor = peer.getTextNonSelectionColor
    def textNonSelectionColor_=(c: Color) {peer.setTextNonSelectionColor(c)}
    def textSelectionColor = peer.getTextSelectionColor
    def textSelectionColor_=(c: Color) {peer.setTextSelectionColor(c)}

    override def componentFor(tree: Tree[_], value: A, info: Renderer.CellInfo): Component = {
      peer.defaultRendererComponent(tree.peer, value.asInstanceOf[AnyRef], info.isSelected, info.isExpanded, info.isLeaf, info.row, info.hasFocus)
      this
    }
  }
}



object Tree extends TreeRenderers with TreeEditors { 

  val Path = collection.immutable.IndexedSeq
  type Path[+A] = collection.immutable.IndexedSeq[A]
  
  /**
  *  The style of lines drawn between tree nodes.
  */
  object LineStyle extends Enumeration {
    val Angled = Value("Angled")
    val None = Value("None")
    
   
    // "Horizontal" is omitted; it does not display as expected, because of the hidden root; it only shows lines 
    // for the top level.
    // val Horizontal = Value("Horizontal")
  }
  
  object SelectionMode extends Enumeration {
    val Contiguous = Value(jst.TreeSelectionModel.CONTIGUOUS_TREE_SELECTION)
    val Discontiguous = Value(jst.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
    val Single = Value(jst.TreeSelectionModel.SINGLE_TREE_SELECTION)
  }

  protected trait JTreeMixin[A] { def treeWrapper: Tree[A] }
}



/**
 * Wrapper for a JTree.  The tree model is represented by a 
 * lazy child expansion function that may or may not terminate in leaf nodes.
 *
 * The tree publishes structural events, such as nodes being added or removed, on its main publisher,
 * whereas selection changes are published to the dedicated [[Tree#selection]] object.
 * 
 * @see javax.swing.JTree
 */
class Tree[A](private var treeDataModel: TreeModel[A] = TreeModel.empty[A])
  extends Component
  with CellView[A]
  with EditableCells[A]
  with RenderableCells[A]
  with Scrollable.Wrapper { thisTree =>

  import Tree._  

  override val companion = Tree

  override lazy val peer: js.JTree = new js.JTree(model.peer) with JTreeMixin[A] {
    def treeWrapper = thisTree
    
    // We keep the true root node as an invisible and empty value; the user's data will 
    // always sit visible beneath it.  This is so we can support multiple "root" nodes from the 
    // user's perspective, while maintaining type safety.
    setRootVisible(false)
  }
  
  protected def scrollablePeer = peer
  
  /**
   * Implicitly converts Tree.Path[A] lists to TreePath objects understood by the underlying peer JTree. 
   * In addition to the objects in the list, the JTree's hidden root node must be prepended.
   */
  implicit def pathToTreePath(p: Path[A]): jst.TreePath = model pathToTreePath p
 
  /**
   * Implicitly converts javax.swing.tree.TreePath objects to Tree.Path[A] lists recognised in Scala Swing.  
   * TreePaths will include the underlying JTree's hidden root node, which is omitted for Tree.Paths.
   */
  implicit def treePathToPath(tp: jst.TreePath): Path[A] = model treePathToPath tp

  /**
   *  Implicit method to produce a generic editor.
   *  This lives in the Tree class rather than the companion object because it requires 
   *  an actual javax.swing.JTree instance to be initialised.
   */
  implicit def genericEditor[B] = new Editor[B] {
    // Get the current renderer if it passes for a DefaultTreeCellRenderer, or create a new one otherwise.
    def rendererIfDefault = if (renderer != null) {
      renderer.peer match {
        case defRend: jst.DefaultTreeCellRenderer => defRend
        case _ => new jst.DefaultTreeCellRenderer
      }
    } else new jst.DefaultTreeCellRenderer
    
    override lazy val peer: jst.TreeCellEditor = new jst.DefaultTreeCellEditor(thisTree.peer, rendererIfDefault) {
      listenToPeer(this)
    }
    override def componentFor(tree: Tree[_], a: B, info: Editor.CellInfo): Component = {
      val c = peer.getTreeCellEditorComponent(tree.peer, a, info.isSelected, info.isExpanded, 
          info.isLeaf, info.row)
          
      // Unfortunately the underlying editor peer returns a java.awt.Component, not a javax.swing.JComponent.
      // Since there is currently no way to wrap a java.awt.Component in a scala.swing.Component, we need to 
      // wrap it in a JComponent somehow.

      val jComp = new js.JPanel(new java.awt.GridLayout(1,1))
      jComp.add(c)
      Component.wrap(jComp) // Needs to wrap JComponent
    }
    def value = peer.getCellEditorValue.asInstanceOf[B]
  }
  
  /**
   * Selection model for Tree.
   *
   * To observe tree selections, make the reactor listen to this publishing object which will then dispatch
   * instances of [[scalaswingcontrib.event.TreePathSelected]].
   */
  object selection extends CellSelection {
  
    object rows extends SelectionSet(peer.getSelectionRows) {
      def -=(r: Int) = { peer.removeSelectionRow(r); this }
      def +=(r: Int) = { peer.addSelectionRow(r);    this }
      def --=(rs: Seq[Int]) = { peer.removeSelectionRows(rs.toArray); this }
      def ++=(rs: Seq[Int]) = { peer.addSelectionRows(   rs.toArray); this }
      def maxSelection  = peer.getMaxSelectionRow
      def minSelection  = peer.getMinSelectionRow
      def leadSelection = peer.getLeadSelectionRow
    }

    object paths extends SelectionSet[Path[A]]({
      val p = peer.getSelectionPaths
      if (p == null) Seq.empty else p.map(treePathToPath)(breakOut)
    }) {
      def -=(p: Path[A]) = { peer.removeSelectionPath(p); this }
      def +=(p: Path[A]) = { peer.addSelectionPath(p); this }
      def --=(ps: Seq[Path[A]]) = { peer.removeSelectionPaths(ps.map(pathToTreePath).toArray); this }
      def ++=(ps: Seq[Path[A]]) = { peer.addSelectionPaths(ps.map(pathToTreePath).toArray); this }
      def leadSelection: Option[Path[A]] = Option(peer.getLeadSelectionPath)
    }

    peer.getSelectionModel.addTreeSelectionListener(new jse.TreeSelectionListener {
      def valueChanged(e: jse.TreeSelectionEvent) {
        val (pathsAdded, pathsRemoved) = e.getPaths.toList.partition(e.isAddedPath)
        
        publish(new TreePathSelected(thisTree, 
                pathsAdded map treePathToPath,
                pathsRemoved map treePathToPath,
                Option(e.getNewLeadSelectionPath: Path[A]), 
                Option(e.getOldLeadSelectionPath: Path[A])))
      }
    })
    
    def cellValues: Iterator[A] = paths.iterator.map(_.last)
    
    
    def mode             = Tree.SelectionMode(peer.getSelectionModel.getSelectionMode)
    def mode_=(m: Tree.SelectionMode.Value) { peer.getSelectionModel.setSelectionMode(m.id) }
    def selectedNode: Option[A] = Option(peer.getLastSelectedPathComponent.asInstanceOf[A])
    def isEmpty          = peer.isSelectionEmpty
    def size             = peer.getSelectionCount

    def clear() { peer.clearSelection() }
  }

  
  protected val modelListener = new jse.TreeModelListener {
    override def treeStructureChanged(e: jse.TreeModelEvent) {
      publish(TreeStructureChanged[A](Tree.this, e.getPath.asInstanceOf[Array[A]].toIndexedSeq, 
              e.getChildIndices.toList, e.getChildren.asInstanceOf[Array[A]].toList))
    }
    override def treeNodesInserted(e: jse.TreeModelEvent) {
      publish(TreeNodesInserted[A](Tree.this, e.getPath.asInstanceOf[Array[A]].toIndexedSeq, 
              e.getChildIndices.toList, e.getChildren.asInstanceOf[Array[A]].toList))
    }
    override def treeNodesRemoved(e: jse.TreeModelEvent) {
      publish(TreeNodesRemoved[A](Tree.this, e.getPath.asInstanceOf[Array[A]].toIndexedSeq, 
              e.getChildIndices.toList, e.getChildren.asInstanceOf[Array[A]].toList))
    }
    def treeNodesChanged(e: jse.TreeModelEvent) {
      publish(TreeNodesChanged[A](Tree.this, e.getPath.asInstanceOf[Array[A]].toIndexedSeq, 
              e.getChildIndices.toList, e.getChildren.asInstanceOf[Array[A]].toList))
    }
  }
  
  def isVisible( path: Path[A]) = peer isVisible  path
  def expandPath(path: Path[A]) { peer expandPath path }
  def expandRow( row: Int)      { peer expandRow  row  }

  /**
   * Expands every row. Will not terminate if the tree is of infinite depth.
   */
  def expandAll() {
    var i = 0
    while (i < rowCount) {
      expandRow(i)
      i += 1
    }
  } 
  
  def collapsePath(path: Path[A]) { peer collapsePath path }
  def collapseRow(row: Int)       { peer collapseRow  row  }

  def model = treeDataModel
  
  def model_=(tm: TreeModel[A]) {
    if (treeDataModel != null)
      treeDataModel.peer.removeTreeModelListener(modelListener)
      
    treeDataModel = tm
    peer.setModel(tm.peer)
    treeDataModel.peer.addTreeModelListener(modelListener)
  }
  
  override def cellValues: Iterator[A] = model.depthFirstIterator
  
  /**
   * Collapses all visible rows.
   */
  def collapseAll() { rowCount-1 to 0 by -1 foreach collapseRow }
  
  def isExpanded( path: Path[A]) = peer isExpanded  path
  def isCollapsed(path: Path[A]) = peer isCollapsed path
  
  def isEditing = peer.isEditing
  
  def editable: Boolean      = peer.isEditable
  def editable_=(b: Boolean) { peer.setEditable(b) }
  
  def editor: Editor[A] = Editor.wrap(peer.getCellEditor)
  def editor_=(r: Tree.Editor[A])   { peer.setCellEditor(r.peer); editable = true }
  
  def renderer: Renderer[A] = Renderer.wrap(peer.getCellRenderer)
  def renderer_=(r: Tree.Renderer[A])     { peer.setCellRenderer(r.peer) }
  
  def showsRootHandles               = peer.getShowsRootHandles
  def showsRootHandles_=(b: Boolean) { peer.setShowsRootHandles(b) }
  
  def startEditingAtPath(path: Path[A]) { peer.startEditingAtPath(pathToTreePath(path)) }

  def getRowForLocation(x: Int, y: Int): Int = peer.getRowForLocation(x, y)
  def getRowForPath(path: Path[A]) : Int     = peer.getRowForPath(pathToTreePath(path))
  def getClosestPathForLocation(x: Int, y: Int): Option[Path[A]] = Option(peer.getClosestPathForLocation(x, y))
  def getClosestRowForLocation( x: Int, y: Int): Int      = peer.getClosestRowForLocation( x, y)
  
  def lineStyle        = Tree.LineStyle withName peer.getClientProperty("JTree.lineStyle").toString
  def lineStyle_=(style: Tree.LineStyle.Value) { peer.putClientProperty("JTree.lineStyle", style.toString) }

  
  // Follows the naming convention of ListView.selectIndices()
  def selectRows(rows: Int*)                { peer.setSelectionRows(rows.toArray) }
  def selectPaths(paths: Path[A]*)          { peer.setSelectionPaths(paths.map(pathToTreePath).toArray) }
  def selectInterval(first: Int, last: Int) { peer.setSelectionInterval(first, last) }
  
  def rowCount    = peer.getRowCount
  def rowHeight   = peer.getRowHeight
  def largeModel  = peer.isLargeModel
  def scrollableTracksViewportHeight = peer.getScrollableTracksViewportHeight
  def expandsSelectedPaths               = peer.getExpandsSelectedPaths
  def expandsSelectedPaths_=(b: Boolean) { peer.setExpandsSelectedPaths(b) }
  def dragEnabled               = peer.getDragEnabled
  def dragEnabled_=(b: Boolean) { peer.setDragEnabled(b) }

  def visibleRowCount: Int         = peer.getVisibleRowCount
  def visibleRowCount_=(rows: Int) { peer.setVisibleRowCount(rows) }
  def makeVisible(path: Path[A])   { peer.makeVisible(pathToTreePath(path)) }
  def cancelEditing()              { peer.cancelEditing() }
  def stopEditing(): Boolean     = { peer.stopEditing() }
  def editingPath: Option[Path[A]] = Option(peer.getEditingPath)
}
