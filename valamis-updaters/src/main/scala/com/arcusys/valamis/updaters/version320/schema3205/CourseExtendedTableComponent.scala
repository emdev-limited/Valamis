package com.arcusys.valamis.updaters.version320.schema3205

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import org.joda.time.DateTime

/*
* One to one relation with LF course
* */
trait CourseExtendedTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  type CourseExtendedEntity = (Long, Option[String], Option[Int], Option[DateTime], Option[DateTime])

  class CourseExtendedTable(tag: Tag) extends Table[CourseExtendedEntity](tag, tblName("COURSE_EXTENDED")) {

    def courseId = column[Long]("COURSE_ID", O.PrimaryKey)

    def longDescription = column[Option[String]]("LONG_DESCRIPTION", O.SqlType(varCharMax))

    def userLimit = column[Option[Int]]("USER_LIMIT")

    def beginDate = column[Option[DateTime]]("BEGIN_DATE")

    def endDate = column[Option[DateTime]]("END_DATE")

    def * = (courseId, longDescription, userLimit, beginDate, endDate)

  }

  val coursesExtended = TableQuery[CourseExtendedTable]
}