package com.arcusys.valamis.persistence.impl.settings

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait StatementToActivityTableComponent extends LongKeyTableComponent with  TypeMapper {self:SlickProfile =>
  import driver.simple._

  class StatementToActivityTable(tag: Tag) extends LongKeyTable[StatementToActivity](tag, "STATEMENT_TO_ACTIVITY") {
    def courseId = column[Long]("COURSE_ID")
    def title = column[String]("TITLE", O.Length(512, varying = true))
    def activityFilter = column[Option[String]]("ACTIVITY_FILTER", O.Length(1000, varying = true))
    def verbFilter = column[Option[String]]("VERB_FILTER", O.Length(1000, varying = true))

    def * = (courseId, title, activityFilter, verbFilter, id.?) <> (StatementToActivity.tupled, StatementToActivity.unapply)

    def update = (courseId, title, activityFilter, verbFilter) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }
  val statementToActivity = TableQuery[StatementToActivityTable]
}
