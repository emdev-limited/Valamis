import sbt._
import sbt.Keys._

object Settings {

  val common = Seq(
    organization := "com.arcusys.valamis",
    version := "2.6.1",
    scalaVersion := Version.scala,
    resolvers ++= Seq(
      ArcusysResolvers.public,
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeIvyRepo("releases"),
      DefaultMavenRepository
    ),
    libraryDependencies ++= Dependencies.common,
    publishArtifact in packageDoc := false,
    publishArtifact in packageSrc := false,
    publishMavenStyle             := true,
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  )

  val liferay = Liferay620

  object Liferay620 {
    val dependencies = Dependencies.liferay620
    val supportVersion = "6.2.3"
    val version = Version.liferay620
    val lfPersistenceFolder = "learn-persistence-liferay620"
  }
}
