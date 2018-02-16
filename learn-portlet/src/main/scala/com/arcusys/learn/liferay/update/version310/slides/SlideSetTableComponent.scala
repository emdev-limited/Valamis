package com.arcusys.learn.liferay.update.version310.slides

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait SlideSetTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.simple._

  case class SlideSet(id: Long,
                       title: String,
                       description: String,
                       courseId: Long,
                       logo: Option[String],
                       isTemplate: Boolean,
                       isSelectedContinuity: Boolean,
                       themeId: Option[Long],
                       duration: Option[Long],
                       scoreLimit: Option[Double],
                       playerTitle: String,
                       topDownNavigation: Boolean,
                       activityId: String,
                       status: String,
                       version: Double,
                       modifiedDate: DateTime,
                       oneAnswerAttempt: Boolean)

  class SlideSetTable(tag: Tag) extends Table[SlideSet](tag, tblName("SLIDE_SET")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def description = column[String]("DESCRIPTION", O.SqlType(varCharMax))

    def courseId = column[Long]("COURSE_ID")

    def logo = column[Option[String]]("LOGO")

    def isTemplate = column[Boolean]("IS_TEMPLATE")

    def isSelectedContinuity = column[Boolean]("IS_SELECTED_CONTINUITY")

    def themeId = column[Option[Long]]("THEME_ID")

    def duration = column[Option[Long]]("DURATION")

    def scoreLimit = column[Option[Double]]("SCORE_LIMIT")

    def playerTitle = column[String]("PLAYER_TITLE")

    def topDownNavigation = column[Boolean]("TOP_DOWN_NAVIGATION")

    def activityId = column[String]("ACTIVITY_ID")

    def status = column[String]("STATUS")

    def version = column[Double]("VERSION")

    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def oneAnswerAttempt = column[Boolean]("ONE_ANSWER_ATTEMPT")

    type Data = (Long,
      String,
      String,
      Long,
      Option[String],
      Boolean,
      Boolean,
      Option[Long],
      Option[Long],
      Option[Double],
      String,
      Boolean,
      String,
      String,
      Double,
      DateTime,
      Boolean)

    def constructSlideSet: Data => SlideSet = {
      case (
        id,
        title,
        description,
        courseId,
        logo,
        isTemplate,
        isSelectedContinuity,
        themeId,
        duration,
        scoreLimit,
        playerTitle,
        topDownNavigation,
        activityId,
        status,
        version,
        modifiedDate,
        oneAnswerAttempt) =>
        SlideSet(
          id = id,
          title = title,
          description = description,
          courseId = courseId,
          logo = logo,
          isTemplate = isTemplate,
          isSelectedContinuity = isSelectedContinuity,
          themeId = themeId,
          duration = duration,
          scoreLimit = scoreLimit,
          playerTitle = playerTitle,
          topDownNavigation = topDownNavigation,
          activityId = activityId,
          status = status,
          version = version,
          modifiedDate = modifiedDate,
          oneAnswerAttempt = oneAnswerAttempt)
    }

    def extractSlideSet: PartialFunction[SlideSet, Data] = {
      case SlideSet(
      id,
      title,
      description,
      courseId,
      logo,
      isTemplate,
      isSelectedContinuity,
      themeId,
      duration,
      scoreLimit,
      playerTitle,
      topDownNavigation,
      activityId,
      status,
      version,
      modifiedDate,
      oneAnswerAttempt) =>
        (id,
          title,
          description,
          courseId,
          logo,
          isTemplate,
          isSelectedContinuity,
          themeId,
          duration,
          scoreLimit,
          playerTitle,
          topDownNavigation,
          activityId,
          status,
          version,
          modifiedDate,
          oneAnswerAttempt)
    }

    def * = (
      id,
      title,
      description,
      courseId,
      logo,
      isTemplate,
      isSelectedContinuity,
      themeId,
      duration,
      scoreLimit,
      playerTitle,
      topDownNavigation,
      activityId,
      status,
      version,
      modifiedDate,
      oneAnswerAttempt) <>(constructSlideSet, extractSlideSet.lift)

  }

  val slideSets = TableQuery[SlideSetTable]
}
