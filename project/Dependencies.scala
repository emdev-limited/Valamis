import sbt._

object Version {
  //project's versions
  val valamis           = "3.4.1"
  val releaseName       = "Asteroid (3.4.1)"
  val schemaVersion     = "3312"  //!!!should be equals to release.info.build.number from portal-ext.properties!!!

  //versions of modules developed/forked by Arcusys
  val valamisSettings      = "3.4.0"
  val valamisLrsSupport    = "1.1.3"

  val trainingEvents    = "1.2.4"

  val slickMigration    = "3.0.7"
  val slickDrivers      = "3.0.3"

  val poi               = "3.16-beta1-arcusys-0.2.2"

  //third-party libraries versions
  val scala             = "2.11.8"
  val json4s            = "3.2.11"
  val sprayJson         = "1.3.2"
  val scalatest         = "2.2.3"
  val slf4j             = "1.6.4"
  val commonLang        = "2.6"
  val commonFileUpload  = "1.3.1"
  val commonIO          = "2.4"

  val jodaConvert       = "1.8.1"
  val jodaTime          = "2.9.7"
  val prettyTime        = "3.2.7.Final"
  val commonsValidator  = "1.4.1"
  val subcut            = "2.1"

  val portletApi        = "2.0"
  val servletApi        = "2.5"
  val javaxMail         = "1.4"
  val javaxInject       = "1"

  val liferay620           = "6.2.5"
  val liferay620Calendar   = "6.2.0.13"

  val liferay7             = "7.0.0"
  val liferay7Plugins      = "2.3.0"
  val liferay7Utils        = "2.0.1"
  val liferay7JournalApi   = "2.0.1"
  val liferay7PollsApi     = "2.0.0"
  val liferay7DdmApi       = "3.0.0"
  val liferay7BookmarksApi = "2.0.0"
  val liferayCalendarApi   = "2.0.1"
  val liferay7WikiApi      = "2.2.0"
  val liferay7MsgBoardApi  = "3.0.1"
  val liferay7Application  = "2.1.2"
  val lfPortalUpgrade      = "2.0.1"

  val junit             = "4.12"
  val specs             = "2.3.13"
  val scalaMock         = "3.2.2"
  val mockito           = "1.10.17"
  val guiceScala        = "4.0.0"
  val guice             = "4.0"
  val scalatra          = "2.3.1"
  val h2                = "1.4.194"
  val oauth             = "20100527"
  val oauthHttpClient   = "20090913"
  val httpClient        = "4.4"
  val antiSamy          = "1.5.1"
  val nimbusJose        = "3.2"
  val antiXml           = "0.5.2"

  val apachePDF         = "2.0.0-SNAPSHOT"
  val apacheXML         = "2.0"
  val apacheAvalon      = "4.3.1"

  val oshi              = "2.6-m-java7"//used in OsgiHelper only

  val slick             = "3.0.3"
  val hikari            = "2.3.7"

  //Additional OSGi dependencies
  val bcmail            = "1.46"
  val bctsp             = "1.46"
  val ooxmlSchemas      = "1.3"
  val crimson           = "1.1.3_2"
  val xmlResolver       = "1.2"
  val xmlSec            = "1.5.1"

  val jackson           = "1.9.13"
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
  val lfPortalService620 = "com.liferay.portal"   % "portal-service"           % Version.liferay620
  val lfPortalImpl620    = "com.liferay.portal"   % "portal-impl"              % Version.liferay620
  val lfUtilJava620      = "com.liferay.portal"   % "util-java"                % Version.liferay620
  val lfCalendar620      = "com.liferay.calendar" % "calendar-portlet-service" % Version.liferay620Calendar

  val lfPortalService7   = "com.liferay.portal" % "com.liferay.portal.kernel" % Version.liferay7Plugins
  val lfPortalImpl7      = "com.liferay.portal" % "com.liferay.portal.impl"   % Version.liferay7Plugins
  val lfUtilJava700      = "com.liferay.portal" % "com.liferay.util.java"     % Version.liferay7Utils
  val lfJournalApi       = "com.liferay"        % "com.liferay.journal.api"   % Version.liferay7JournalApi
  val lfPollsApi         = "com.liferay"        % "com.liferay.polls.api"     % Version.liferay7PollsApi
  val lfDdmApi           = "com.liferay"        % "com.liferay.dynamic.data.mapping.api" % Version.liferay7DdmApi
  val lfBookmarksApi     = "com.liferay"        % "com.liferay.bookmarks.api"  % Version.liferay7BookmarksApi
  val lfCalendar7Api     = "com.liferay"        % "com.liferay.calendar.api"   % Version.liferayCalendarApi
  val lfWikiApi7         = "com.liferay"        % "com.liferay.wiki.api"       % Version.liferay7WikiApi
  val lfMsgBoardApi7     = "com.liferay"        % "com.liferay.message.boards.api" % Version.liferay7MsgBoardApi
  val lfApplication7     = "com.liferay"        % "com.liferay.application.list.api" % Version.liferay7Application
  val lfPortalUpgrade    = "com.liferay"        % "com.liferay.portal.upgrade" % Version.lfPortalUpgrade

  val osgiAnnotation = "org.osgi" % "org.osgi.annotation" % "6.0.0"
  val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "5.0.0"
  val osgiCore = "org.osgi" % "org.osgi.core" % "5.0.0"
  val osgiWhiteboard = "org.osgi" % "org.osgi.service.http.whiteboard" % "1.0.0"


  // javax
  val portletApi = "javax.portlet" % "portlet-api" % Version.portletApi
  val servletApi = "javax.servlet" % "servlet-api" % Version.servletApi
  val jspApi     = "javax.servlet" % "jsp-api"     % Version.portletApi
  val mail       = "javax.mail"    % "mail"        % Version.javaxMail
  val javaxInject = "javax.inject" % "javax.inject" % Version.javaxInject

  // slick
  val slick           = "com.typesafe.slick" %% "slick"           % Version.slick
  // slick -> com.zaxxer » HikariCP-java6
  val hikari          = "com.zaxxer"         %  "HikariCP-java6"  % Version.hikari

  val slickDrivers    = "com.arcusys.slick"  %% "slick-drivers"   % Version.slickDrivers
  // slickDrivers -> resource
  val scalaARM        = "com.jsuereth"       %% "scala-arm"       % "1.4"

  val slickMigration  = "com.arcusys.slick"  %% "slick-migration" % Version.slickMigration

  val postgresDriver = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
  val h2Driver              = "com.h2database"  % "h2"                           % Version.h2

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

  //OAuth 1.0 Provider & Consumer Library
  val oauthCore       = "net.oauth.core" % "oauth"             % Version.oauth
  val oauthConsumer   = "net.oauth.core" % "oauth-consumer"    % Version.oauth
  val oauthHttpClient = "net.oauth.core" % "oauth-httpclient4" % Version.oauthHttpClient

  //apache xml graphics
  val apacheXmlFop     = "org.apache.xmlgraphics"      % "fop"                   % Version.apacheXML
  val apacheAvalonApi  = "org.apache.avalon.framework" % "avalon-framework-api"  % Version.apacheAvalon
  val apacheAvalonImpl = "org.apache.avalon.framework" % "avalon-framework-impl" % Version.apacheAvalon

  // other
  val poiOoxml   = "org.apache.poi"            % "poi-ooxml"       % Version.poi
  val poiScratchPad   = "org.apache.poi"       % "poi-scratchpad"  % Version.poi
  val httpClient = "org.apache.httpcomponents" % "httpclient"      % Version.httpClient
  val antiSamy   = "org.owasp.antisamy"        % "antisamy"        % Version.antiSamy
  val antiXml    = "no.arktekk"                %% "anti-xml"       % Version.antiXml
  val apachePDF  = "org.apache.pdfbox"         % "pdfbox"          % Version.apachePDF

  //Additional OSGi dependencies that are not needed in compile time, but has to be deployed to OSGi framework
  //without adding them here they are not collected by collectDependencies task
  val bcmail = "org.bouncycastle" % "bcmail-jdk15" % Version.bcmail
  val bctsp = "org.bouncycastle" % "bctsp-jdk15" % Version.bctsp
  val ooxmlSchemas = "org.apache.poi" % "ooxml-schemas" % Version.ooxmlSchemas
  val crimson = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.crimson" % Version.crimson
  val xmlResolver = "xml-resolver" % "xml-resolver" % Version.xmlResolver
  val xmlSec = "org.apache.santuario" % "xmlsec" % Version.xmlSec
  val avalonLogkit = "org.apache.avalon.logkit" % "avalon-logkit" % "2.2.1"
  val w3cCss =  "org.w3c.css" % "sac" % "1.3"
  val w3cSwg =   "org.axsl.org.w3c.dom.svg" % "svg-dom-java" % "1.1"
  val mozillaScript = "org.mozilla" % "rhino" % "1.7.6"
  val fontbox = "org.apache.pdfbox" % "fontbox" % "1.8.12"

  //json
  val jacksonCore      = "org.codehaus.jackson"           % "jackson-core-asl"    % Version.jackson
  val jacksonMapper    = "org.codehaus.jackson"           % "jackson-mapper-asl"  % Version.jackson

  val valamisSettings = "com.arcusys.valamis" %% "valamis-settings" % Version.valamisSettings
  val lrsSupport = "com.arcusys.valamis" %% "valamis-lrssupport" % Version.valamisLrsSupport

  val trainingEventsApi = "com.arcusys.valamis" %% "valamis-training-events-api" % Version.trainingEvents
  val trainingEventsService = "com.arcusys.valamis" %% "valamis-training-events-service" % Version.trainingEvents

}

object Dependencies {
  import Libraries._

  val common = Seq(
    jodaTime, jodaConvert,
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
    h2Driver % Test,
    slickMigration
  )

  val scalatra = Seq(
    scalatraBase,
    scalatraAuth,
    scalatraJson.exclude("org.json4s", "json4s-core_2.11"),
    scalatraScalatest % Test
  )

  val jackson = Seq(
    jacksonCore,
    jacksonMapper
  )

  val liferay620 = (lfCalendar620 +: javax) ++ Seq(lfUtilJava620, lfPortalService620, lfPortalImpl620).map( _ % Provided)
  val liferay700 = javax ++ Seq(lfPortalService7, lfCalendar7Api, lfPortalImpl7, lfUtilJava700, lfJournalApi,
    lfPollsApi, lfDdmApi, lfBookmarksApi, lfWikiApi7, lfMsgBoardApi7, lfApplication7, lfPortalUpgrade).map( _ % Provided)

  val trainingEventService = Seq(Libraries.trainingEventsApi, Libraries.trainingEventsService)

  val osgi = Seq(osgiAnnotation, osgiCompendium, osgiCore, osgiWhiteboard) map (_ % Provided)

  val valamisOsgiDependencies = Seq(bcmail, bctsp, ooxmlSchemas, crimson, xmlResolver, xmlSec, avalonLogkit,
    w3cCss, w3cSwg, mozillaScript)

  var utils = json4sBase ++ Seq(commonsIO)
}
