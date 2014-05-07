ScalaSwingContrib
=================

A collection of community-contributed extensions to Scala Swing, including

* A Tree and TreeModel for wrapping JTree, contributed by Ken Scambler & Sciss
* A GroupPanel to display items using javax.swing.GroupLayout, contributed by Andreas Flierl
* A PopupMenu wrapper contributed by https://github.com/sullivan-
* A ColorChooser contributed by Andy Hicks
* RichColor enriches java.awt.Color with methods to move around in color space, contributed by Ben Hutchison
* AbsoluteLayoutPanel supports absolute layouts (Swing's null LayoutManager) in ScalaSwing, contributed by Ben Hutchison and Ken Scambler
* RichFont enriches java.awt.Font

All classes reside in the scalaswingcontrib package namespace.

Jars can be downloaded manually from the distributions directory.

ScalaSwingContrib is derived from [https://github.com/kenbot/scala-swing] and [https://github.com/ingoem/scala-swing/pulls], 
and is maintained by Ben Hutchison and Ken Scambler.

ScalaSwingContrib is built with SBT: sbt package

###Maven details

1.5 is Cross-built for 2.10.4 and 2.11.0:

"com.github.benhutchison" %% "ScalaSwingContrib" % "1.5"


Version 1.4 is built with Scala 2.10.1. 1.3 is built with Scala 2.9.2.

Bug reports and pull requests welcome. 

Be aware that ScalaSwingContrib is intended to be strictly an /extension/, not a /modification/ to ScalaSwing, so changes to existing ScalaSwing APIs should be submitted to the core Scala team.
