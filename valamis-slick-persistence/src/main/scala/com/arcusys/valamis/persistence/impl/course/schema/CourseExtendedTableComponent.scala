package com.arcusys.valamis.persistence.impl.course.schema

import com.arcusys.valamis.course.model.CourseExtended
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._
import org.joda.time.DateTime

trait CourseExtendedTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  class CourseExtendedTable(tag: Tag) extends Table[CourseExtended](tag, tblName("COURSE_EXTENDED")) {

    def courseId = column[Long]("COURSE_ID", O.PrimaryKey)

    def longDescription = column[Option[String]]("LONG_DESCRIPTION", O.SqlType(varCharMax))

    def userLimit = column[Option[Int]]("USER_LIMIT")

    def beginDate = column[Option[DateTime]]("BEGIN_DATE")

    def endDate = column[Option[DateTime]]("END_DATE")

    def * = (courseId, longDescription, userLimit, beginDate, endDate) <> (CourseExtended.tupled, CourseExtended.unapply)

  }

  val coursesExtended = TableQuery[CourseExtendedTable]
}
