package scalaswingcontrib
package event

import scala.swing.event.Event


trait CellEditorEvent[A] extends Event {
  val source: CellEditor[A]
}
case class CellEditingStopped[A](source: CellEditor[A]) extends CellEditorEvent[A]
case class CellEditingCancelled[A](source: CellEditor[A]) extends CellEditorEvent[A]
