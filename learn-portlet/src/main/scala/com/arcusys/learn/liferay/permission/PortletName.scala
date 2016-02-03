package com.arcusys.learn.liferay.permission

/**
 * Created by asemenov on 09.02.15.
 */
sealed abstract class PortletName(val name: String) {
  val key = name + "_WAR_learnportlet"

  override def toString(): String = {
    name
  }
}

object PortletName { //TODO: Enumeration?

  case object GradeBook extends PortletName("Gradebook")

  case object CertificateManager extends PortletName("Curriculum")

  case object CertificateViewer extends PortletName("CurriculumUser")

  case object LessonManager extends PortletName("PackageManager")

  case object LessonViewer extends PortletName("SCORMApplication")

  case object ContentManager extends PortletName("ContentManager")

  case object LRSToActivityMapper extends PortletName("LRSToActivityMapper")

  case object LearningTranscript extends PortletName("LearningTranscript")

  case object UserPortfolio extends PortletName("SCORMUserAccount")

  case object AdminView extends PortletName("SCORMApplicationAdmin")

  case object ActivityToLRSMapper extends PortletName("SocialActivitiesTinCanMapper")

  case object LessonStudio extends PortletName("ValamisSlidesEditor")

  case object CompetencesUser extends PortletName("CompetencesUser")

  case object CompetencesAdmin extends PortletName("CompetencesAdmin")

  case object AchievedCertificates extends PortletName("AchievedCertificates")

  case object RecentLessons extends PortletName("RecentLessons")

  case object ValamisActivities extends PortletName("ValamisActivities")

  case object LearningPaths extends PortletName("LearningPaths")

  case object MyLessons extends PortletName("MyLessons")

  case object ValamisStudySummary extends PortletName("ValamisStudySummary")

  case object PhenomenizerStudio extends PortletName("PhenomenizerStudio")

  case object PhenomenizerReport extends PortletName("PhenomenizerReport")

  case object MyCertificates extends PortletName("MyCertificates")

  case object MyCourses extends PortletName("MyCourses")
}

