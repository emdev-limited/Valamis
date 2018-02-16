import sbt._
import sbt.Keys._

import com.arcusys.sbt.keys.CommonKeys
import com.arcusys.sbt.keys.OsgiCommonKeys
import com.arcusys.sbt.keys.DeployKeys

object Settings {

  val liferay = Liferay620

  val common = Seq(
    CommonKeys.lfVersion := liferay.version,
    DeployKeys.lf6Version := Liferay620.version,
    OsgiCommonKeys.lf7Version := Liferay700.version,
    organization := "com.arcusys.valamis",
    version := Version.valamis,
    scalaVersion := Version.scala,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      ArcusysResolvers.mavenCentral,
      ArcusysResolvers.public
    ),
    libraryDependencies ++= Dependencies.common,
    updateOptions := updateOptions.value.withLatestSnapshots(false),
    publishArtifact in packageDoc := false,
    publishArtifact in packageSrc := false,
    publishMavenStyle             := true,
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  )

  object Liferay620 {
    val dependencies = Dependencies.liferay620
    val supportVersion = "6.2.*"
    val version = Version.liferay620
  }

  object Liferay700 {
    val dependencies = Dependencies.liferay700
    val supportVersion = "7.0.0+"
    val version = Version.liferay7
  }

  object ValamisPluginProperties extends LiferayPluginProperties {
    val longDescription = "Valamis is a eLearning portlet family based on the SCORM and Tincan standards, including, package manager, player, Quiz editor, gradebook and curriculum portlets. This brings elements of Learning Management Systems (LMS) to the Liferay Portal Platform and expands them with more flexible options for eLearning."
    val pageUrl = "http://valamis.arcusys.com/"
    val tags = "valamis,eLearning,scorm,quiz"
    val author="Arcusys Oy."
    val supportedLiferayVersion = Settings.liferay.supportVersion
    val appVersion = Version.valamis
  }

  val liferayPluginPropertiesPath =  "WEB-INF/liferay-plugin-package.properties"

  def fillLiferayPluginProperties(source: File,
                                  pluginProperties: LiferayPluginProperties): String = {

    IO.read(source)
      .replace("${supported.liferay.versions}", pluginProperties.supportedLiferayVersion)
      .replace("${properties.longDescription}", pluginProperties.longDescription)
      .replace("${properties.pageUrl}", pluginProperties.pageUrl)
      .replace("${properties.tags}", pluginProperties.tags)
      .replace("${properties.author}", pluginProperties.author)
      .replace("${app.version}", pluginProperties.appVersion)
  }

  trait LiferayPluginProperties {
    def longDescription: String
    def pageUrl: String
    def tags: String
    def author: String
    def appVersion: String
    def supportedLiferayVersion: String
  }
}
