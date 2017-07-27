package com.arcusys.learn.liferay.util

sealed abstract class PortletName(val name: String) {
  val key = name

  override def toString: String = {
    name
  }
}

object PortletName { //TODO: Enumeration?

  val namePrefix = "com_arcusys_valamis_web_portlet_"

  case object Gradebook extends PortletName(s"${namePrefix}GradebookView")

  case object LessonManager extends PortletName(s"${namePrefix}LessonManagerView")

  case object LessonViewer extends PortletName(s"${namePrefix}LessonViewerView")

  case object ContentManager extends PortletName(s"${namePrefix}ContentManagerView")

  case object LessonStudio extends PortletName(s"${namePrefix}LessonStudioView")

  case object LRSToActivityMapper extends PortletName(s"${namePrefix}LRSToActivityMapperView")

  case object LearningTranscript extends PortletName(s"${namePrefix}LearningTranscriptView")

  case object AdminView extends PortletName(s"${namePrefix}AdminView")

  case object ActivityToLRSMapper extends PortletName(s"${namePrefix}SocialActivitiesTinCanMapperView")

  case object AchievedCertificates extends PortletName(s"${namePrefix}AchievedCertificatesView")

  case object RecentLessons extends PortletName(s"${namePrefix}RecentLessonsView")

  case object ValamisActivities extends PortletName(s"${namePrefix}ValamisActivitiesView")

  case object LearningPaths extends PortletName(s"${namePrefix}LearningPathsView")

  case object MyLessons extends PortletName(s"${namePrefix}MyLessonsView")

  case object ValamisStudySummary extends PortletName(s"${namePrefix}ValamisStudySummaryView")

  case object MyCertificates extends PortletName(s"${namePrefix}MyCertificatesView")

  case object MyCourses extends PortletName(s"${namePrefix}MyCoursesView")

  case object AllCourses extends PortletName(s"${namePrefix}AllCoursesView")

  case object ContentProviderManager extends PortletName(s"${namePrefix}ContentProviderManager")

  case object TinCanStatementViewer extends PortletName(s"${namePrefix}TinCanStatementViewerView")

  case object ValamisReport extends PortletName(s"${namePrefix}ValamisReport")

  case object CertExpirationTracker extends PortletName(s"${namePrefix}CertificateExpirationTrackerView")

  case object LearningReport extends PortletName(s"${namePrefix}LearningReportView")
}

