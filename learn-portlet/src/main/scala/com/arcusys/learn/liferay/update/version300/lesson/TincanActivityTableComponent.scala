package com.arcusys.learn.liferay.update.version300.lesson

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait TincanActivityTableComponent {self:SlickProfile =>

  import driver.simple._

  case class TincanActivity(lessonId: Long,
                            activityId: String,
                            activityType: String,
                            name: String,
                            description: String,
                            launch: Option[String],
                            resource: Option[String],
                            id: Option[Long] = None)

  class TincanActivityTable(tag: Tag) extends Table[TincanActivity](tag, tblName("TINCAN_ACTIVITY")) {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)
    def lessonId = column[Long]("LESSON_ID")
    def activityId = column[String]("ACTIVITY_ID", O.Length(2000, true))
    def activityType = column[String]("ACTIVITY_TYPE", O.Length(2000, true))
    def name = column[String]("NAME", O.Length(2000, true))
    def description = column[String]("DESCRIPTION", O.Length(2000, true))
    def launch = column[Option[String]]("LAUNCH", O.Length(2000, true))
    def resource = column[Option[String]]("RESOURCE", O.Length(2000, true))

    def * = (lessonId, activityId, activityType, name, description, launch, resource, id.?) <> (TincanActivity.tupled, TincanActivity.unapply)
  }

  val tincanActivitiesTQ = TableQuery[TincanActivityTable]
}
