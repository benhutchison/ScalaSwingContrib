ScalaSwingContrib
=================

A collection of community-contributed extensions to Scala Swing, including

* A Tree and TreeModel for wrapping JTree, contributed by Ken Scambler & Sciss
* A GroupPanel to display items using javax.swing.GroupLayout, contributed by Andreas Flierl
* A PopupMenu wrapper contributed by <https://github.com/sullivan->
* A ColorChooser contributed by Andy Hicks
* RichColor enriches java.awt.Color with methods to move around in color space, contributed by Ben Hutchison
* AbsoluteLayoutPanel supports absolute layouts (Swing's null LayoutManager) in ScalaSwing, contributed by Ben Hutchison and Ken Scambler
* RichFont enriches `java.awt.Font`

All classes reside in the `scalaswingcontrib` package namespace.

ScalaSwingContrib is derived from <https://github.com/kenbot/scala-swing> and <https://github.com/ingoem/scala-swing/pulls>, 
and is maintained by Ben Hutchison and Ken Scambler.

### Changelog

- `1.4`: Not Recorded
- `1.5` April 2013: Fixes/enhancements to Tree (thanks @Sciss) 
- `1.6` Dec 2015: Fixes/enhancements to Tree (thanks @OndrejSpanel)   
- `1.7` Nov 2016: Scala 2.12, library upgrades, jdk8 only 

### Maven details

`1.7` is Cross-built for `2.10.6`, `2.11.8`, `2.12.0`:

    "com.github.benhutchison" %% "scalaswingcontrib" % "1.7"

Version `1.4` is built with Scala `2.10.1`. `1.3` is built with Scala `2.9.2`.

### Publishing Instructions

Ensure sonatype credentials at `$HOME/.sbt/(sbt-version)/sonatype.sbt` 
[More info](https://github.com/xerial/sbt-sonatype#homesbtsbt-versionsonatypesbt)

`+publishSigned`

`sonatypeRelease`

### Other

Bug reports and pull requests welcome. 

Be aware that ScalaSwingContrib is intended to be strictly an _extension_, not a _modification_ to ScalaSwing, so changes to existing ScalaSwing APIs should be submitted to the core Scala team.
