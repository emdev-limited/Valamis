package com.arcusys.valamis.web.servlet.base

import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.web.configuration.database.DatabaseInit
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.ByteArrayEntity
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfter, FunSuiteLike}
import org.scalatra.test.HttpComponentsClientResponse
import org.scalatra.test.scalatest.ScalatraSuite
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

/**
  * Created by pkornilov on 2/3/17.
  */
trait ServletTestBase extends ScalatraSuite
  with FunSuiteLike
  with BeforeAndAfter
  with SlickDbTestBase
  with JsonMethods {

  implicit val formats = org.json4s.DefaultFormats

  def initDatabase(): Unit = {
    createDB()
    new DatabaseInit(slickDbInfo).init()
  }

  def slickDbInfo: SlickDBInfo = new SlickDBInfo {
    override def databaseDef: JdbcBackend#DatabaseDef = db

    override def slickProfile: JdbcProfile = driver

    override def slickDriver: JdbcDriver = driver
  }

  def getWithStatusAssert[T](url: String, expectedStatus: Int)(action: => T): T = {
    get(url) {
      statusAssert("GET", url, expectedStatus)
      action
    }
  }

  def statusAssert(method: String, url: String, expectedStatus: Int): Unit =
    assert(status == expectedStatus, s" for $method $url; response: $body")

  def deleteWithBody[A](path: String, body: Array[Byte])(f: => A): A = {
    val client = createClient
    val url = "%s/%s".format(baseUrl, path)

    val req = new HttpDeleteWithBody(url)

    req.setEntity(new ByteArrayEntity(body))

    req.setConfig(RequestConfig.custom().setCookieSpec("compatibility").build())

    withResponse(HttpComponentsClientResponse(client.execute(req))) { f }
  }
}
