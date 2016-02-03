package com.arcusys.learn.liferay

/**
 * Created by aklimov on 05.02.15.
 */

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.services.{AssetVocabularyLocalServiceHelper, GroupLocalServiceHelper, UserLocalServiceHelper}
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portlet.asset.NoSuchVocabularyException

class CreateTagVocabulary extends SimpleAction with Injectable {
  implicit lazy val bindingModule = Configuration

  override def run(companyIds: Array[String]): Unit = {
    companyIds.foreach(companyId => {
      val defaultUserId = UserLocalServiceHelper().getDefaultUserId(companyId.toLong)

      createTagVocabulary(companyId.toLong, defaultUserId)
    })
  }

  private def createTagVocabulary(companyId: Long, userId: Long) {
    val vocabularyName = "ValamisPackageTags"
    val globalGroupId = GroupLocalServiceHelper.getCompanyGroup(companyId).getGroupId

    try {
      GroupLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName)
    } catch {
      case e: NoSuchVocabularyException =>
        AssetVocabularyLocalServiceHelper.addAssetVocabulary(companyId, vocabularyName)
    }
  }
}
