package com.arcusys.valamis.slick.util

import java.sql.Connection

import slick.driver.JdbcDriver
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


trait SlickDbTestBase {

  //to isolate parallel tests we will use different databases
  lazy val dbName: String = getClass.getName.split('.').last

  private val properties = SlickTestProperties(dbName)

  lazy val driver: JdbcDriver = SlickHelper.getSlickDriver(properties.jdbcDriver)
  lazy val rootDB: JdbcBackend#DatabaseDef = getDB(properties.mainJdbcUrl)
  lazy val db: JdbcBackend#DatabaseDef = getDB(properties.testJdbcUrl)

  var connection: Option[Connection] = None

  protected def createDB(): Unit = {
    import driver.api._

    properties.initDbSql
      .map(q => rootDB.run(sqlu"#$q"))
      .foreach(await)

    if (properties.aliveConnection) {
      connection = Some(db.source.createConnection())
    }
  }

  protected def dropDB(): Unit = {
    import driver.api._

    connection.foreach(_.close)
    connection = None

    properties.dropDbSql
      .map(q => rootDB.run(sqlu"#$q"))
      .foreach(await)
  }

  private def getDB(jdbcUrl: String) = {
    driver.profile.backend.Database.forURL(
      jdbcUrl,
      properties.userName,
      properties.password,
      driver = properties.jdbcDriver
    )
  }

  protected def await[T](f: Future[T]) ={
    Await.result(f, Duration.Inf)
  }
}
