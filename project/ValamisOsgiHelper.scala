import java.util.jar.Attributes

import aQute.lib.utf8properties.UTF8Properties
import sbt._

import coursier.{Dependency, Module, _}

import com.arcusys.sbt.utils.OsgiHelper

object ValamisOsgiHelper extends OsgiHelper {

  override val osgiBundlesZip = "ValamisBundles.zip"
  override val osgiDepsZip = "ValamisDependencies.zip"
  override val lpkgName = "ValamisOnly"

  val allInOneLpkgName = "Valamis"
  val allDir = "valamis-lrs-assignment"

  //structure of this map:
  //libFileName -> (Host Bundle-SymbolicName,Fragment Bundle-SymbolicName)
  override protected val fragments = Map(
    //fragments of poi-3.14-beta2-arcusys-0.2.0.jar(Apache POI)
    s"poi-ooxml-${Version.poi}.jar" ->("Apache POI", "Apache POI OOXML"),
    s"poi-ooxml-schemas-${Version.poi}.jar" ->("Apache POI", "Apache POI OOXML Schemas"),
    s"poi-scratchpad-${Version.poi}.jar" ->("Apache POI", "Apache POI Scratchpad"),
    "ooxml-schemas-1.3.jar" ->("Apache POI", "ooxml-schemas"),

    //fragments of scalatra_2.11-2.3.1.jar (scalatra)
    s"scalatra-common_2.11-${Version.scalatra}.jar" -> ("scalatra", "scalatra-common"),
    //scalatra-auth, scalatra-json have their own package names and don't have to be a fragment

    //fragments of json4s-core_2.11-3.2.11.jar (json4s-core)
    s"json4s-ast_2.11-${Version.json4s}.jar" -> ("json4s-core", "json4s-ast"),
    //json4s-ext has its own package name and don't have to be a fragment

    s"avalon-framework-impl-${Version.apacheAvalon}.jar" ->
      (s"avalon-framework-api-${Version.apacheAvalon}.jar","avalon-framework-impl")
  )

  //it's not used now, but I left it just in case
  override protected val customVersions: Map[String, (String, String)] = Map()

  override protected val customBundleNames = Map("svg-dom-java-1.1.jar" -> "W3C SVG DOM bindings for Java",
  "smil-boston-dom-java-2000-02-25.jar" -> "W3C SMIL DOM bindings for Java")

  override protected val customExport = Map(
    s"slick-migration_2.11-${Version.slickMigration}.jar" -> s"""com.arcusys.slick.migration.*;version="${Version.slickMigration}"""",
    s"slick-drivers_2.11-${Version.slickDrivers}.jar" -> s"""com.arcusys.slick.drivers.*;version="${Version.slickDrivers}""""
  )

  // Original exported version is hidden by Liferay Portal Remote CXF Common (2.0.5, Liferay 7.0 GA2)
  // without providing access to classes implementation.
  // Set other version to access classes by OSHI library.
  private val jsonVersion = "1.0.41"
  override protected val customOsgiExport = Map(
    "fontbox-2.0.0-SNAPSHOT.jar" -> "org.apache.fontbox.*;version=2.0.0",
    "javax.json-1.0.4.jar" -> (
      "javax.json;version=\"" + jsonVersion + "\"," +
        "javax.json.spi;version=\"" + jsonVersion + "\"," +
        "javax.json.stream;version=\"" + jsonVersion + "\",*"
      )
  )

  // Without import correction (change version, remove) the OSHI library (oshi-core-2.6-m-java7.jar)
  // can not found json classes if the javax.json deployed before OSHI.
  override protected val customOsgiImport = Map(
    "fontbox-2.0.0-SNAPSHOT.jar" -> "!org.apache.fontbox,*",
    "javax.json-1.0.4.jar" -> (
      "javax.json;version=\"" + jsonVersion + "\";resolution:=optional," +
        "javax.json.spi;version=\"" + jsonVersion + "\";resolution:=optional," +
        "javax.json.stream;version=\"" + jsonVersion + "\";resolution:=optional,*"
      )
  )

  //com.sun.management, com.sun.tools.javadoc are available through bootdelegation mechanism
  //and should NOT be imported explicitly
  //see also: http://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-osgi/
  override protected val customImport = Map(
    s"oshi-core-${Version.oshi}.jar" -> ("!com.sun.management,javax.json;version=\"" + jsonVersion + "\",*"),
    "xmlbeans-2.6.0.jar" -> "!com.sun.tools.javadoc,com.sun.javadoc;resolution:=optional,*",
    "avalon-logkit-2.2.1.jar" -> ("org.apache.log4j.lf5;resolution:=optional," +
      "org.apache.log4j.lf5.viewer;resolution:=optional,*"),
    s"fop-${Version.apacheXML}.jar" ->
      s"""org.apache.fontbox.*;version="[1.8.12,2.0)",
         |javax.media.jai;resolution:=optional,*""".stripMargin,
    s"poi-ooxml-${Version.poi}.jar" ->
      ("!junit.framework,!org.etsi.uri.x01903.v14,!org.junit.*," +
        "org.apache.xmlbeans.impl.schema," +
        "*"),
    s"slick-migration_2.11-${Version.slickMigration}.jar" ->
      s"""
         |slick.*;version="[${Version.slick},3.1)",
         |com.arcusys.slick.drivers.*;version="[${Version.slickDrivers},3.1)",*""".stripMargin,
    s"slick-drivers_2.11-${Version.slickDrivers}.jar" ->  s"""slick.*;version="[${Version.slick},3.1)",*"""
  )

  //TODO get rid of this method and hardcoded versions
  override protected def fixVersion(attributes: Option[Attributes])(implicit props: UTF8Properties): Unit = {
    val version = attributes.flatMap(a => Option(a.getValue("Implementation-Version")))
      .map(_.replace("-SNAPSHOT", ".SNAPSHOT"))
      .map(_.replace("3.16-beta1", "3.16"))
      .map(_.replace("1.7+r608262", "1.7"))
      .map(_.replace("1.8.0_20", "1.8.0.20"))
      .map(_.replace("2.6-m-java7", "2.6"))
      .map(_.replace("2.3.1-valamis", "2.3.1"))
      .map(_.replace("2000-02-25", "25022000"))

    for (v <- version) props.put("Bundle-Version", v)
  }

  override val manuallyAddedDeps = Set(Dependency(Module("org.apache.pdfbox", "fontbox"), "1.8.12"))

  //for some reason collectDependencies task produce some unnecessary(in OSGi runtime) dependencies
  //so here we manually exclude them
  override val exceptions: ModuleFilter = moduleFilter() - (//all modules except those listed below
      "ch.qos.logback" % "logback-classic" |
      "ch.qos.logback" % "logback-core" |

      "org.slf4j" % "slf4j-api" |
      "org.slf4j" % "slf4j-simple" |
      "org.slf4j" % "jcl-over-slf4j" |

      "xml-apis" % "xml-apis" |
      "xml-apis" % "xml-apis-ext" |

      "avalon-framework" % "avalon-framework-api" % "4.2.0" |
      "avalon-framework" % "avalon-framework-impl" % "4.2.0" |

      "stax" % "stax-api" |
      "xalan" % "xalan" |
      "org.owasp.antisamy" % "antisamy" |
      "net.sourceforge.nekohtml" % "nekohtml" |
      "xerces" % "xercesImpl"
    )

}