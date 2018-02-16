package com.arcusys.learn.liferay.update.version310.slides3101

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait SlideSetTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.simple._

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

    def lockUserId = column[Option[Long]]("LOCK_USER_ID")

    def lockDate = column[Option[DateTime]]("LOCK_DATE")

    def requiredReview = column[Boolean]("REQUIRED_REVIEW")

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
      Boolean,
      Option[Long],
      Option[DateTime],
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
        oneAnswerAttempt,
        lockUserId,
        lockDate,
        requiredReview) =>
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
          oneAnswerAttempt = oneAnswerAttempt,
          lockUserId = lockUserId,
          lockDate = lockDate,
          requiredReview = requiredReview)
    }

    def extractSlideSet: PartialFunction[SlideSet, Data] = {
      case SlideSet(
      id,
      title,
      description,
      courseId,
      logo,
      _,
      isTemplate,
      isSelectedContinuity,
      themeId,
      duration,
      scoreLimit,
      playerTitle,
      _,
      topDownNavigation,
      activityId,
      status,
      version,
      modifiedDate,
      oneAnswerAttempt,
      _,
      lockUserId,
      lockDate,
      requiredReview,
      _) =>
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
          oneAnswerAttempt,
          lockUserId,
          lockDate,
          requiredReview)
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
      oneAnswerAttempt,
      lockUserId,
      lockDate,
      requiredReview) <>(constructSlideSet, extractSlideSet.lift)

  }

  val slideSets = TableQuery[SlideSetTable]
}
