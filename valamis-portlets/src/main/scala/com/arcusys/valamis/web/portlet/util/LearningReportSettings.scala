package com.arcusys.valamis.web.portlet.util

import javax.portlet.PortletPreferences

import com.arcusys.learn.liferay.LiferayClasses.LThemeDisplay
import com.arcusys.learn.liferay.constants.PortletKeysHelper
import com.arcusys.learn.liferay.services.PortletPreferencesLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.reports.model.{PathsReportStatus, PatternReportStatus}
import com.arcusys.valamis.web.servlet.report.response.learningReport.PathsGoalType

import scala.util.{Failure, Success, Try}

case class LearningReportLessonsSettings(courseId: Long,
                                  status: PatternReportStatus.Value,
                                  questionOnly: Boolean,
                                  lessonIds: Seq[Long],
                                  userIds: Seq[Long],
                                  hasSummary: Boolean
                                 )



case class LearningReportPathsSettings(courseId: Long,
                                       status: PathsReportStatus.Value,
                                       goalType: PathsGoalType.Value,
                                       certificateIds: Seq[Long],
                                       userIds: Seq[Long],
                                       hasSummary: Boolean
                                      )

class LearningReportSettingsService(val settingsId: Long,
                                    courseId: Long,
                                    preferences: PortletPreferences) extends Logging {

  private val statusKey = "Status"
  private val courseIdKey = "CourseId"
  private val questionOnlyKey = "QuestionOnly"
  private val lessonIdsKey = "LessonIds"
  private val userIdsKey = "UserIds"
  private val hasSummaryKey = "HasSummary"

  private val pathsStatusKey = "PathsStatus"
  private val pathsGoalsTypeKey = "PathsGoal"
  private val pathsCourseIdKey = "PathsCourseId"
  private val pathsCertificateIdsKey = "PathsCertificateIds"
  private val pathsUserIdsKey = "PathsUserIds"
  private val pathsHasSummaryKey = "PathsHasSummary"

  def this(themeDisplay: LThemeDisplay) = this(themeDisplay.getPlid, themeDisplay.getScopeGroupId, {
    val plId = themeDisplay.getPlid
    val portletId = PortletName.LearningReport.key

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    //when called with portletId we must create settings if not found
    PortletPreferencesLocalServiceHelper.getStrictPreferences(themeDisplay.getCompanyId, ownerId, ownerType, plId, portletId)
  })

  def this(settingsId: Long, companyId: Long, courseId: Long) = this(settingsId, courseId, {
    val plId = settingsId
    val portletId = PortletName.LearningReport.key

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    PortletPreferencesLocalServiceHelper.fetchPreferences(companyId, ownerId, ownerType, plId, portletId)
      .getOrElse {
        //when called with incorrect settingsId we must throw exception
        throw new EntityNotFoundException("learning portlet settings with id: " + settingsId)
      }
  })


  def getSettingsLessons: LearningReportLessonsSettings = {
    val reportCourseId = preferences.getParsedValue(courseIdKey, _.toLong).getOrElse(courseId)
    val status = preferences.getParsedValue(statusKey, PatternReportStatus.withName).getOrElse(PatternReportStatus.Empty)

    val lessonIds = preferences.getParsedValues(lessonIdsKey, _.toLong)
    val userIds = preferences.getParsedValues(userIdsKey, _.toLong)

    val questionOnly = preferences.getParsedValue(questionOnlyKey, _.toBoolean).getOrElse(false)
    val hasSummary = preferences.getParsedValue(hasSummaryKey, _.toBoolean).getOrElse(false)


    LearningReportLessonsSettings(
      reportCourseId,
      status,
      questionOnly,
      lessonIds,
      userIds,
      hasSummary
    )
  }

  def getSettingsPaths: LearningReportPathsSettings = {
    val reportCourseId = preferences.getParsedValue(pathsCourseIdKey, _.toLong).getOrElse(courseId)
    val status = preferences.getParsedValue(pathsStatusKey, PathsReportStatus.withName).getOrElse(PathsReportStatus.Empty)

    val certificateIds = preferences.getParsedValues(pathsCertificateIdsKey, _.toLong)
    val userIds = preferences.getParsedValues(pathsUserIdsKey, _.toLong)

    val goalType = preferences.getParsedValue(pathsGoalsTypeKey, PathsGoalType.withName).getOrElse(PathsGoalType.Empty)
    val hasSummary = preferences.getParsedValue(pathsHasSummaryKey, _.toBoolean).getOrElse(false)

    LearningReportPathsSettings(
      reportCourseId,
      status,
      goalType,
      certificateIds,
      userIds,
      hasSummary
    )
  }

  def set(settings: LearningReportLessonsSettings): Unit = {
    preferences.setValue(courseIdKey, settings.courseId.toString)
    preferences.setValue(statusKey, settings.status.toString)
    preferences.setValue(questionOnlyKey, settings.questionOnly.toString)
    preferences.setValue(hasSummaryKey, settings.hasSummary.toString)

    preferences.setValues(lessonIdsKey, settings.lessonIds.map(_.toString).toArray)
    preferences.setValues(userIdsKey, settings.userIds.map(_.toString).toArray)

    preferences.store()
  }

  def set(settings: LearningReportPathsSettings): Unit = {
    preferences.setValue(pathsCourseIdKey, settings.courseId.toString)
    preferences.setValue(pathsStatusKey, settings.status.toString)

    preferences.setValue(pathsGoalsTypeKey, settings.goalType.toString)

    preferences.setValue(pathsHasSummaryKey, settings.hasSummary.toString)

    preferences.setValues(pathsCertificateIdsKey, settings.certificateIds.map(_.toString).toArray)
    preferences.setValues(pathsUserIdsKey, settings.userIds.map(_.toString).toArray)

    preferences.store()
  }

  private implicit class PreferencesEx(val preferences: PortletPreferences) {
    def getParsedValue[T](key: String, parse: String => T): Option[T] = {
      Try {
        Option(preferences.getValue(key, null)).map(parse)
      } match {
        case Success(value) => value
        case Failure(exception) =>
          logger.error(s"Preference error for key: $key, error: ${exception.getMessage} ")
          None
      }
    }

    def getParsedValues[T](key: String, parse: String => T): Seq[T] = {
      Try {
        preferences.getValues(key, Array()).map(parse)
      } match {
        case Success(values) => values
        case Failure(exception) =>
          logger.error(s"Preference error for key: $key, error: ${exception.getMessage} ")
          Nil
      }
    }
  }

}
