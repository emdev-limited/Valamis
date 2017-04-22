package com.arcusys.learn.storage

import java.sql.Connection

import com.arcusys.valamis.persistence.common.{DbNameUtils, SlickDBInfo}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.web.configuration.database.{CreateTables, CreateTablesNew}
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend


class SlickTableCreationTest extends FunSuite with BeforeAndAfter with SlickDBInfo with SlickDbTestBase {

  before {
    createDB()
  }
  after {
    dropDB()
  }

  override def slickDriver: JdbcDriver = driver

  override def databaseDef: JdbcBackend#DatabaseDef = db

  override def slickProfile: JdbcProfile = slickDriver

  private def checkIdentifiers[T <: JdbcProfile#Table[_]](table: T) = {
    val identifiers = table.create_*.map(_.name) ++ //columns
      table.indexes.map(_.name) ++
      table.foreignKeys.map(_.name) ++
      table.primaryKeys.map(_.name) ++
      Seq(table.tableName)
    identifiers.foreach(DbNameUtils.checkLengthAndReturn)
  }

  test("check table creation") {
    val slick2Tables = new CreateTables(this)
    slick2Tables.tables.foreach { table => checkIdentifiers(table.baseTableRow) }

    val slick3Tables = new CreateTablesNew(this)
    slick3Tables.tables.foreach { table => checkIdentifiers(table.baseTableRow) }

    slick2Tables.create()
    slick3Tables.create()
  }


}
