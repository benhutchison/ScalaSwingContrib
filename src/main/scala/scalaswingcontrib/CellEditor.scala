package scalaswingcontrib

import scala.swing.Publisher

/**
* Common superclass of cell editors.
* @author Ken Scambler
*/
trait CellEditor[+A] extends Publisher {
  def peer: AnyRef
  def value: A
  def cellEditable: Boolean
  def shouldSelectCell: Boolean
  def cancelCellEditing(): Unit
  def stopCellEditing(): Boolean
}


