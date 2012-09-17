/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.swing

import javax.swing.JPopupMenu

object PopupMenu {
  private[PopupMenu] trait JPopupMenuMixin { def popupMenuWrapper: PopupMenu }
}

/**
 * A popup menu.
 * 
 * Example usage:
 *
 * {{{
 * val popupMenu = new PopupMenu {
 *   contents += new Menu("menu 1") {
 *     contents += new RadioMenuItem("radio 1.1")
 *     contents += new RadioMenuItem("radio 1.2")
 *   }
 *   contents += new Menu("menu 2") {
 *     contents += new RadioMenuItem("radio 2.1")
 *     contents += new RadioMenuItem("radio 2.2")
 *   }
 * }
 * val button = new Button("Show Popup Menu")
 * reactions += {
 *   case e: ButtonClicked => popupMenu.show(button, 0, button.bounds.height)
 * }
 * listenTo(button)
 * }}}
 * 
 * @see javax.swing.JPopupMenu
 */
class PopupMenu extends Component with SequentialContainer.Wrapper {

  override lazy val peer: JPopupMenu = new JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int): Unit = peer.show(invoker.peer, x, y)
}