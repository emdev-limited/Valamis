package com.arcusys.valamis.social.service

import javax.portlet.PortletPreferences

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.model.Activity
import com.arcusys.valamis.model.SkipTake

object ValamisActivitiesSettings {
  
  val COUNT_PROPERTY_NAME = "activitiesCount"
  val COUNT_DEFAULT_VALUE = "30"
  
  val visibleSettings = Map(
    "showBlogs" -> classOf[LBlogsEntry].getCanonicalName,
    "showBookmarks" -> classOf[LBookmarksEntry].getCanonicalName,
    "showEvents" -> "com.liferay.calendar.model.CalendarBooking",
    "showDocuments" -> classOf[LDLFileEntry].getCanonicalName,
    "showWebContents" -> classOf[LJournalArticle].getCanonicalName
  )

  def getVisibleLiferayActivities(prefs: Option[PortletPreferences]): Set[String] = {
    prefs.fold(Set[String]())(prefs =>
      ValamisActivitiesSettings.visibleSettings.collect { case (propName, className) if prefs.getValue(propName, "true") == "true" =>
        className
      }.toSet)
  }

}

trait ActivityService {
  def create(companyId: Long, userId: Long, content: String): Activity

  def share(companyId: Long, userId: Long, packageId: Long, comment: Option[String]): Option[Activity]

  def getBy(companyId: Long, userId: Option[Long], skipTake: Option[SkipTake], showAll: Boolean, lActivitiesToBeShown: Option[Set[String]]): Seq[Activity]

  def getById(activityId: Long): Activity

  def delete(activityId: Long): Unit
}