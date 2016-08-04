import sbt._
import sbt.Keys._

object Settings {

  val common = Seq(
    organization := "com.arcusys.valamis",
    version := Version.valamis,
    scalaVersion := Version.scala,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      ArcusysResolvers.public
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
    val supportVersion = "6.2.*"
    val version = Version.liferay620
  }

  val liferayPluginProperties = LiferayPluginProperties

  object LiferayPluginProperties {
    val longDescription = "Valamis is a eLearning portlet family based on the SCORM and Tincan standards, including, package manager, player, Quiz editor, gradebook and curriculum portlets. This brings elements of Learning Management Systems (LMS) to the Liferay Portal Platform and expands them with more flexible options for eLearning."
    val pageUrl = "http://valamis.arcusys.com/"
    val tags = "valamis,eLearning,scorm,quiz"
    val author="Arcusys Oy."
    val valamisVersion = Version.valamis
  }

  val liferayPluginPropertiesPath =  "WEB-INF/liferay-plugin-package.properties"

  def getLiferayPluginProperties(source: File): String = {

    IO.read(source)
      .replace("${supported.liferay.versions}", Settings.liferay.supportVersion)
      .replace("${properties.longDescription}", Settings.liferayPluginProperties.longDescription)
      .replace("${properties.pageUrl}", Settings.liferayPluginProperties.pageUrl)
      .replace("${properties.tags}", Settings.liferayPluginProperties.tags)
      .replace("${properties.author}", Settings.liferayPluginProperties.author)
      .replace("${valamis.version}", Settings.liferayPluginProperties.valamisVersion)
  }
}
