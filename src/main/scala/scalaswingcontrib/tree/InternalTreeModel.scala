package scalaswingcontrib
package tree

import javax.swing.{tree => jst}
import Tree.Path
import TreeModel.hiddenRoot
import scala.collection.JavaConversions.enumerationAsScalaIterator
import InternalTreeModel.{PeerModel, PeerNode}
import scala.collection.breakOut

object InternalTreeModel {
  
  def empty[A] = new InternalTreeModel[A](new PeerModel(new jst.DefaultMutableTreeNode(hiddenRoot)))
  
  def apply[A](roots: A*)(children: A => Seq[A]): InternalTreeModel[A] = {
    def createNode(a: A): PeerNode = {
      val node = new PeerNode(a)
      children(a) map createNode foreach node.add
      node
    }
    
    val rootNode = new PeerNode(hiddenRoot)
    roots map createNode foreach rootNode.add
    
    new InternalTreeModel(new PeerModel(rootNode))
  }
  
  private[tree] type PeerModel = jst.DefaultTreeModel
  private[tree] type PeerNode  = jst.DefaultMutableTreeNode
}


class InternalTreeModel[A] private (val peer: PeerModel) extends TreeModel[A] { 
  self =>
    
  def this() = this(new PeerModel(new PeerNode(hiddenRoot)))
    
  def pathToTreePath(path: Path[A]): jst.TreePath = {
    
    val nodePath = path.foldLeft(List(rootPeerNode)) { (pathList, a) => 
      val childNodes = getNodeChildren(pathList.head)
      val node = childNodes.find(_.getUserObject == a) getOrElse sys.error("Couldn't find internal node for " + a)
      node :: pathList
    }.reverse

    val array = nodePath.toArray[AnyRef]
    new jst.TreePath(array)
  }

  def treePathToPath(tp: jst.TreePath): Path[A] = {
    if (tp == null) null 
    else ((tp.getPath map unpackNode)(breakOut): Path[A]).tail
  } 
  
  private def rootPeerNode = peer.getRoot.asInstanceOf[PeerNode]

  def roots: Seq[A] = getNodeChildren(rootPeerNode) map unpackNode
  
  def update(path: Path[A], newValue: A) {
    peer.valueForPathChanged(pathToTreePath(path), newValue)
  }

  private def getPeerNodeAt(path: Path[A]): PeerNode = {
    pathToTreePath(path).getLastPathComponent.asInstanceOf[PeerNode]
  }
  
  def insertUnder(parentPath: Path[A], newValue: A, index: Int): Boolean = {
    peer.insertNodeInto(new PeerNode(newValue), getPeerNodeAt(parentPath), index)
    true
  }
  
  def remove(pathToRemove: Path[A]): Boolean = {
    peer.removeNodeFromParent(getPeerNodeAt(pathToRemove))
    true
  }

  def move(pathFrom: Path[A], pathTo: Path[A], indexTo: Int): Boolean = {
    val node = getPeerNodeAt(pathFrom)
    peer.removeNodeFromParent(node)
    peer.insertNodeInto(node, getPeerNodeAt(pathTo), indexTo)
    true
  }

  
  def map[B](f: A => B): InternalTreeModel[B] = new InternalTreeModel[B] {
    override val peer = copyFromModel(self, f)
  }

  protected[tree] def copyFromModel[B](otherModel: TreeModel[B], f: B => A): jst.DefaultTreeModel = {
    def copyNodeAt(bPath: Path[B]): PeerNode = {
      val copiedNode     = new PeerNode(f(bPath.last))
      val otherChildren  = otherModel.getChildrenOf(bPath)
      val copiedChildren = otherChildren map { b => copyNodeAt(bPath :+ b) }
      copiedChildren foreach copiedNode.add
      copiedNode
    }
    
    val rootNode = new PeerNode(hiddenRoot)
    val children = otherModel.roots map { b => copyNodeAt(Path(b)) }
    children foreach rootNode.add
    new jst.DefaultTreeModel(rootNode)
  }
  
  private def getNodeChildren(node: PeerNode): Seq[PeerNode] = node.children.toSeq.asInstanceOf[Seq[PeerNode]]
  
  def getChildrenOf(parentPath: Path[A]): Seq[A] = {
    val lastNode = pathToTreePath(parentPath).getLastPathComponent.asInstanceOf[PeerNode]
    getNodeChildren(lastNode) map unpackNode
  }

  def filter(p: A => Boolean): InternalTreeModel[A] = {
    def filterChildren(node: PeerNode): PeerNode = {
      val newNode = new PeerNode(node.getUserObject)
      val okChildren = getNodeChildren(node) filter { n => p(unpackNode(n)) }
      okChildren map filterChildren foreach newNode.add
      newNode
    }
    new InternalTreeModel(new PeerModel(filterChildren(rootPeerNode)))
  }
    
  def toInternalModel: InternalTreeModel[A] = this
  
  def isExternalModel = false
  
  override def unpackNode(node: Any): A = node.asInstanceOf[PeerNode].getUserObject.asInstanceOf[A]

  private[tree] override def isHiddenRoot(node: Any): Boolean = node.asInstanceOf[PeerNode].getUserObject == TreeModel.hiddenRoot
}
