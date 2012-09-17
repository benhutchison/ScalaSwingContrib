package scalaswingcontrib
package test

import scala.swing.{Label, Component, Button, Alignment, SimpleSwingApplication, MainFrame, BorderPanel}
import scala.swing.event.ButtonClicked
import BorderPanel.Position
import scalaswingcontrib.event.ColorChangeEvent
import java.awt.{Color,Font,Dimension}
import javax.swing.{border => jsb} 

/**
 * Demo for ColorChooser.
 * Based on http://download.oracle.com/javase/tutorial/uiswing/components/colorchooser.html
 * 
 * @author andy@hicks.net
 */
object ColorChooserDemo  extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "ColorChooser Demo"
    size = new Dimension(400, 400)
    
    contents = ui
  }

  def ui = new BorderPanel {
    val colorChooser = new ColorChooser {
      reactions += {
        case ColorChangeEvent(c: Color) =>
          banner.foreground = c
      }
    }

    colorChooser.border = new jsb.TitledBorder("Choose Text Color")
    
    val banner = new Label("Welcome to Scala Swing", null, Alignment.Center) {
      foreground = Color.yellow
      background = Color.blue
      opaque = true
      font = new Font("SansSerif", Font.BOLD, 24)
    }
   
    val bannerArea = new BorderPanel {
      layout(banner) = Position.Center
      border = new jsb.TitledBorder("Banner")
    }
    
    // Display a color selection dialog when button pressed 
    val selectColor = new Button("Choose Background Color") {
      reactions += {
        case ButtonClicked(_) =>
          ColorChooser.showDialog(this, "Test", Color.red) match {
            case Some(c) => banner.background = c
            case None =>
          }
      }
    }

    layout(bannerArea) = Position.North
    layout(colorChooser) = Position.Center
    layout(selectColor) = Position.South
  }
}
