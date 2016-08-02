package com.arcusys.learn.liferay.update.version270.slide

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait SlideSetTableComponent extends TypeMapper {self: SlickProfile =>

  import driver.simple._

  case class SlideSet(id: Option[Long] = None,
                      title: String,
                      description: String,
                      courseId: Long,
                      logo: Option[String] = None,
                      isTemplate: Boolean = false,
                      isSelectedContinuity: Boolean = false,
                      themeId: Option[Long] = None,
                      duration: Option[Long] = None,
                      scoreLimit: Option[Double] = None,
                      topDownNavigation:Boolean = false,
                      activityId: String,
                      status: String,
                      version: Double,
                      modifiedDate: DateTime = new DateTime())

  class SlideSetTable(tag: Tag) extends Table[SlideSet](tag, tblName("SLIDE_SET")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION", O.DBType(varCharMax))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO")
    def isTemplate = column[Boolean]("IS_TEMPLATE")
    def isSelectedContinuity = column[Boolean]("IS_SELECTED_CONTINUITY")
    def themeId = column[Option[Long]]("THEME_ID")
    def duration = column[Option[Long]]("DURATION")
    def scoreLimit = column[Option[Double]]("SCORE_LIMIT")
    def topDownNavigation = column[Boolean]("TOP_DOWN_NAVIGATION")
    def activityId = column[String]("ACTIVITY_ID")
    def status = column[String]("STATUS")
    def version = column[Double]("VERSION")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (id.?, title, description, courseId, logo, isTemplate, isSelectedContinuity, themeId, duration, scoreLimit, topDownNavigation, activityId, status, version, modifiedDate) <>(SlideSet.tupled, SlideSet.unapply)
  }

  val slideSets = TableQuery[SlideSetTable]
}
