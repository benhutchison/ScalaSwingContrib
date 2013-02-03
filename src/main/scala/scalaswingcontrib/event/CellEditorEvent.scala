package scalaswingcontrib
package event

import swing.event.Event

sealed trait CellEditorEvent[+A] extends Event {
  def source: CellEditor[A]
}
final case class CellEditingStopped[A](  source: CellEditor[A]) extends CellEditorEvent[A]
final case class CellEditingCancelled[A](source: CellEditor[A]) extends CellEditorEvent[A]
