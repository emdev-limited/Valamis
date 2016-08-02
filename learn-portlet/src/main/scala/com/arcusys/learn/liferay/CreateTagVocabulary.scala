package com.arcusys.learn.liferay

/**
 * Created by aklimov on 05.02.15.
 */

import com.arcusys.learn.liferay.LiferayClasses.{LNoSuchVocabularyException, LSimpleAction}
import com.arcusys.learn.liferay.services.{AssetVocabularyLocalServiceHelper, GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class CreateTagVocabulary extends LSimpleAction with Injectable {
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
      case e: LNoSuchVocabularyException =>
        AssetVocabularyLocalServiceHelper.addAssetVocabulary(companyId, vocabularyName)
    }
  }
}
