import sbt._

logLevel := Level.Warn

addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.8.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

libraryDependencies += "biz.aQute.bnd" % "bndlib" % "2.4.0"

libraryDependencies ++= Seq(
  "io.get-coursier" %% "coursier" % "1.0.0-M14",
  "io.get-coursier" %% "coursier-cache" % "1.0.0-M14"
)