import sbt._

def lfService = Settings.liferay.version match {
  case Settings.Liferay620.version => lfService620
}

lazy val util = {
  (project in file("valamis-util"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-util")
    .settings(libraryDependencies ++= Dependencies.json4sBase)
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
    .dependsOn(lfService)
}

lazy val questionbank = {
  (project in file("valamis-questionbank"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-questionbank")
    .settings(libraryDependencies += Libraries.subcut)
    .dependsOn(slickSupport, util)
}

lazy val core = {
  (project in file("valamis-core"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-core")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies += Libraries.subcut)
    .dependsOn(lfService, util)
}

lazy val lrssupport = {
  (project in file("valamis-lrssupport"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lrssupport")
    .settings(libraryDependencies ++= (Settings.liferay.dependencies ++ Dependencies.oauthClient ++ Dependencies.lrs))
    .settings(libraryDependencies += Libraries.subcut)
    .dependsOn(lfService, util)
}

lazy val lesson = {
  (project in file("valamis-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies ++ Dependencies.slick)
    .dependsOn(core, lrssupport, slickSupport)
}

lazy val scormLesson = {
  (project in file("valamis-scorm-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-scorm-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Seq(Libraries.scalaMock, Libraries.junit).map(_ % Test))
    .dependsOn(core, util, lesson, slickSupport, lfService)
}

lazy val tincanLesson = {
  (project in file("valamis-tincan-lesson"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-tincan-lesson")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(core, util, lesson, slickSupport)
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
    .settings(libraryDependencies ++= Settings.liferay.dependencies ++ Dependencies.json4s)
    .dependsOn(core, lrssupport, lesson, gradebook, slickSupport)
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
    .dependsOn(core, questionbank, lesson)
}

lazy val slide = {
  (project in file("valamis-slide"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slide")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .dependsOn(core, questionbank, lesson, tincanLesson, lessonGenerator, lrssupport)
}

lazy val slickPersistence = {
  (project in file("valamis-slick-persistence"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-slick-persistence")
    .settings(libraryDependencies ++= Dependencies.slick)
    .settings(parallelExecution in test := false)
    .dependsOn(core, slickSupport, social, slide, lfService)
}

lazy val hookUtils = (project in file("hook-utils"))
  .settings(Settings.common: _*)
  .settings(name := "hook-utils")
  .settings(libraryDependencies ++= Settings.liferay.dependencies)


lazy val hookLf620 = {
  (project in file("valamis-hook"))
    .settings(Settings.common: _*)
    .settings(warSettings ++ webappSettings : _*)
    .settings(artifactName in packageWar := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      "learn-liferay620-hook." + artifact.extension
    })
    .settings(libraryDependencies ++= Settings.Liferay620.dependencies)
    .settings(postProcess in webapp := { webappDir =>
      IO.delete(webappDir / Settings.liferayPluginPropertiesPath)

      val propertiesContent = Settings.getLiferayPluginProperties(
        webappDir / "../../../valamis-hook/src/main/resources/liferay-plugin-package.properties")

      IO.write(webappDir / Settings.liferayPluginPropertiesPath, propertiesContent)
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

      val propertiesContent = Settings.getLiferayPluginProperties(
        webappDir / "../../../valamis-hook-theme30-lf62/src/main/resources/liferay-plugin-package.properties")

      IO.write(webappDir / Settings.liferayPluginPropertiesPath, propertiesContent)
    })
    .dependsOn(hookUtils)
}


lazy val valamisPortlet = {
  (project in file("valamis-portlets"))
    .settings(Settings.common: _*)
    .settings(name := "valamis-portlets")
    .settings(libraryDependencies ++= Settings.liferay.dependencies)
    .settings(libraryDependencies ++= Dependencies.scalatra)
    .settings(libraryDependencies ++= Seq(
      Libraries.prettyTime, //todo: try to remove dependency
      Libraries.commonsFileUpload,
      Libraries.poiOoxml, Libraries.poiScratchPad,
      Libraries.apachePDF
    ))
    .dependsOn(
      util, questionbank, lfService, lrssupport, core,
      tincanLesson, scormLesson, lesson, lessonGenerator,
      gradebook, certificate, slide, social,
      slickPersistence
    )
}

lazy val portlet = (project in file("learn-portlet"))
  .settings(Settings.common: _*)
  .settings(organization := "com.arcusys.learn")
  .settings(warSettings ++ webappSettings : _*)
  .settings(artifactName in packageWar := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
    module.name + "." + artifact.extension
  })
  .settings(postProcess in webapp := { webappDir =>

    IO.copyDirectory(
      webappDir / "../../../learn-portlet/src/main/resources/ext-libs",
      webappDir / "WEB-INF/lib",
      overwrite = true)

    //todo: move it to prepare resource task
    val valamisJsAppPath = webappDir / "js2.0/helpers/Utils.js"

    IO.write(valamisJsAppPath, Settings.getLiferayPluginProperties(valamisJsAppPath))

    IO.write(
      file = webappDir / Settings.liferayPluginPropertiesPath,
      content = Settings.getLiferayPluginProperties(
        webappDir / "../../../learn-portlet/src/main/resources/liferay-plugin-package.properties"
      ),
      append = false
    )
  })
  .settings(name := "learn-portlet")
  .settings(libraryDependencies ++= (
    Settings.liferay.dependencies
      ++ Dependencies.slick
      ++ Dependencies.json4s
      ++ Dependencies.scalatra
      ++ Dependencies.apacheXml // xml graphics for transcript, todo: remove
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
    questionbank, lfService, lrssupport, core,
    tincanLesson, scormLesson, lesson, lessonGenerator,
    gradebook, certificate, slide, social,
    slickPersistence
  )
