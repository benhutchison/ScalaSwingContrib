package scalaswingcontrib
package event

import tree.Tree
import swing.event.{SelectionEvent, ComponentEvent}

sealed trait TreeEvent[A] extends ComponentEvent {
  val source: Tree[A]
}

object TreeNodeSelected {
  def unapply(any: Any) = any match {
    case TreePathSelected(_, _, _, newPath, _) => newPath map (_.last)
    case _ => None
  }
}

final case class TreePathSelected[A](source: Tree[A],
                                     newPaths: List[Tree.Path[A]],
                                     oldPaths: List[Tree.Path[A]],
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
