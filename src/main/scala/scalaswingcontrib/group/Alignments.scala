package scalaswingcontrib.group

import javax.{swing => js}

/** Provides alignment constants for parallel groups in a `GroupPanel`.
  * 
  * @author Andreas Flierl
  */
trait Alignments {
  /**
   * Represents an alignment of a component (or group) within a parallel group.
   * 
   * @see javax.swing.GroupLayout.Alignment
   */
  protected final class Alignment(private[group] val wrapped: js.GroupLayout.Alignment)
  
  /** Elements are aligned along their baseline. Only valid along the vertical axis. */
  final val Baseline = new Alignment(js.GroupLayout.Alignment.BASELINE)
  
  /** Elements are centered inside the group. */
  final val Center = new Alignment(js.GroupLayout.Alignment.CENTER)
  
  /** Elements are anchored to the leading edge (origin) of the group. */
  final val Leading = new Alignment(js.GroupLayout.Alignment.LEADING)
  
  /** Elements are anchored to the trailing edge (end) of the group. */
  final val Trailing = new Alignment(js.GroupLayout.Alignment.TRAILING)
}
