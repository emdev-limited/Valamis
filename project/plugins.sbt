import sbt._

logLevel := Level.Warn

//to resolve Valamis SBT plugins
resolvers ++= Seq(
  Resolver.url("arcusys-public-releases",
    url("https://dev-1.arcusys.fi/mvn/repository/public/"))(Resolver.ivyStylePatterns),

  Resolver.url("sonatype-releases",
    url("https://oss.sonatype.org/content/repositories/releases/"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.8.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.arcusys.valamis" % "sbt-plugins" % "1.0.1")

addSbtPlugin("com.aol.sbt" % "sbt-sonarrunner-plugin" % "1.0.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

libraryDependencies ++= Seq(
  "biz.aQute.bnd" % "bndlib" % "2.4.0",
  "io.get-coursier" %% "coursier" % "1.0.0-M14",
  "io.get-coursier" %% "coursier-cache" % "1.0.0-M14"
)