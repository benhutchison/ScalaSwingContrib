package scalaswingcontrib

import java.awt.Cursor
import java.awt.datatransfer.{Clipboard, Transferable, DataFlavor}
import java.awt.dnd.DragSource
import javax.activation.{DataHandler, ActivationDataFlavor}
import javax.swing.JTable.DropLocation
import javax.swing._

import scala.reflect.ClassTag
import scalaswingcontrib.tree.Tree

// adapted from http://stackoverflow.com/a/4769575/16673

trait ClipboardCallbacks {
  def paste(xf:Transferable) = {}
  def copy(clip: Clipboard) = {}
  def cut(clip: Clipboard) = {}
}

trait Reorderable {
  def reorder(from: Int, to: Int): Unit
}

object RowTransferable {
  val localObjectFlavor = new ActivationDataFlavor(classOf[RowTransferable], DataFlavor.javaJVMLocalObjectMimeType,
    "Integer Row Index")
}

import RowTransferable._

private class RowTransferable(val row: Int, val from: JComponent) extends Transferable {

  override def getTransferData(flavor: DataFlavor): AnyRef = if (flavor == localObjectFlavor) this else null

  override def getTransferDataFlavors: Array[DataFlavor] = Array(localObjectFlavor)

  override def isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == localObjectFlavor
}

abstract class TransferRowContainer[Table: ClassTag, DropLocation: ClassTag] {
  def isContainer(c: java.awt.Component): Boolean
  def container: Table
  def selectedRow: Int
  def setCursor(cursor: Cursor): Unit

  def handleDrop(rowFrom: RowTransferable, dl: DropLocation): Boolean
}

class TransferHandlerRow[Table: ClassTag, DropLocation: ClassTag](
  private val container: TransferRowContainer[Table, DropLocation], val clipboardCallbacks: ClipboardCallbacks
) extends TransferHandler {

  protected override def createTransferable(c: JComponent): Transferable = {
    assert(container.isContainer(c))
    new DataHandler(new RowTransferable(container.selectedRow, c), localObjectFlavor.getMimeType)
  }

  override def canImport(info: TransferHandler.TransferSupport): Boolean = {
    val b = container.isContainer(info.getComponent) && info.isDrop && info.isDataFlavorSupported(localObjectFlavor)
    container.setCursor(if (b) DragSource.DefaultMoveDrop else DragSource.DefaultMoveNoDrop)
    b
  }

  override def getSourceActions(c: JComponent): Int = TransferHandler.COPY_OR_MOVE

  override def importData(info: TransferHandler.TransferSupport): Boolean = {
    if (true) {
      if (info.isDrop) {
        val dl = info.getDropLocation.asInstanceOf[DropLocation]
        container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
        try {
          val rowFrom = info.getTransferable.getTransferData(localObjectFlavor).asInstanceOf[RowTransferable]
          // may return null when dropping from a different JVM (application instance)
          container.handleDrop(rowFrom, dl)
        } catch {
          case e: Exception =>
            e.printStackTrace()
            false
        }
      } else {
        clipboardCallbacks.paste(info.getTransferable)
        true
      }
    } else {
      true
    }
  }

  override def exportToClipboard(comp: JComponent, clip: Clipboard, action: Int): Unit = {
    // action == TransferHandler.MOVE means cut, TransferHandler.MOVE means copy
    exportDone(comp, null, TransferHandler.NONE)
  }

  protected override def exportDone(c: JComponent, t: Transferable, act: Int): Unit = {
    act match {
      case TransferHandler.MOVE =>
        container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
      case TransferHandler.NONE =>
        container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
    }
  }
}


class TransferRowContainerTable(override val container: JTable) extends TransferRowContainer[JTable, JTable.DropLocation] {
  private def dropRow(dl: DropLocation) = dl.getRow
  private def rowCount = container.getRowCount
  private def select(index: Int) = container.getSelectionModel.setSelectionInterval(index, index)
  private def reorder(from: Int, to: Int) = container.getModel.asInstanceOf[Reorderable].reorder(from, to)


  override def isContainer(c: java.awt.Component): Boolean = container == c
  override def selectedRow = container.getSelectedRow
  override def setCursor(cursor: Cursor) = container.setCursor(cursor)

  override def handleDrop(rowFrom: RowTransferable, dl: DropLocation) = {
    var index = dropRow(dl)
    val max = rowCount
    if (index < 0 || index > max) index = max
    if (rowFrom != null && rowFrom.from == container && rowFrom.row != -1 && rowFrom.row != index) {
      reorder(rowFrom.row, index)
      if (index > rowFrom.row) index -= 1
      select(index)
      true
    }
    else false
  }
}

class TableTransferHandlerRow(table: JTable, clipboardCallbacks: ClipboardCallbacks)
  extends TransferHandlerRow[JTable, JTable.DropLocation](new TransferRowContainerTable(table), clipboardCallbacks)


class TransferRowContainerTree[A](override val container: Tree[A]) extends TransferRowContainer[Tree[A], JTree.DropLocation] {
  private def tree =  container
  override def isContainer(c: java.awt.Component): Boolean = container.peer == c
  override def selectedRow = container.peer.getLeadSelectionRow
  override def setCursor(cursor: Cursor) = container.peer.setCursor(cursor)
  override def handleDrop(rowFrom: RowTransferable, dl: JTree.DropLocation): Boolean = {
    // import container._ // shorter code, but perhaps dangerous conversions?
    val pathFrom = tree.treePathToPath(tree.peer.getPathForRow(rowFrom.row))
    val pathTo = tree.treePathToPath(dl.getPath)
    if (dl.getChildIndex >= 0) {
      // DropMode.ON - path is the node
      tree.model.move(pathFrom, pathTo, dl.getChildIndex)
    } else {
      //DropMode.INSERT
      tree.model.move(pathFrom, pathTo, tree.model.getChildrenOf(pathTo).size)
    }
  }
}

class TreeTransferHandlerRow[A](tree: Tree[A], clipboardCallbacks: ClipboardCallbacks)
  extends TransferHandlerRow[Tree[A], JTree.DropLocation](new TransferRowContainerTree(tree), clipboardCallbacks)
