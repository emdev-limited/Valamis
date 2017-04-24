package com.arcusys.valamis.web.portlet.util

import javax.portlet.{PortletPreferences, PortletRequest}

import com.arcusys.learn.liferay.LiferayClasses.LThemeDisplay
import com.arcusys.learn.liferay.constants.PortletKeysHelper
import com.arcusys.learn.liferay.services.{PortletPreferencesLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.portlet.base.LiferayHelpers
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

object ReportTypes extends Enumeration {
  val Certificate = Value("reportTypeCertificates")
  val Lesson = Value("reportTypeTopLessons")
  val User = Value("reportTypeMostActiveUsers")
}

object ReportPeriodType extends Enumeration {
  val LastWeek = Value("periodLastWeek")
  val LastMonth = Value("periodLastMonth")
  val LastYear = Value("periodLastYear")
  val Dates = Value("periodCustom")
}

object ReportsScopes extends Enumeration {
  val InstantScope = Value("instantScope")
  val CurrentCourse = Value("currentCourse")
}

class ReportPortletPreferences(preferences: PortletPreferences, companyId: Long) extends Logging
  with Injectable {

  implicit lazy val bindingModule = Configuration

  lazy val userService = inject[UserService]

  import ReportPortletPreferences._

  def store(): Unit = {
    preferences.store()
  }

  def getReportType: ReportTypes.Value = {
    val raw = preferences.getValue(ReportTypeKey, "")

    Option(raw).filter(_.nonEmpty)
      .map(ReportTypes.withName)
      .getOrElse(ReportTypes.Lesson)
  }

  def setReportType(reportType: ReportTypes.Value): Unit = {
    preferences.setValue(ReportTypeKey, reportType.toString)
  }

  def getReportsScope: ReportsScopes.Value = {
    val raw = preferences.getValue(ReportsScopeKey, "")

    Option(raw).filter(_.nonEmpty)
      .map(ReportsScopes.withName)
      .getOrElse(ReportsScopes.CurrentCourse)
  }

  def setReportsScope(reportScope: ReportsScopes.Value): Unit = {
    preferences.setValue(ReportsScopeKey, reportScope.toString)
  }

  def setPeriodType(periodType: ReportPeriodType.Value): Unit = {
    preferences.setValue(PeriodTypeKey, periodType.toString)
  }

  def setPeriodDates(startDate: String, endDate: String): Unit = {
    preferences.setValue(PeriodStartKey, startDate)
    preferences.setValue(PeriodEndKey, endDate)
  }

  def getPeriodType: ReportPeriodType.Value = {
    val raw = preferences.getValue(PeriodTypeKey, "")

    Option(raw).filter(_.nonEmpty)
      .map(ReportPeriodType.withName)
      .getOrElse(ReportPeriodType.LastWeek)
  }

  def getPeriodStart: String = {
    preferences.getValue(PeriodStartKey, "")
  }
  def getPeriodEnd: String = {
    preferences.getValue(PeriodEndKey, "")
  }

  def setUserIds(userIds: Seq[Long]): Unit = {
    preferences.setValue(UserIdsKey, userIds.mkString(","))
  }

  def getUserIds: Seq[Long] = {
    try {
      val userIds = preferences.getValue(UserIdsKey, "")
        .split(",")
        .filterNot(_.isEmpty)
        .map(_.toLong)
      userService.getByIds(companyId, userIds.toSet)
        .filter(_.isActive == true)
        .map(_.getUserId)
    }
    catch {
      case e: Exception =>
        logger.info("Couldn't retrieve userIds settings for ReportPreferences", e)
        Seq()
    }
  }

}

object ReportPortletPreferences {

  val ReportTypeKey = "reportType"
  val UserIdsKey = "userIds"
  val PeriodTypeKey = "periodType"
  val ReportsScopeKey = "reportsScope"

  val PeriodStartKey = "periodStart"
  val PeriodEndKey = "periodEnd"
  val PeriodDateTimeFormat = "YYYY/MM/DD"

  def apply(request: PortletRequest): ReportPortletPreferences = {
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    apply(themeDisplay)
  }

  def apply(themeDisplay: LThemeDisplay): ReportPortletPreferences = {
    val plId = themeDisplay.getPlid
    val companyId = themeDisplay.getCompanyId

    ReportPortletPreferences(plId, companyId)
  }

  def apply(plId: Long, companyId: Long): ReportPortletPreferences = {
    val portletId = PortletName.ValamisReport.key

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    val preferences = PortletPreferencesLocalServiceHelper.getStrictPreferences(companyId, ownerId, ownerType, plId, portletId)

    new ReportPortletPreferences(preferences, companyId)
  }
}
