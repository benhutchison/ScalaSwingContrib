package scalaswingcontrib.group

import scala.swing.Component
import javax.{swing => js}
import scala.language.implicitConversions

/** Provides an implicit conversion and wrappers so that arbitrary Swing
  * components may be placed inside a `GroupPanel` but still be checked for
  * validity at compile time.
  *
  * @author Andreas Flierl
  */
trait ComponentsInGroups extends SizeTypes { this: GroupPanel =>
  /**
   * Implicit conversion that puts a component into the correct context on demand.
   */
  protected final implicit def add[A <: G](comp: Component): ComponentInGroup[A] =
    new ComponentInGroup[A](comp)

  /** Triplet of minimum, preferred and maximum size. */
  private[group] case class Sizes(min: Size, pref: Size, max: Size)

  /**
   * Wraps an arbitrary component so that it may appear within a group of
   * type `A`.
   */
  protected class ComponentInGroup[A <: G](comp: Component)
      extends InGroup[A] with SizeHelpers[A] {

    override private[group] def build(parent: A) = parent.addComponent(comp.peer)

    /**
     * Specifies size constraints for this component.
     *
     * @param min minimum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param pref preferred size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param max maximum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     */
    def sized(min: Size, pref: Size, max: Size): InGroup[A] =
      new ComponentWithSize[A](comp, Sizes(min, pref, max))

    /** Specifies that this component should be used to calculate the baseline
      * of its surrounding group, which must be sequential. This is needed to
      * align that sequential group along the baseline of a baseline-aligned
      * parallel group that surrounds it.
      *
      * Only *one* component in a sequential group should be marked this way.
      * If there are several, the last one marked will be used.
      */
    def asBaseline = new ComponentInSequential(comp, None, true)

    /**
     * Specifies an alignment for this component. May only be used inside a
     * parallel group.
     *
     * @param newAlign the alignment to use
     */
    def aligned(newAlign: Alignment) = new ComponentInParallel(comp, None, newAlign)
  }

  /**
   * Wraps an arbitrary component to allow for custom size constraints.
   */
  protected class ComponentWithSize[A <: G](comp: Component,
      sizes: Sizes) extends InGroup[A] {
    override private[group] def build(parent: A) =
      parent.addComponent(comp.peer, sizes.min.pixels, sizes.pref.pixels,
                          sizes.max.pixels)

    /** Specifies that this component should be used to calculate the baseline
      * of its surrounding group, which must be sequential. This is needed to
      * align that sequential group along the baseline of a baseline-aligned
      * parallel group that surrounds it.
      *
      * Only *one* component in a sequential group should be marked this way.
      * If there are several, the last one marked will be used.
      */
    def asBaseline = new ComponentInSequential(comp, Some(sizes), true)

    /**
     * Specifies an alignment for this component. May only be used inside a
     * parallel group.
     *
     * @param newAlign the alignment to use
     */
    def aligned(newAlign: Alignment) =
      new ComponentInParallel(comp, Some(sizes), newAlign)
  }

  /**
   * Wraps a GUI component so that it may appear in a sequential group.
   *
   * @see javax.swing.GroupLayout.SequentialGroup
   */
  protected class ComponentInSequential(comp: Component,
      sizes: Option[Sizes], useAsBaseline: Boolean)
      extends InSequential with SizeHelpers[js.GroupLayout#SequentialGroup] {

    override private[group] def build(parent: js.GroupLayout#SequentialGroup) =
      if (sizes.isDefined)
        parent.addComponent(useAsBaseline, comp.peer, sizes.get.min.pixels,
            sizes.get.pref.pixels, sizes.get.max.pixels)
      else parent.addComponent(useAsBaseline, comp.peer)

    /**
     * Specifies size constraints for this component.
     *
     * @param min minimum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param pref preferred size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param max maximum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     */
    def sized(min: Size, pref: Size, max: Size): InSequential =
      new ComponentInSequential(comp, Some(Sizes(min, pref, max)), useAsBaseline)
  }

  /**
   * Wraps a GUI component so that it may appear in a parallel group.
   *
   * @see javax.swing.GroupLayout.ParallelGroup
   */
  protected class ComponentInParallel(comp: Component,
      sizes: Option[Sizes], align: Alignment)
      extends InParallel with SizeHelpers[js.GroupLayout#ParallelGroup] {

    override private[group] def build(parent: js.GroupLayout#ParallelGroup) =
      if (sizes.isDefined)
        parent.addComponent(comp.peer, align.wrapped, sizes.get.min.pixels,
            sizes.get.pref.pixels, sizes.get.max.pixels)
      else parent.addComponent(comp.peer, align.wrapped)

    /**
     * Specifies size constraints for this component.
     *
     * @param min minimum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param pref preferred size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param max maximum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     */
    def sized(min: Size, pref: Size, max: Size): InParallel =
      new ComponentInParallel(comp, Some(Sizes(min, pref, max)), align)
  }

  /**
   * Additional methods for nicer control over resizing behaviour.
   */
  private[group] trait SizeHelpers[A <: G] {
    /**
     * Specifies size constraints for this component.
     *
     * @param min minimum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param pref preferred size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     * @param max maximum size >= 0 (or one of `UseDefault`, `UsePreferred` and `Infinite`)
     */
    def sized(min: Size, pref: Size, max: Size): InGroup[A]

    /**
     * Fixes the size of this component to its default size.
     */
    def fixedToDefaultSize = sized(UsePreferred, UseDefault, UsePreferred)

    /**
     * Fixes the size of this component to the specified size.
     *
     * @param size the desired size in pixels
     */
    def sized(size: Size): InGroup[A] = sized(UsePreferred, size, UsePreferred)

    /**
     * Forces this component to be resizable (useful e.g. for buttons). Its
     * minimum size is set to its default size.
     */
    def resizable(min: Size = UseDefault, pref: Size = UseDefault, max: Size = Infinite) =
      sized(min, pref, max)

    /**
     * Forces this component to be resizable (useful e.g. for buttons). Its
     * minimum size is set to 0 pixels.
     */
    def fullyResizable(pref: Size = UseDefault) = sized(0, pref, Infinite)
  }
}
