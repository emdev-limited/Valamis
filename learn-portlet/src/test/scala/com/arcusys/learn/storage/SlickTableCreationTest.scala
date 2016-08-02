package com.arcusys.learn.storage

import java.sql.Connection

import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.database.{CreateTables, CreateTablesNew}
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.H2Driver.simple._
import slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend


class SlickTableCreationTest extends FunSuite with BeforeAndAfter with SlickDBInfo {

  val db = Database.forURL("jdbc:h2:mem:slicktablecreation", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
  }
  after {
    connection.close()
  }

  override def slickDriver: JdbcDriver = H2Driver

  override def databaseDef: JdbcBackend#DatabaseDef = db

  override def slickProfile: JdbcProfile = slickDriver

  test("check table creation") {
    new CreateTables(this).create()
    new CreateTablesNew(this).create()
  }


}
