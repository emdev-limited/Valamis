package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper

// convert package activities
class DBUpdater2717 extends LUpgradeProcess {

  override def getThreshold = 2717

  val newAssetClassName = "com.arcusys.valamis.lesson.model.Lesson"
  val oldAssetClassNames = Set(
    "com.arcusys.valamis.lesson.tincan.model.TincanPackage",
    "com.arcusys.valamis.lesson.scorm.model.ScormPackage"
  )

  override def doUpgrade(): Unit = {
    val newClassNameId = PortalUtilHelper.getClassNameId(newAssetClassName)

    oldAssetClassNames.toStream.foreach { className =>
      updateClassName(className, newClassNameId)
    }
  }


  private def updateClassName(className: String, newClassNameId: Long) = {
    val activities = SocialActivityLocalServiceHelper.getActivities(className, -1, -1)

    for (activity <- activities) {
      activity.setClassNameId(newClassNameId)
      SocialActivityLocalServiceHelper.updateSocialActivity(activity)
    }
  }
}
