name := "ScalaSwingContrib"

organization := "com.github.benhutchison"

version := "1.8"

scalaVersion := "2.13.1"

sonatypeProfileName := "com.github.benhutchison"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing" % "2.1.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",

  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",

  "org.specs2" %% "specs2-core" % "4.8.3" % "test",
  "org.specs2" %% "specs2-junit" % "4.8.3" % "test",

  "junit" % "junit" % "4.7" % "test"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1")

unmanagedSourceDirectories in Compile += {
  val sourceDir = (sourceDirectory in Compile).value
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
    case _                       => sourceDir / "scala-2.13-"
  }
}

// Following settings taken from: 
//https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

//for local testing
//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra in Global := (
  <url>http://github.com/benhutchison/ScalaSwingContrib</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:benhutchison/ScalaSwingContrib.git</url>
    <connection>scm:git:git@github.com:benhutchison/ScalaSwingContrib.git</connection>
  </scm>
  <developers>
    <developer>
      <id>benhutchison</id>
      <name>Ben Hutchison</name>
      <url>http://github.com:benhutchison</url>
    </developer>
    <developer>
      <id>kenbot</id>
      <name>Ken Scambler</name>
      <url>http://github.com:kenbot</url>
    </developer>
  </developers>)
  
