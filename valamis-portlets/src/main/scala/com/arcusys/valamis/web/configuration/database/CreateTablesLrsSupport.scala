package com.arcusys.valamis.web.configuration.database

import java.sql.SQLException

import com.arcusys.slick.drivers.{DB2Driver, OracleDriver, SQLServerDriver}
import com.arcusys.valamis.lrssupport.tables.{LrsEndpointTableComponent, TokenTableComponent}
import slick.driver.HsqldbDriver
import slick.jdbc.JdbcBackend
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.driver.JdbcProfile


class CreateTablesLrsSupport(val jdbcProfile: JdbcProfile, db: JdbcBackend#DatabaseDef)
  extends LrsEndpointTableComponent
  with TokenTableComponent {


  import jdbcProfile.api._

  val tables = Seq(
    tokens, lrsEndpointTQ
  )

  def create() {
    if (!hasTables) {
      Await.result(db.run(createLrsEndpointSchema(db)), Duration.Inf)
      Await.result(db.run(createTokensSchema(db)), Duration.Inf)
    }
  }

  private def hasTables: Boolean = {
    tables.headOption.fold(true)(t => hasTable(t.baseTableRow.tableName))
  }

  private def hasTable(tableName: String): Boolean = {
    jdbcProfile match {
      case SQLServerDriver | OracleDriver =>
        try {
          Await.result(db.run(sql"""SELECT COUNT(*) FROM #$tableName WHERE 1 = 0""".as[Int]), Duration.Inf)
          true
        } catch {
          case e: SQLException => false
        }
      case DB2Driver =>
        Await.result(db.run(jdbcProfile.defaultTables), Duration.Inf).map(_.name.name).contains(tableName)
      case driver: HsqldbDriver =>
        val action = MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).headOption
        Await.result(db.run(action), Duration.Inf).isDefined
      case _ => Await.result(db.run(MTable.getTables(tableName).headOption), Duration.Inf).isDefined
    }
  }
}
