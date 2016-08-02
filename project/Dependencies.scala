import sbt._

object Version {
  val valamis           = "3.0.4"
  val scala             = "2.11.8"
  val json4s            = "3.2.11"
  val sprayJson         = "1.3.2"
  val scalatest         = "2.2.3"
  val slf4j             = "1.6.4"
  val commonLang        = "2.6"
  val commonFileUpload  = "1.3.1"
  val commonIO          = "2.4"

  val jodaConvert       = "1.7"
  val jodaTime          = "2.8.1"
  val prettyTime        = "3.2.7.Final"
  val commonsValidator  = "1.4.1"
  val subcut            = "2.1"
  val lrs               = "3.0.1"

  val portletApi        = "2.0"
  val servletApi        = "2.5"
  val javaxMail         = "1.4"
  val javaxInject       = "1"

  val liferay620           = "6.2.5"

  val junit             = "4.12"
  val specs             = "2.3.13"
  val scalaMock         = "3.2.2"
  val mockito           = "1.10.17"
  val guiceScala        = "4.0.0"
  val guice             = "4.0"
  val scalatra          = "2.3.1"
  val h2                = "1.3.170"
  val oauth             = "20100527"
  val oauthHttpClient   = "20090913"
  val httpClient        = "4.4"
  val poi               = "3.14-beta2-arcusys-0.2.0"
  val antiSamy          = "1.5.1"
  val nimbusJose        = "3.2"
  val antiXml           = "0.5.2"

  val apachePDF         = "2.0.0-SNAPSHOT"
  val apacheXML         = "1.1"
  val apacheAvalon      = "4.3.1"

  val slick             = "3.0.3"
  val hikari            = "2.3.7"
  val slickMigration    = "3.0.2"
  val slickDrivers      = "3.0.3"
}

object Libraries {
  // general
  val subcut            = "com.escalatesoft.subcut"    %% "subcut"             % Version.subcut
  val slf4j             = "org.slf4j"                  %  "slf4j-api"          % Version.slf4j
  val slf4jSimple       = "org.slf4j"                  %  "slf4j-simple"       % Version.slf4j
  val slf4jLclOver      = "org.slf4j"                  %  "jcl-over-slf4j"     % Version.slf4j
  val jodaTime          = "joda-time"                  %  "joda-time"          % Version.jodaTime
  val prettyTime        = "org.ocpsoft.prettytime"     %  "prettytime"          % Version.prettyTime
  val jodaConvert       = "org.joda"                   %  "joda-convert"       % Version.jodaConvert
  val commonsValidator  = "commons-validator"          %  "commons-validator"  % Version.commonsValidator
  val commonsLang       = "commons-lang"               %  "commons-lang"       % Version.commonLang
  val commonsFileUpload = "commons-fileupload"         %  "commons-fileupload" % Version.commonFileUpload
  val commonsIO         = "commons-io"                 %  "commons-io"         % Version.commonIO

  // scalatra
  val scalatraBase = "org.scalatra" %% "scalatra"      % Version.scalatra
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % Version.scalatra
  val scalatraJson = "org.scalatra" %% "scalatra-json" % Version.scalatra

  // json4s
  val json4sJakson = "org.json4s" %% "json4s-jackson" % Version.json4s
  val json4sCore   = "org.json4s" %% "json4s-core"    % Version.json4s
  val json4sAst    = "org.json4s" %% "json4s-ast"     % Version.json4s
  val json4sExt    = "org.json4s" %% "json4s-ext"     % Version.json4s

  // liferay
  val lfPortalService620 = "com.liferay.portal" % "portal-service" % Version.liferay620
  val lfPortalImpl620    = "com.liferay.portal" % "portal-impl"    % Version.liferay620
  val lfUtilJava620      = "com.liferay.portal" % "util-java"      % Version.liferay620

  // javax
  val portletApi = "javax.portlet" % "portlet-api" % Version.portletApi
  val servletApi = "javax.servlet" % "servlet-api" % Version.servletApi
  val jspApi     = "javax.servlet" % "jsp-api"     % Version.portletApi
  val mail       = "javax.mail"    % "mail"        % Version.javaxMail
  val javaxInject = "javax.inject" % "javax.inject" % Version.javaxInject

  // valamis core / LRS
  val lrsApi = "com.arcusys.valamis" %% "valamis-lrs-api" % Version.lrs

  // slick
  val slick           = "com.typesafe.slick" %% "slick"           % Version.slick
  // slick -> com.zaxxer » HikariCP-java6
  val hikari          = "com.zaxxer"         %  "HikariCP-java6"  % Version.hikari

  val slickDrivers    = "com.arcusys.slick"  %% "slick-drivers"   % Version.slickDrivers
  // slickDrivers -> resource
  val scalaARM        = "com.jsuereth"       %% "scala-arm"       % "1.4"

  val slickMigration  = "com.arcusys.slick"  %% "slick-migration" % Version.slickMigration

  // guice
  val guiceScala        = "net.codingwell"               %% "scala-guice"        % Version.guiceScala
  val guiceMultibinding = "com.google.inject.extensions" % "guice-multibindings" % Version.guice
  val guiceServlet      = "com.google.inject.extensions" % "guice-servlet"       % Version.guice

  // test
  val specs             = "org.specs2"      %% "specs2"                      % Version.specs
  val scalatest         = "org.scalatest"   %% "scalatest"                   % Version.scalatest
  val scalaMock         = "org.scalamock"   %% "scalamock-scalatest-support" % Version.scalaMock
  val scalatraScalatest = "org.scalatra"    %% "scalatra-scalatest"          % Version.scalatra
  val mockito           = "org.mockito"     % "mockito-all"                  % Version.mockito
  val portletTester     = "com.portletguru" % "portlettester"                % "0.1"
  val junit             = "junit"           % "junit"                        % Version.junit
  val h2DB              = "com.h2database"  % "h2"                           % Version.h2

  //OAuth 1.0 Provider & Consumer Library
  val oauthCore       = "net.oauth.core" % "oauth"             % Version.oauth
  val oauthConsumer   = "net.oauth.core" % "oauth-consumer"    % Version.oauth
  val oauthHttpClient = "net.oauth.core" % "oauth-httpclient4" % Version.oauthHttpClient

  //apache xml graphics
  val apacheXmlFop     = "org.apache.xmlgraphics"      % "fop"                   % Version.apacheXML
  val apacheAvalonApi  = "org.apache.avalon.framework" % "avalon-framework-api"  % Version.apacheAvalon
  val apacheAvalonImpl = "org.apache.avalon.framework" % "avalon-framework-impl" % Version.apacheAvalon

  //selenium
  val selenium = "org.seleniumhq.selenium" % "selenium-java" % "2.53.0"
  val seleniumFF = "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.53.0"

  // other
  val poiOoxml   = "org.apache.poi"            % "poi-ooxml"       % Version.poi
  val poiScratchPad   = "org.apache.poi"       % "poi-scratchpad"  % Version.poi
  val httpClient = "org.apache.httpcomponents" % "httpclient"      % Version.httpClient
  val antiSamy   = "org.owasp.antisamy"        % "antisamy"        % Version.antiSamy
  val antiXml    = "no.arktekk"                %% "anti-xml"       % Version.antiXml
  val apachePDF  = "org.apache.pdfbox"         % "pdfbox"          % Version.apachePDF
}

object Dependencies {
  import Libraries._

  val common = Seq(
    jodaTime,
    scalatest % Test,
    specs     % Test,
    mockito   % Test,
    scalaMock % Test,
    junit     % Test
  )

  val apacheXml = Seq(
    Libraries.apacheXmlFop
      exclude("org.apache.avalon.framework", "avalon-framework-api")
      exclude("org.apache.avalon.framework", "avalon-framework-impl"),
    Libraries.apacheAvalonApi,
    Libraries.apacheAvalonImpl
  )

  val guice = Seq(
    guiceScala
  )

  val json4sBase = Seq(
    json4sJakson,
    javaxInject // required in runtime, json4s-jackson -> json4s-core -> paranamer-2.6.jar -> javax.inject
  )

  val json4s = json4sBase ++ Seq(
    json4sCore,
    json4sAst,
    json4sExt
  )

  val javax = Seq(portletApi, servletApi, jspApi, mail).map( _ % Provided)

  val oauthClient = Seq(
    oauthCore,
    oauthConsumer,
    oauthHttpClient
      exclude("net.oauth.core", "oauth-consumer"),
    httpClient
  )

  val slick = Seq(
    Libraries.slick, hikari,
    slickDrivers, scalaARM,
    h2DB % Test,
    slickMigration
  )

  val lrs = Seq(
    lrsApi
      exclude("commons-fileupload", "commons-fileupload")
      exclude("commons-lang", "commons-lang")
      exclude("org.json4s", "json4s-ext_2.11"),
    commonsValidator,
    commonsLang
  )

  val scalatra = Seq(
    scalatraBase,
    scalatraAuth,
    scalatraJson
      exclude("org.json4s", "json4s-core_2.11")
  )

  val liferay620 = javax ++ Seq(lfUtilJava620, lfPortalService620, lfPortalImpl620).map( _ % Provided)

  val uiTests = Seq(
    selenium   % Test,
    seleniumFF % Test
  )
}
