import java.io.File

import scala.collection.mutable
import scala.io.Source
import scala.xml.pull._

case class ServletInfo(
    name: String,
    className: String,
    loadOnStartup: String,
    initParams: scala.collection.mutable.Map[String, String],
    pattern: String
)


object WebXmlSupport {

  val exceptions = Seq(
    "com.arcusys.learn.controllers.api.TranscriptPrintApiController"
  )

  def processWebXml(input: File, outPath: String): Unit = {
    val servlets = scala.collection.mutable.ListBuffer.empty[ServletInfo]
    val servletsMapping = scala.collection.mutable.Map.empty[String, String]

    val inputReader = new XMLEventReader(Source.fromFile(input))

    inputReader.foreach {
      case EvElemStart(_,label, _, _) =>
        label match {
          case "servlet" =>
            val itemIter = inputReader.takeWhile {
              case EvElemEnd(_, labelEnd) if labelEnd == "servlet" => false
              case _ => true;
            }
            servlets += buildServletInfo(itemIter)
          case "servlet-mapping" =>
            val (name, pattern) = readServletPattern(getItemIter(inputReader, "servlet-mapping"))
            if (!name.isEmpty && !pattern.isEmpty) {
              servletsMapping.put(name, pattern)
            }
          case _ => ()
        }
      case _ => ()
    }
    val res = servlets.collect {
      case info =>
      if (!info.name.isEmpty) {
        info.copy(pattern = servletsMapping.getOrElse(info.name, ""))
      } else {
        info
      }
    }
    generateServletXmls(res, outPath)
  }

  private def generateServletXmls(servlets: Seq[ServletInfo], outPath: String) = {
    servlets.foreach { info =>
      val servletClass = info.initParams.getOrElse("servlet-class", info.className)
      if (!exceptions.contains(servletClass)) {
        val xml =
          <component name={servletClass} immediate="true">
            <implementation class={info.className.replace("com.liferay.portal.kernel.servlet.PortalDelegateServlet",
            "com.arcusys.learn.liferay.servlet.CustomPortalDelegateServlet")}/>
            <service>
              <provide interface="javax.servlet.Servlet"/>
            </service>

            <property name="osgi.http.whiteboard.servlet.name" type="String" value={info.name}/>
            <property name="osgi.http.whiteboard.servlet.pattern" type="String" value={info.pattern}/>

            <property name="osgi.http.whiteboard.servlet.load-on-startup" type="String" value={info.loadOnStartup}/>

            <property name="servlet.init.servlet-class" type="String" value={info.initParams.getOrElse("servlet-class", "")}/>
            <property name="servlet.init.sub-context" type="String" value={info.initParams.getOrElse("sub-context", "")}/>


          </component>
        scala.xml.XML.save(s"$outPath/$servletClass.xml", xml, "UTF-8", xmlDecl = true)
      }
    }
  }


  private def getItemIter(iter: Iterator[XMLEvent], labelName: String) = {
    iter.takeWhile {
      case EvElemEnd(_, labelEnd) if labelEnd == labelName => false
      case _ => true;
    }
  }


  private def buildServletInfo(iter: Iterator[XMLEvent]): ServletInfo = {
    var info = ServletInfo("","","", mutable.Map.empty,"")
    iter.foreach {
      case EvElemStart(_, label, _,_) =>
        label match {
          case "servlet-name" =>
            info = info.copy(name = getValue(iter, "servlet-name"))
          case "servlet-class" =>
            info = info.copy(className = getValue(iter, "servlet-class"))
          case "load-on-startup" =>
            info = info.copy(loadOnStartup = getValue(iter, "load-on-startup"))
          case "init-param" =>
            val (name, value) = readInitParam(getItemIter(iter, "init-param"))
            info.initParams.put(name, value)
          case _ => ()
        }
      case _ => ()
    }
    info
  }

  def readServletPattern(iter: Iterator[XMLEvent]): (String, String) = {
    var name = ""
    var pattern = ""
    iter.foreach {
      case EvElemStart(_, label, _,_) =>
        label match {
          case "servlet-name" => name = getValue(iter, "servlet-name")
          case "url-pattern" => pattern = getValue(iter, "url-pattern")
          case _ => ()
        }
      case _ => ()
    }
    (name, pattern)
  }

  def readInitParam(iter: Iterator[XMLEvent]): (String, String) = {
    var name = ""
    var value = ""
    iter.foreach {
      case EvElemStart(_,label, _, _) =>
        label match {
          case "param-name" => name = getValue(iter, "param-name")
          case "param-value" => value = getValue(iter, "param-value")
          case _ => ()
        }
      case _ => ()
    }
    (name, value)
  }

  def getValue(iter: Iterator[XMLEvent], name: String): String = {
    var res: String = ""
    iter.foreach {
      case EvText(text) => res = text.trim
      case EvElemEnd(_, label) if label == name => return res
    }
    res
  }
}