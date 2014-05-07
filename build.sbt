import SonatypeKeys._

name := "ScalaSwingContrib"

organization := "com.github.benhutchison"

version := "1.5"

scalaVersion := "2.11.0"

sonatypeSettings

profileName := "benhutchison"

libraryDependencies ++= {
  val sv = scalaVersion.value
  if (sv startsWith "2.10")
    Seq("org.scala-lang" % "scala-swing" % sv)
  else
    Seq(
      "org.scala-lang.modules" %% "scala-swing" % "1.0.1",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
    )
}

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.11" % "test",
  "junit" % "junit" % "4.7" % "test"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.4", "2.11.0")

// Following settings taken from: 
//https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md

publishMavenStyle := true


publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

//for local testing
//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
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
  
