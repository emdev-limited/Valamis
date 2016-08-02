package com.arcusys.valamis.web.configuration.database

import java.sql.SQLException

import com.arcusys.slick.drivers.{OracleDriver, SQLServerDriver}
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import slick.driver.HsqldbDriver
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class CreateTablesNew(dbInfo: SlickDBInfo)
  extends SlickProfile
    with ContentTableComponent {

  val dbTimeout = Duration.Inf
  val db = dbInfo.databaseDef
  val driver = dbInfo.slickProfile

  import driver.api._

  val tables = Seq(questionCategories, questions, plainTexts, answers)

  def create() {
    if (!hasTables) {
      Await.result(db.run(DBIO.sequence(tables.map(_.schema.create))), Duration.Inf)
    }
  }

  private def hasTables: Boolean = {
    tables.headOption.fold(true)(t => hasTable(t.baseTableRow.tableName))
  }

  private def hasTable(tableName: String): Boolean = {
    driver match {
      case SQLServerDriver | OracleDriver =>
        try {
          Await.result(db.run(sql"""SELECT COUNT(*) FROM #$tableName WHERE 1 = 0""".as[Int]), Duration.Inf)
          true
        } catch {
          case e: SQLException => false
        }
      case driver: HsqldbDriver =>
        val action = MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).headOption
        Await.result(db.run(action), Duration.Inf).isDefined
      case _ => Await.result(db.run(MTable.getTables(tableName).headOption), Duration.Inf).isDefined
    }
  }

}
