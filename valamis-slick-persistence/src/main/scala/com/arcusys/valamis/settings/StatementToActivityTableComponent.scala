package com.arcusys.valamis.settings

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.settings.model.StatementToActivity

import scala.slick.driver.JdbcProfile

trait StatementToActivityTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  class StatementToActivityTable(tag: Tag) extends Table[StatementToActivity](tag, tblName("STATEMENT_TO_ACTIVITY")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def courseId = column[Long]("COURSE_ID")
    def title = column[String]("TITLE", O.Length(512, varying = true))
    def activityFilter = column[Option[String]]("ACTIVITY_FILTER", O.Length(1000, varying = true))
    def verbFilter = column[Option[String]]("VERB_FILTER", O.Length(1000, varying = true))

    def * = (courseId, title, activityFilter, verbFilter, id.?) <> (StatementToActivity.tupled, StatementToActivity.unapply)
  }
  val statementToActivity = TableQuery[StatementToActivityTable]
}
