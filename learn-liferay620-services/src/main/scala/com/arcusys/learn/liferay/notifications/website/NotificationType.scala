package com.arcusys.learn.liferay.notifications.website

object NotificationType extends Enumeration {
  type PortletType = Value

  //Should match portlet id
  val Activity = "ValamisActivities_WAR_learnportlet"
  val Gradebook = "Gradebook_WAR_learnportlet"
}
