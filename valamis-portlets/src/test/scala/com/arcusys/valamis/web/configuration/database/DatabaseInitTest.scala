package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

/**
  * Created by mminin on 15/09/16.
  */
class DatabaseInitTest
  extends FunSuite
    with BeforeAndAfter
    with SlickDbTestBase {

  def slickDbInfo: SlickDBInfo = new SlickDBInfo {
    override def databaseDef: JdbcBackend#DatabaseDef = DatabaseInitTest.this.db
    override def slickProfile: JdbcProfile = DatabaseInitTest.this.driver
    override def slickDriver: JdbcDriver = DatabaseInitTest.this.driver
  }

  before {
    createDB()
  }
  after {
    dropDB()
  }

  test("init without error") {
    new DatabaseInit(slickDbInfo).init()
  }

  test("init second time") {
    new DatabaseInit(slickDbInfo).init()

    new DatabaseInit(slickDbInfo).init()
  }

}
