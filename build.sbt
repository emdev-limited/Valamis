import sbt._
import Settings.ValamisPluginProperties

scalaVersion in ThisBuild := Version.scala

def lfService = Settings.liferay.version match {
  case Settings.Liferay620.version => lfService620
}

lazy val util = {
  (project in file("valamis-util"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-util")
    .settings(libraryDependencies ++= Dependencies.utils)
}

lazy val lfService620 = {
  (project in file("learn-liferay620-services"))
    .settings(Settings.common: _*)
    .settings(libraryDependencies ++= Dependencies.liferay620)
}

lazy val slickSupport = {
  (project in file("valamis-slick-support"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slick-support")
    .settings(libraryDependencies ++= Dependencies.slick)
    .dependsOn(slickSupportTest % Test)
}

lazy val slickSupportTest = {
  project
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
    .dependsOn(slickSupport, util)
}

lazy val core = {
  (project in file("valamis-core"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-core")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(lfService, util)
}

lazy val lrssupport = {
  (project in file("valamis-lrssupport"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lrssupport")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies ++ Dependencies.oauthClient ++ Dependencies.lrs))
    .dependsOn(lfService, util)
}

lazy val lesson = {
  (project in file("valamis-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies ++ Dependencies.slick)
    .dependsOn(core, lrssupport, slickSupport, slickSupportTest % Test)
}

lazy val scormLesson = {
  (project in file("valamis-scorm-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-scorm-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Seq(Libraries.scalaMock, Libraries.junit).map(_ % Test))
    .settings(libraryDependencies += Libraries.subcut)
    .dependsOn(core, util, lesson, slickSupport, lfService)
}

lazy val tincanLesson = {
  (project in file("valamis-tincan-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-tincan-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(core, util, lesson, slickSupport)
}

lazy val course = {
  (project in file("valamis-course"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-course")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies :+ (Libraries.mockito % Test)))
    .dependsOn(core, certificate, lfService, queueSupport, slickSupport, slickSupportTest % Test)
}

lazy val gradebook = (project in file("valamis-gradebook"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-gradebook")
  .settings(libraryDependencies ++= Settings.liferay.dependencies)
  .dependsOn(core, scormLesson, lesson, lfService, slickSupport)


lazy val certificate = {
  (project in file("valamis-certificate"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-certificate")
    .settings(libraryDependencies ++= Settings.liferay.dependencies ++
      Dependencies.json4s ++ Dependencies.slick)
    .dependsOn(core, lrssupport, lesson, gradebook, slickSupport, slickSupportTest % Test)
}

lazy val social = {
  (project in file("valamis-social"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-social")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(lfService, gradebook, certificate, lesson)
}

lazy val lessonGenerator = {
  (project in file("valamis-lesson-generator"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lesson-generator")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies ++ Seq(Libraries.commonsLang, Libraries.poiOoxml)))
    .dependsOn(questionbank)
}

lazy val slide = {
  (project in file("valamis-slide"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slide")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(questionbank, lesson, tincanLesson, lessonGenerator, course)
}

lazy val slickPersistence = {
  (project in file("valamis-slick-persistence"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slick-persistence")
    .settings(libraryDependencies ++= Dependencies.slick)
    .dependsOn(core, slickSupport, social, slide, lfService,
      certificate, course, slickSupportTest % Test)
}

lazy val hookUtils = (project in file("hook-utils"))
  .settings(Settings.common: _*)
  .settings(name := "hook-utils")
  .settings(libraryDependencies ++= Settings.Liferay620.dependencies)


lazy val hookTheme30Lf620 = {
  (project in file("valamis-hook-theme30-lf62"))
    .settings(Settings.common: _*)
    .settings(warSettings ++ webappSettings: _*)
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
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Dependencies.scalatra)
    .settings(libraryDependencies ++= Dependencies.jackson)
    .settings(libraryDependencies ++= Dependencies.apacheXml)
    .settings(libraryDependencies ++= Seq(
      Libraries.prettyTime, //TODO try to remove dependency
      Libraries.commonsFileUpload,
      Libraries.poiOoxml, Libraries.poiScratchPad,
      Libraries.apachePDF
    ))
    .dependsOn(
      util, lfService, lrssupport, core,
      tincanLesson, scormLesson, lesson, lessonGenerator,
      gradebook, certificate, slide, social,
      slickPersistence, reports,
      course, slickSupportTest % Test
    )
}

lazy val portlet = (project in file("learn-portlet"))
  .settings(Settings.common: _*)
  //.enablePlugins(DeployPlugin)
  .settings(organization := "com.arcusys.learn")
  .settings(warSettings ++ webappSettings: _*)
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
    lfService, lrssupport, core,
    tincanLesson, scormLesson, lesson, lessonGenerator,
    gradebook, certificate, slide, social,
    slickPersistence, valamisUpdaters,
    course, slickSupportTest % Test
  )

lazy val uiTest = (project in file("valamis-ui-tests"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-ui-test")
  .settings(parallelExecution in test := false)
  .settings(testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-hD", "valamis-ui-tests/target/report", "-o"))
  .settings(libraryDependencies ++= Dependencies.uiTests)



lazy val reports = (project in file("valamis-reports"))
  .settings(name := "valamis-reports")
  .settings(Settings.common: _*)
  .settings(libraryDependencies ++= Settings.liferay.dependencies ++ Dependencies.slick)
  .dependsOn(lesson, tincanLesson, certificate, slickSupportTest % Test)

lazy val valamisUpdaters = (project in file("valamis-updaters"))
  .settings(name := "valamis-updaters")
  .settings(Settings.common: _*)
  .settings(libraryDependencies ++=
    Settings.liferay.dependencies ++
      Dependencies.slick //++
    //Dependencies.osgi
  )
  .dependsOn(lfService, slickSupport, slickSupportTest % Test)

lazy val devHook = {
  (project in file("valamis-dev-hook"))
    .settings(Settings.common: _*)
    .settings(warSettings ++ webappSettings: _*)
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

lazy val queueSupport = (project in file("valamis-queue-support"))
  .settings(Settings.common: _*)
  .settings(name := "valamis-queue-support")
  .settings(libraryDependencies ++=
    Dependencies.slick)
  .dependsOn(slickSupport, slickSupportTest % Test) //