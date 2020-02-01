package scalaswingcontrib

import scala.swing.{Component, Publisher}
import scala.collection.mutable

/**
* Describes components that have a concept of a "cell", each of which contains a value, may be selected, 
* and may support pluggable Renderers and Editors.
*/
trait CellView[+A] {
  this: Component =>
    
  def editable: Boolean 
  def cellValues: Iterator[A]
  
  /**
  * Provides common functionality for the `selection` object found in CellView implementation.  Each 
  * will have one or more selection sets based on different types of cell coordinate, such as row, 
  * column, index or tree path.  All events published from `selection` objects will derive from 
  * scala.swing.event.SelectionEvent.
  */
  trait CellSelection extends Publisher {
    /**
    * Allows querying and modification of the current selection state, for some unique coordinate S.
    * There may be more than one selection set supporting different coordinates, such as rows and columns.
    */
    protected abstract class SelectionSet[S](a: => collection.Seq[S]) extends MutableSet[S] {
      def subtractOne(s: S): this.type
      def addOne(s: S): this.type
      def --=(ss: collection.Seq[S]): this.type
      def ++=(ss: collection.Seq[S]): this.type
      override def size = nonNullOrEmpty(a).length
      def contains(s: S) = nonNullOrEmpty(a) contains s
      def iterator = nonNullOrEmpty(a).iterator
      protected def nonNullOrEmpty[A1](s: collection.Seq[A1]) = if (s != null) s else Seq.empty
    }
    
    /**
    *  Returns an iterator that traverses the currently selected cell values.
    */
    def cellValues: Iterator[A]
    
    /**
    * Whether or not the current selection is empty.
    */
    def isEmpty: Boolean
    
    /**
    * Returns the number of cells currently selected.
    */
    def size: Int
  } 
  
  val selection: CellSelection
}

/**
* This should be mixed in to CellView implementations that support pluggable renderers.
*/
trait RenderableCells[A] {
  _: CellView[A] =>
  val companion: RenderableCellsCompanion
  def renderer: companion.Renderer[A]
  def renderer_=(r: companion.Renderer[A]): Unit
}

/**
* This should be mixed in to CellView implementations that support pluggable editors.
*/
trait EditableCells[A]  {
  _: CellView[A] =>
  val companion: EditableCellsCompanion
  def editor: companion.Editor[A]
  def editor_=(r: companion.Editor[A]): Unit
}