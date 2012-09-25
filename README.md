ScalaSwingContrib
=================

A collection of community-contributed extensions to Scala Swing, including

* A Tree and TreeModel for wrapping JTree, contributed by Ken Scambler
* A GroupPanel to display items using javax.swing.GroupLayout, contributed by Andreas Flierl
* A PopupMenu wrapper contributed by https://github.com/sullivan-
* A ColorChooser contributed by Andy Hicks
* RichColor enriches java.awt.Color with methods to move around in color space, contributed by Ben Hutchison
* AbsoluteLayoutPanel supports absolute layouts (Swing's null LayoutManager) in ScalaSwing, contributed by Ben Hutchison and Ken Scambler

All classes reside in the scalaswingcontrib package namespace.

Jars can be downloaded manually from the distributions directory.

ScalaSwingContrib is derived from [https://github.com/kenbot/scala-swing] and [https://github.com/ingoem/scala-swing/pulls], 
and is maintained by Ben Hutchison and Ken Scambler.

ScalaSwingContrib is built with SBT: sbt package

A Maven hosting application with Sonatype is in progress

* Group Id:  com.github.benhutchison
* Aftifact Id: ScalaSwingContrib
* Version: 1.0

Bug reports and pull requests welcome. 

Be aware that ScalaSwingContrib is intended to be strictly an /extension/, not a /modification/ to ScalaSwing, so changes to existing ScalaSwing APIs should be submitted to the core Scala team.