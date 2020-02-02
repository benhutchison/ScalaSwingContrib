package scalaswingcontrib

import javax.{swing => js}
import scala.swing.{Component, SequentialContainer}

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

  override lazy val peer: js.JPopupMenu = new js.JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int): Unit = {peer.show(invoker.peer, x, y)}

  def showWithCallback(invoker: Component, x: Int, y: Int, onHide: () => Unit): Unit = {
    val listener = new js.event.PopupMenuListener {
      def popupMenuWillBecomeVisible(e: js.event.PopupMenuEvent): Unit = {}
      def popupMenuWillBecomeInvisible(e: js.event.PopupMenuEvent): Unit = {
        onHide()
        peer.removePopupMenuListener(this)
      }
      def popupMenuCanceled(e: js.event.PopupMenuEvent): Unit = {}
    }

    peer.addPopupMenuListener(listener)
    show(invoker, x, y)
  }
}
