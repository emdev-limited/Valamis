package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.services.RatingsStatsLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil

import scala.collection.JavaConverters.asScalaBufferConverter

// convert package ratings statistics
class DBUpdater2722 extends LUpgradeProcess {

  override def getThreshold = 2722

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
      .foreach { classNameId =>
        updateClassNameId(classNameId, newClassNameId)
      }
  }


  private def updateClassNameId(classNameId: Long, newClassNameId: Long) = {
    val query = RatingsStatsLocalServiceHelper.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("classNameId", classNameId))

    val statistics = RatingsStatsLocalServiceHelper.dynamicQuery(query)

    for (stats <- statistics) {
      val newStats = RatingsStatsLocalServiceHelper.getStats(newAssetClassName, stats.getClassPK)
      newStats.setAverageScore(stats.getAverageScore)
      newStats.setTotalEntries(stats.getTotalEntries)
      newStats.setTotalScore(stats.getTotalScore)

      RatingsStatsLocalServiceHelper.updateRatingsStats(newStats)
      RatingsStatsLocalServiceHelper.deleteRatingsStats(stats.getStatsId)
    }
  }
}
