package scalaswingcontrib

import scala.swing.{Component, Publisher}
import scalaswingcontrib.event.{CellEditingCancelled, CellEditingStopped}
import javax.{swing => js}
import javax.swing.{event => jse}

/**
* Describes the structure of a component's companion object where pluggable cell editors must be supported.
* @author Ken Scambler
*/
trait EditableCellsCompanion {
  type Editor[A] <: CellEditor[A]
  protected type Owner <: Component with CellView[_]
  
  val Editor: CellEditorCompanion
  

  trait CellEditorCompanion {
    type Peer <: js.CellEditor
    type CellInfo
    val emptyCellInfo: CellInfo
    def wrap[A](e: Peer): Editor[A]
    def apply[A, B: Editor](toB: A => B, toA: B => A): Editor[A]
  }
  
  trait CellEditor[A] extends Publisher with scalaswingcontrib.CellEditor[A] {
    val companion: CellEditorCompanion
    def peer: companion.Peer

    protected def fireCellEditingCancelled() { publish(CellEditingCancelled(CellEditor.this)) }
    protected def fireCellEditingStopped() { publish(CellEditingStopped(CellEditor.this)) }

    protected def listenToPeer(p: js.CellEditor) {
      p.addCellEditorListener(new jse.CellEditorListener {
        override def editingCanceled(e: jse.ChangeEvent) { fireCellEditingCancelled() }
        override def editingStopped(e: jse.ChangeEvent) { fireCellEditingStopped() }
      })
    }

    abstract class EditorPeer extends js.AbstractCellEditor {
      override def getCellEditorValue(): AnyRef = value.asInstanceOf[AnyRef]
      listenToPeer(this)
    }

    def componentFor(owner: Owner, value: A, cellInfo: companion.CellInfo): Component
    
    def cellEditable = peer.isCellEditable(null)
    def shouldSelectCell = peer.shouldSelectCell(null)
    def cancelCellEditing() = peer.cancelCellEditing
    def stopCellEditing() = peer.stopCellEditing
  }
}
