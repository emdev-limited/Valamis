package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.services.RatingsEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil

import scala.collection.JavaConverters.asScalaBufferConverter

// convert package ratings
class DBUpdater2718 extends LUpgradeProcess {

  override def getThreshold = 2718

  val newAssetClassName = "com.arcusys.valamis.lesson.model.Lesson"
  val oldAssetClassNames = Set(
    "com.arcusys.valamis.lesson.model.BaseManifest"
  )

  override def doUpgrade(): Unit = {
    val newClassNameId = PortalUtilHelper.getClassNameId(newAssetClassName)

    oldAssetClassNames.toStream
      .map {
        PortalUtilHelper.getClassNameId
      }
      .foreach {classNameId =>
        updateClassNameId(classNameId, newClassNameId)
      }
  }


  private def updateClassNameId(classNameId: Long, newClassNameId: Long) = {
    val query = RatingsEntryLocalServiceHelper.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("classNameId", classNameId))

    val ratings = RatingsEntryLocalServiceHelper.dynamicQuery(query)

    for (rating <- ratings) {
      rating.setClassNameId(newClassNameId)
      RatingsEntryLocalServiceHelper.updateRatingsEntry(rating)
    }
  }
}
