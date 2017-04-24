import sbt._
import sbt.Keys._

object Settings {

  def uiTestFilter(name: String): Boolean = !name.startsWith("com.arcusys.valamis.tests.ui") //true

  val liferay = Liferay620

  val common = Seq(
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
    publishMavenStyle := true,
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    testOptions ++= Seq(Tests.Filter(uiTestFilter))
  )

  object Liferay620 {
    val dependencies = Dependencies.liferay620
    val supportVersion = "6.2.5"
    val version = Version.liferay620
  }

  object ValamisPluginProperties extends LiferayPluginProperties {
    val longDescription = "Valamis is a eLearning portlet family based on the SCORM and Tincan standards, including, package manager, player, Quiz editor, gradebook and curriculum portlets. This brings elements of Learning Management Systems (LMS) to the Liferay Portal Platform and expands them with more flexible options for eLearning."
    val pageUrl = "http://valamis.arcusys.com/"
    val tags = "valamis,eLearning,scorm,quiz"
    val author = "Arcusys Oy."
    val supportedLiferayVersion = Settings.liferay.supportVersion
    val appVersion = Version.valamis
  }

  val liferayPluginPropertiesPath = "WEB-INF/liferay-plugin-package.properties"

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
