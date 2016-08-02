package com.arcusys.valamis.persistence.impl.settings

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.settings.model.ActivityToStatement

trait ActivityToStatementTableComponent extends TypeMapper { self:SlickProfile =>

  import driver.simple._

  class ActivityToStatementTable(tag: Tag) extends Table[ActivityToStatement](tag, tblName("ACTIVITY_TO_STATEMENT")) {
    def courseId = column[Long]("COURSE_ID")
    def activityClassId = column[Long]("ACTIVITY_CLASS_ID")
    def verb = column[String]("VERB", O.Length(50, varying = true))

    def * = (courseId, activityClassId, verb) <>(ActivityToStatement.tupled, ActivityToStatement.unapply)
    def pk = primaryKey("PK_ACTIVITY_TO_STATEMENT", (courseId, activityClassId, verb))
  }

  val activityToStatement = TableQuery[ActivityToStatementTable]
}
