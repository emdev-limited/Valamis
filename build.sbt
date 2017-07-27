import sbt._
import Settings.ValamisPluginProperties
import com.arcusys.sbt.keys._

scalaVersion in ThisBuild := Version.scala

def lfService = Settings.liferay.version match {
  case Settings.Liferay620.version => lfService620
  case Settings.Liferay700.version => lfService700
}

enablePlugins(SonarRunnerPlugin)

sonarRunnerOptions := Seq("-e", "-X")

lazy val util = {
  (project in file("valamis-util"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-util")
    .settings(libraryDependencies ++= Dependencies.utils)
    .settings(libraryDependencies ++= Dependencies.osgi)
    .settings(OsgiKeys.bundleActivator := Some("com.arcusys.valamis.util.Activator"))
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.util.*"))
    .enablePlugins(BuildInfoPlugin)
    .settings(
      buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion, sbtVersion,
        "releaseName" -> Version.releaseName),
      buildInfoPackage := "com.arcusys.valamis.util"
    )
}

lazy val lfService620 = {
  (project in file("learn-liferay620-services"))
    .settings(Settings.common: _*)
    .settings(libraryDependencies ++= Dependencies.liferay620)
}

lazy val lfService700 = {
  (project in file("learn-liferay700-services"))
    .settings(Settings.common: _*)
    .settings(libraryDependencies ++= Dependencies.liferay700)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.bundleSymbolicName := "com.arcusys.learn.liferay700")
    .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.learn.liferay.*"))
}

lazy val slickSupport = {
  (project in file("valamis-slick-support"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slick-support")
    .settings(libraryDependencies ++= Dependencies.slick)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.importPackage := Seq(
      s"""com.arcusys.slick.drivers.*;version="[${Version.slickDrivers},3.1)"""",
      "*"))
    .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.persistence.common.*"))
    .dependsOn(slickSupportTest % Test)
}

lazy val slickSupportTest = { project
  .in(file("valamis-slick-test"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-slick-test")
  .settings(libraryDependencies ++= {
    Dependencies.slick :+
      Libraries.h2Driver
  })
}


lazy val questionbank = {
  (project in file("valamis-questionbank"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-questionbank")
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.content.*",
      "com.arcusys.valamis.questionbank.*"
    ))
    .dependsOn(slickSupport, util)
}

lazy val core = {
  (project in file("valamis-core"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-core")
    .settings(libraryDependencies ++= Seq(Libraries.valamisSettings, Libraries.lrsSupport)
      ++ Settings.liferay.dependencies)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.course.api",
      "com.arcusys.valamis.exception.*",
      "com.arcusys.valamis.export.*",
      "com.arcusys.valamis.file.*",
      "com.arcusys.valamis.liferay.*",
      "com.arcusys.valamis.log.*",
      "com.arcusys.valamis.member.*",
      "com.arcusys.valamis.model.*",
      "com.arcusys.valamis.ratings.*",
      "com.arcusys.valamis.settings.*",
      "com.arcusys.valamis.statements.*",
      "com.arcusys.valamis.tag.*",
      "com.arcusys.valamis.uri.*",
      "com.arcusys.valamis.user.*",
      "com.arcusys.valamis.utils.*"
    ))
    .dependsOn(lfService, util)
}

lazy val lesson = {
  (project in file("valamis-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lesson")
    .settings(libraryDependencies ++=  (Settings.liferay.dependencies ++
      Dependencies.slick ++ Seq(Libraries.lrsSupport)))
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.lesson.exception.*",
      "com.arcusys.valamis.lesson.model.*",
      "com.arcusys.valamis.lesson.service.*",
      "com.arcusys.valamis.lesson.storage.*"
    ))
    .dependsOn(core, slickSupport, slickSupportTest % Test)
}

lazy val scormLesson = {
  (project in file("valamis-scorm-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-scorm-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Seq(Libraries.scalaMock, Libraries.junit).map(_ % Test))
    .settings(libraryDependencies += Libraries.subcut)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.lesson.scorm.*"
    ))
    .dependsOn(core, util, lesson, slickSupport, lfService)
}

lazy val tincanLesson = {
  (project in file("valamis-tincan-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-tincan-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.lesson.tincan.*"
    ))
    .dependsOn(core, util, lesson, slickSupport)
}

lazy val course = {
  (project in file("valamis-course"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-course")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies :+ (Libraries.mockito % Test)))
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.course",
      "com.arcusys.valamis.course.model",
      "com.arcusys.valamis.course.service",
      "com.arcusys.valamis.course.storage",
      "com.arcusys.valamis.course.util",
      "com.arcusys.valamis.course.schema",
      "com.arcusys.valamis.course.exception"
    ))
    .dependsOn(core, certificate, lfService, queueSupport, slickSupport, slickSupportTest % Test)
}

lazy val gradebook = (project in file("valamis-gradebook"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-gradebook")
  .settings(libraryDependencies ++= Settings.liferay.dependencies)
  .enablePlugins(SbtOsgi)
  .settings(OsgiKeys.requireCapability :=
    "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
  .settings(OsgiKeys.exportPackage ++= Seq(
    "com.arcusys.valamis.gradebook.*"
  ))
  .dependsOn(core, scormLesson, lesson, lfService, slickSupport)

lazy val storyTree = {
  (project in file("valamis-storyTree"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-storyTree")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.storyTree.*"
    ))
    .dependsOn(core, lfService, lesson, tincanLesson, slickSupportTest % Test)
}

lazy val certificate = {
  (project in file("valamis-certificate"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-certificate")
    .settings(libraryDependencies ++=
      (Libraries.trainingEventsApi +: Settings.liferay.dependencies) ++
        Dependencies.json4s ++ Dependencies.slick)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.certificate.*"
    ))
    .dependsOn(core, lesson, gradebook, slickSupport, slickSupportTest % Test)
}

lazy val social = {
  (project in file("valamis-social"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-social")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.social.*"
    ))
    .dependsOn(lfService, gradebook, certificate, lesson)
}

lazy val lessonGenerator = {
  (project in file("valamis-lesson-generator"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lesson-generator")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies ++ Seq(Libraries.commonsLang, Libraries.poiOoxml)))
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.lesson.generator.*"
    ))
    .dependsOn(questionbank)
}

lazy val slide = {
  (project in file("valamis-slide"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slide")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.slide.*"
    ))
    .dependsOn(questionbank, lesson, tincanLesson, lessonGenerator, course)
}

lazy val slickPersistence = {
  (project in file("valamis-slick-persistence"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slick-persistence")
    .settings(libraryDependencies ++= Dependencies.slick)
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.exportPackage ++= Seq(
      "com.arcusys.valamis.persistence.impl.*"
    ))
    .dependsOn(core, slickSupport, storyTree, social, slide, lfService,
      certificate, course, slickSupportTest % Test)
}

lazy val hookUtils = (project in file("hook-utils"))
  .settings(Settings.common: _*)
  .settings(name := "hook-utils")
  .settings(libraryDependencies ++= Settings.Liferay620.dependencies)


lazy val hookLf620 = {
  (project in file("valamis-hook"))
    .settings(Settings.common: _*)
    .enablePlugins(DeployPlugin)
    .settings(warSettings ++ webappSettings : _*)
    .settings(artifactName in packageWar := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      "learn-liferay620-hook." + artifact.extension
    })
    .settings(libraryDependencies ++= Settings.Liferay620.dependencies)
    .settings(postProcess in webapp := { webappDir =>
      IO.delete(webappDir / Settings.liferayPluginPropertiesPath)

      ResourceActions.fillTemplateFile(
        webappDir,
        "../../../valamis-hook/src/main/resources/liferay-plugin-package.properties",
        ValamisPluginProperties,
        Settings.liferayPluginPropertiesPath)
    })
    .dependsOn(hookUtils)
}

lazy val hookTheme30Lf620 = {
  (project in file("valamis-hook-theme30-lf62"))
    .settings(Settings.common: _*)
    .settings(warSettings ++ webappSettings : _*)
    .settings(artifactName in packageWar := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      "learn-theme30-liferay620-hook." + artifact.extension
    })
    .settings(libraryDependencies ++= Settings.Liferay620.dependencies)
    .settings(postProcess in webapp := { webappDir =>
      IO.delete(webappDir / Settings.liferayPluginPropertiesPath)

      ResourceActions.fillTemplateFile(
        webappDir,
        "../../../valamis-hook-theme30-lf62/src/main/resources/liferay-plugin-package.properties",
        ValamisPluginProperties,
        Settings.liferayPluginPropertiesPath)
    })
    .dependsOn(hookUtils)
}

lazy val valamisPortlet = {
  (project in file("valamis-portlets"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-portlets")
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.bundleSymbolicName := "com.arcusys.valamis.web")
    .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.web.*"))
    //we need to add this import explicitly as classes from this package
    //are loaded dynamically using TCCL
    .settings(OsgiKeys.importPackage ++= Seq("org.apache.poi.sl.draw.binding"))
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Dependencies.scalatra)
    .settings(libraryDependencies ++= Dependencies.jackson)
    .settings(libraryDependencies ++= Dependencies.apacheXml)
    .settings(libraryDependencies  += Libraries.trainingEventsApi)
    .settings(libraryDependencies  += Libraries.lrsSupport)
    .settings(libraryDependencies ++= Seq(
      Libraries.prettyTime, //TODO try to remove dependency
      Libraries.commonsFileUpload,
      Libraries.poiOoxml, Libraries.poiScratchPad,
      Libraries.apachePDF
    ))
    .dependsOn(
      util, lfService, core,
      tincanLesson, scormLesson, lesson, lessonGenerator,
      gradebook, certificate, slide, storyTree, social,
      slickPersistence, reports,
      course, slickSupportTest % Test
    )
}

lazy val valamisBundle = (project in file("valamis-portlets-activator"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-portlets-activator")
    .enablePlugins(SbtOsgi, OsgiDependenciesPlugin, OsgiMainPlugin)
    .settings(OsgiCommonKeys.osgiHelper := ValamisOsgiHelper)
    .settings(CommonKeys.marketplaceFile := "../../src/main/liferay-marketplace.properties")
    .settings(OsgiKeys.bundleSymbolicName := "com.arcusys.valamis.web.bundle")
    .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.web.init.*", "com.arcusys.valamis.web.application.*"))
    .settings(OsgiKeys.bundleActivator := Some("com.arcusys.valamis.web.init.Activator"))
    .settings(OsgiKeys.importPackage := Seq("com.liferay.portal.kernel.messaging", "*"))
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"," +
      "liferay.extender;filter:=\"(&(liferay.extender=spring.extender)(version>=2.0)(!(version>=3.0)))\"")
    .settings(OsgiKeys.additionalHeaders ++= Map("-dsannotations" -> "*",
      "Service-Component" -> "VALAMIS-OSGI-INF/*", //using non default 'OSGI-INF' to avoid conflicts with -dsannotations
      "Liferay-Spring-Context" -> "META-INF/spring",
      "Liferay-Require-SchemaVersion" -> Version.schemaVersion
    ))
    .settings(OsgiKeys.requireBundle ++= Seq(
     "com.arcusys.learn.liferay700",
      "com.arcusys.valamis.web"
    ))
    .settings(resourceGenerators in Compile <+=
      (resourceManaged in Compile, name, version) map { (dir, n, v) =>

        ResourceActions.bundleActions(dir)

        Seq[File]()
      }
    )
    .settings(libraryDependencies ++= Dependencies.liferay700)
    .settings(libraryDependencies ++= Dependencies.osgi)
    .settings(libraryDependencies ++= Dependencies.valamisOsgiDependencies)
    .settings(libraryDependencies ++= Dependencies.scalatra)
    .settings(libraryDependencies  += Libraries.trainingEventsApi)
    .dependsOn(valamisPortlet, valamisUpdaters)


lazy val portlet = (project in file("learn-portlet"))
  .settings(Settings.common: _*)
  .enablePlugins(DeployPlugin)
  .settings(organization := "com.arcusys.learn")
  .settings(warSettings ++ webappSettings : _*)
  .settings(artifactName in packageWar := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
    module.name + "." + artifact.extension
  })
  .settings(postProcess in webapp := { webappDir =>

    ResourceActions.warActions(webappDir)
  })
  .settings(name := "learn-portlet")
  .settings(libraryDependencies ++= (
    Settings.liferay.dependencies
      ++ Dependencies.slick
      ++ Dependencies.json4s
      ++ Dependencies.scalatra
      ++ Dependencies.apacheXml // xml graphics for transcript, TODO remove
      ++ Dependencies.trainingEventService
      ++ Seq(
      Libraries.subcut,
      Libraries.httpClient,
      Libraries.slf4j,
      Libraries.slf4jSimple,
      Libraries.slf4jLclOver,
      Libraries.commonsLang,
      Libraries.commonsIO,
      Libraries.antiSamy,
      Libraries.apacheXmlFop
        exclude("org.apache.avalon.framework", "avalon-framework-api")
        exclude("org.apache.avalon.framework", "avalon-framework-impl"),

      Libraries.scalatraScalatest % Test
    ))
  )
  .dependsOn(
    valamisPortlet,
    lfService, core,
    tincanLesson, scormLesson, lesson, lessonGenerator,
    gradebook, certificate, slide, storyTree, social,
    slickPersistence, valamisUpdaters,
    course, slickSupportTest % Test
  )

lazy val reports = (project in file("valamis-reports"))
  .settings(name := "valamis-reports")
  .settings(Settings.common: _*)
  .enablePlugins(SbtOsgi)
  .settings(OsgiKeys.requireCapability :=
    "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
  .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.reports.*"))
  .settings(libraryDependencies ++= Settings.liferay.dependencies ++ Dependencies.slick)
  .dependsOn(lesson, tincanLesson, certificate, slickSupportTest % Test)

lazy val valamisUpdaters = (project in file("valamis-updaters"))
  .settings(name := "valamis-updaters")
  .settings(Settings.common: _*)
  .enablePlugins(SbtOsgi)
  .settings(OsgiKeys.requireCapability :=
    "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
  .settings(OsgiKeys.importPackage := Seq(
    s"""com.arcusys.slick.migration.*;version="[${Version.slickMigration},3.1)"""",
    "*"))
  .settings(OsgiKeys.exportPackage ++= Seq("com.arcusys.valamis.updaters.*"))
  .settings(libraryDependencies ++=
    Settings.liferay.dependencies ++
      Dependencies.slick ++
      Dependencies.osgi)
  .dependsOn(lfService, slickSupport, slickSupportTest % Test)

lazy val devHook = {
  (project in file("valamis-dev-hook"))
    .settings(Settings.common: _*)
    .settings(warSettings ++ webappSettings : _*)
    .settings(libraryDependencies ++= Settings.Liferay620.dependencies)
    .settings(postProcess in webapp := { webappDir =>
      IO.delete(webappDir / Settings.liferayPluginPropertiesPath)

      ResourceActions.fillTemplateFile(
        webappDir,
        "../../../valamis-dev-hook/src/main/resources/liferay-plugin-package.properties",
        ValamisPluginProperties,
        Settings.liferayPluginPropertiesPath)
    })
    .dependsOn(hookUtils)
}
lazy val emailNotification = {
  (project in file("valamis-hooks700"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-hooks700")
    .enablePlugins(SbtOsgi)
    .settings(OsgiKeys.requireCapability :=
      "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
    .settings(OsgiKeys.fragmentHost := Some("com.liferay.portal.settings.web;bundle-version=\"1.0.5\""))
}

lazy val queueSupport = (project in file("valamis-queue-support"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-queue-support")
  .enablePlugins(SbtOsgi)
  .settings(OsgiKeys.requireCapability :=
    "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"")
  .settings(OsgiKeys.exportPackage ++= Seq(
    "com.arcusys.valamis.queues.model",
    "com.arcusys.valamis.queues.schema",
    "com.arcusys.valamis.queues.service.*"))
  .settings(libraryDependencies ++=
      Dependencies.slick ++
      Dependencies.osgi)
  .dependsOn(slickSupport, slickSupportTest % Test)