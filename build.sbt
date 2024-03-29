import scala.math.Ordering.Implicits._

name := "ScalaSwingContrib"

organization := "com.github.benhutchison"

version := "1.9"

scalaVersion := "3.1.2"

sonatypeProfileName := "com.github.benhutchison"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.1.0",

  "org.scala-lang.modules" %% "scala-collection-compat" % "2.7.0",

  "com.sun.activation" % "javax.activation" % "1.2.0", // clipboard data handlers - deprecated in SDK in Java 9, removed later

  "org.specs2" %% "specs2-core" % "4.15.0" % Test,
  "org.specs2" %% "specs2-junit" % "4.15.0" % Test,

  "junit" % "junit" % "4.7" % Test
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.12.15", "2.13.8", "3.1.2")

Compile / unmanagedSourceDirectories += {
  val sourceDir = (Compile / sourceDirectory).value
  if (CrossVersion.partialVersion(scalaVersion.value).exists(_ >= (2, 13))) {
    sourceDir / "scala-2.13+"
  } else {
    sourceDir / "scala-2.13-"
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

Test / publishArtifact := false

pomIncludeRepository := { _ => false }

Global / pomExtra := (
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
  
