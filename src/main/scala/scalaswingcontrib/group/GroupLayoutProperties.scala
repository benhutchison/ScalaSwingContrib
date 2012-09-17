package scalaswingcontrib.group

import javax.{swing => js}

/** Property wrappers for `GroupLayout`'s setters and getters.
  * 
  * @author Andreas Flierl
  */
trait GroupLayoutProperties {
  def layout: js.GroupLayout
  
  /** Indicates whether gaps between components are automatically created. */
  def autoCreateGaps = layout.getAutoCreateGaps
  
  /** Sets whether gaps between components are automatically created. */
  def autoCreateGaps_=(flag: Boolean) = layout.setAutoCreateGaps(flag)
  
  /** 
   * Indicates whether gaps between components and the container borders are 
   * automatically created. 
   */
  def autoCreateContainerGaps = layout.getAutoCreateContainerGaps
  
  /** 
   * Sets whether gaps between components and the container borders are 
   * automatically created. 
   */
  def autoCreateContainerGaps_=(flag: Boolean) = 
    layout.setAutoCreateContainerGaps(flag)
  
  /** Returns the layout style used. */
  def layoutStyle = layout.getLayoutStyle
  
  /** Assigns a layout style to use. */
  def layoutStyle_=(style: js.LayoutStyle) = layout.setLayoutStyle(style)
  
  /** 
   * Indicates whether the visibilty of components is considered for the layout.
   * If set to `false`, invisible components still take up space.
   * Defaults to `true`.
   */
  def honorsVisibility = layout.getHonorsVisibility
  
  /**
   * Sets whether the visibilty of components should be considered for the 
   * layout. If set to `false`, invisible components still take up 
   * space. Defaults to `true`. 
   */
  def honorsVisibility_=(flag: Boolean) =
    layout.setHonorsVisibility(flag)
}
