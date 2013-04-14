package scalaswingcontrib
package event

import tree.Tree
import swing.event.{SelectionEvent, ComponentEvent}

/** The common trait of all events dispatched by a [[scalaswingcontrib.tree.Tree]]. */
sealed trait TreeEvent[A] extends ComponentEvent {
  val source: Tree[A]
}

object TreeNodeSelected {
  def unapply(any: Any): Option[Any] = any match {
    case TreePathSelected(_, _, _, newLeadSelectionPath, _) => newLeadSelectionPath map (_.last)
    case _ => None
  }
}

/** The event denoting a change in selected paths of a [[scalaswingcontrib.tree.Tree]].
  *
  * The event provides information about which paths have been removed or added to the selection. For example,
  * if a tree contains three elements, A, B, C, and initially A was selected, then an additional selection of
  * B will produce an event which contains the path to B in `pathsAdded`, whereas `pathsRemoved` is empty (no elements
  * were deselected). If in the next step, the selection is replaced by C, an event will be produced whose
  * `pathsAdded` contains `C` and whose `pathsRemoved` contains A and B.
  *
  * If the client does not want to keep track of the total set of selected paths, it may retrieve them through
  * the tree's `selection` object.
  *
  * For convenience, the event contains the optional first paths in the previous and current total selection set.
  * In the example above, assuming an order of A, B, C, the first event will show A as both
  * `newLeadSelectionPath` and `oldLeadSelectionPath`. The second example event will show A in
  * `oldLeadSelectionPath` and `C` in `newLeadSelectionPath`.
  *
  * There is an additional extractor in the `TreePathSelected` companion object which yields the last
  * element the new lead selection path.
  *
  * @param source   the tree in which the selection has been made
  * @param pathsAdded a list of paths which have been added to the selection
  * @param pathsRemoved a list of paths which have been removed from the selection
  * @param newLeadSelectionPath the first path in the list of currently selected paths (if any)
  * @param oldLeadSelectionPath the first path in the list of previously selected paths (if any)
  */
final case class TreePathSelected[A](source: Tree[A],
                                     pathsAdded: List[Tree.Path[A]],
                                     pathsRemoved: List[Tree.Path[A]],
                                     newLeadSelectionPath: Option[Tree.Path[A]],
                                     oldLeadSelectionPath: Option[Tree.Path[A]])
  extends TreeEvent[A] with SelectionEvent

sealed trait TreeExpansionEvent[A] extends TreeEvent[A] {
  def path: Tree.Path[A]
}
final case class TreeCollapsed[A](   source: Tree[A], path: Tree.Path[A]) extends TreeExpansionEvent[A]
final case class TreeExpanded[A](    source: Tree[A], path: Tree.Path[A]) extends TreeExpansionEvent[A]
final case class TreeWillCollapse[A](source: Tree[A], path: Tree.Path[A]) extends TreeExpansionEvent[A]
final case class TreeWillExpand[A](  source: Tree[A], path: Tree.Path[A]) extends TreeExpansionEvent[A]

sealed trait TreeModelEvent[A] extends TreeEvent[A] {
  def path: Tree.Path[A]
  def childIndices: List[Int]
  def children: List[A]
}
final case class TreeNodesChanged[A](    source: Tree[A], path: Tree.Path[A], childIndices: List[Int], children: List[A]) extends TreeModelEvent[A]
final case class TreeNodesInserted[A](   source: Tree[A], path: Tree.Path[A], childIndices: List[Int], children: List[A]) extends TreeModelEvent[A]
final case class TreeNodesRemoved[A](    source: Tree[A], path: Tree.Path[A], childIndices: List[Int], children: List[A]) extends TreeModelEvent[A]
final case class TreeStructureChanged[A](source: Tree[A], path: Tree.Path[A], childIndices: List[Int], children: List[A]) extends TreeModelEvent[A]
