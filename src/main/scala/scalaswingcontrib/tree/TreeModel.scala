package scalaswingcontrib
package tree

import Tree.Path
import javax.swing.{tree => jst}

import scala.reflect.ClassTag

object TreeModel {
  
  /**
   * This value is the root node of every TreeModel's underlying javax.swing.tree.TreeModel.  As we wish to support multiple root 
   * nodes in a typesafe manner, we need to maintain a permanently hidden dummy root to hang the user's "root" nodes off.
   */
  private[tree] case object hiddenRoot
  
  def empty[A: ClassTag]: TreeModel[A] = new ExternalTreeModel[A](Seq.empty, _ => Seq.empty)
  def apply[A: ClassTag](roots: A*)(children: A => Seq[A]): TreeModel[A] = new ExternalTreeModel(roots, children)
}


trait TreeModel[A] {
  
  def roots: collection.Seq[A]
  val peer: jst.TreeModel 
  def getChildrenOf(parentPath: Path[A]): collection.Seq[A]
  def getChildPathsOf(parentPath: Path[A]): collection.Seq[Path[A]] = getChildrenOf(parentPath).map(parentPath :+ _)
  def filter(p: A => Boolean): TreeModel[A]
  def map[B: ClassTag](f: A => B): TreeModel[B]
  def foreach[U](f: A => U): Unit = { depthFirstIterator foreach f }
  def isExternalModel: Boolean
  def toInternalModel: InternalTreeModel[A]
  
  
  def pathToTreePath(path: Path[A]): jst.TreePath
  def treePathToPath(tp: jst.TreePath): Path[A]
 
  /**
   * Replace the item at the given path in the tree with a new value. 
   * Events are fired as appropriate.
   */
  def update(path: Path[A], newValue: A): Unit
  def remove(pathToRemove: Path[A]): Boolean
  def insertUnder(parentPath: Path[A], newValue: A, index: Int): Boolean
  
  def insertBefore(path: Path[A], newValue: A): Boolean = {
    if (path.isEmpty) throw new IllegalArgumentException("Cannot insert before empty path")
    
    val parentPath = path.init
    val index = siblingsUnder(parentPath) indexOf path.last
    insertUnder(parentPath, newValue, index)
  }
  
  def insertAfter(path: Path[A], newValue: A): Boolean = {
    if (path.isEmpty) throw new IllegalArgumentException("Cannot insert after empty path")
    
    val parentPath = path.init
    val index = siblingsUnder(parentPath) indexOf path.last
    insertUnder(parentPath, newValue, index + 1)
  }
  
  protected def siblingsUnder(parentPath: Path[A]) = if (parentPath.isEmpty) roots 
                                                     else getChildrenOf(parentPath)
  

  /**
   * Iterates sequentially through each item in the tree, either in breadth-first or depth-first ordering, 
   * as decided by the abstract pushChildren() method.
   */
  private trait TreeIterator extends Iterator[A] {
    protected var openNodes: Iterator[Path[A]] = roots.map(Path(_)).iterator

    def pushChildren(path: Path[A]): Unit
    def hasNext = openNodes.nonEmpty
    def next() = if (openNodes.hasNext) {
      val path = openNodes.next()
      pushChildren(path)
      path.last
    }
    else throw new NoSuchElementException("No more items")
  }
  
  def breadthFirstIterator: Iterator[A] = new TreeIterator {
    override def pushChildren(path: Path[A]): Unit = { openNodes ++= getChildPathsOf(path).toIterator }
  }
  
  def depthFirstIterator: Iterator[A] = new TreeIterator {
    override def pushChildren(path: Path[A]): Unit = {
      val open = openNodes
      openNodes = getChildPathsOf(path).toIterator ++ open // ++'s argument is by-name, and should not directly pass in a var
    }
  }
  
  def size: Int = depthFirstIterator.size
  
  def unpackNode(node: Any): A = node.asInstanceOf[A]

  private [tree] def isHiddenRoot(node: Any): Boolean = node == TreeModel.hiddenRoot
}

