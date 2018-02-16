package com.arcusys.valamis.web.service.export

import java.io.{BufferedWriter, File, FileWriter}
import java.util

import com.liferay.portal.kernel.log.LogFactoryUtil
import org.codehaus.jackson.{JsonFactory, JsonNode, JsonParser, JsonToken}
import org.codehaus.jackson.map.MappingJsonFactory
import org.codehaus.jackson.node.ArrayNode

import scala.collection.JavaConverters._
import scala.collection.mutable.HashSet

object JsonToCSVConverter {
  private val log = LogFactoryUtil.getLog(this.getClass)

  def convert(in: File, out: File): Unit = {

    val inputFileName = in.getAbsolutePath
    val outputFileName = out.getAbsolutePath
    try {
      log.debug("Starting")
      val f = new MappingJsonFactory
      var jp = f.createJsonParser(new File(inputFileName))
      var current = jp.nextToken
      if (current ne JsonToken.START_ARRAY) {
        log.debug("Error: root should be array: quiting.")
        return
      }
      log.debug("json parsed")
      val file = new File(outputFileName)
      if (!file.exists) file.createNewFile
      val fw = new FileWriter(file.getAbsoluteFile)
      val bw = new BufferedWriter(fw)
      val columnSet = new HashSet[String]
      log.debug("reading statements")
      var count = 0

      //columns
      current = jp.nextToken
      while (current != JsonToken.END_ARRAY) {
        if (current eq JsonToken.START_OBJECT) {
          val node = jp.readValueAsTree
          getTree(node, columnSet, "")
          log.debug("reading statement - " + count)
          count += 1
        }
        else jp.skipChildren
        current = jp.nextToken
      }
      log.debug("Total statements = " + count)
      val columnsString = columnSet mkString(",")
      printOut(bw, columnsString)
      jp.close()

      //data
      count = 0
      jp = f.createJsonParser(new File(inputFileName))
      current = jp.nextToken
      if (current ne JsonToken.START_ARRAY) {
        log.debug("Error: root should be array: quiting.")
        return
      }
      current = jp.nextToken
      while (current != JsonToken.END_ARRAY) {
        if (current eq JsonToken.START_OBJECT) {
          val node = jp.readValueAsTree
          val dataLine = columnSet.toArray map {col =>
            val fieldName = col
            val crumbs = fieldName.split("\\.").toSeq
            var curNode = node
            for (crumb <- crumbs) {
              if (curNode != null) {
                if (curNode.getClass eq classOf[ArrayNode])
                  curNode = curNode.get(crumb.toInt)
                else
                  curNode = curNode.get(crumb)
              }
            }
            if (curNode == null) "" else curNode.toString
          } mkString(",")
          printOut(bw, dataLine)
          log.debug("reading statement - " + count)
          count += 1
        }
        else jp.skipChildren
        current = jp.nextToken
      }
      bw.close()
      jp.close()
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def getTree(node: JsonNode, columnSet: HashSet[String], prefix: String): Unit = {
    val filedIter = node.getFields
    while (filedIter.hasNext) {
      val elem = filedIter.next
      val valNode = elem.getValue
      if (valNode.isValueNode) {
        val key = prefix + putPoint(prefix) + elem.getKey
        if (!columnSet.contains(key) && !elem.getKey.startsWith("http://")) columnSet.add(key) //exclude keys started with http
      }
      else if (valNode.isArray) {
        val arNode = valNode.asInstanceOf[ArrayNode]
        (0 until arNode.size) foreach  {t =>
          val p = prefix + putPoint(prefix) + elem.getKey + "." + t
          getTree(valNode.get(t), columnSet, p)
        }
      }
      else {
        val newPrefix = prefix + putPoint(prefix) + elem.getKey
        getTree(valNode, columnSet, newPrefix)
      }
    }
  }

  private def putPoint(prefix: String): String = if (prefix.equalsIgnoreCase("")) "" else "."

  private def printOut(bw: BufferedWriter, line: String): Unit = {
    bw.write(line)
    bw.newLine()
  }
}
