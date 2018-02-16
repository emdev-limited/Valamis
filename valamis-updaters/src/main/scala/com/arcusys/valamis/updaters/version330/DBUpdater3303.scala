package com.arcusys.valamis.updaters.version330

import com.arcusys.learn.liferay.LiferayClasses.{LNoSuchVocabularyException, LUpgradeProcess}
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.{AssetCategoryLocalServiceHelper, AssetVocabularyLocalServiceHelper, CompanyLocalServiceHelper, GroupLocalServiceHelper}

class DBUpdater3303() extends LUpgradeProcess {

  override def getThreshold = 3303

  private val log = LogFactoryHelper.getLog(getClass)

  private val vocabularyName = "ValamisPackageTags"

  override def doUpgrade(): Unit = {
    log.info("Adding permissions to Valamis Category Vocabulary")
    CompanyLocalServiceHelper
      .getCompanies
      .foreach { company =>
        val globalGroupId = GroupLocalServiceHelper.getCompanyGroup(company.getCompanyId).getGroupId
        try {
          val vocabulary = AssetVocabularyLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName)
          log.info(s"Vocabulary is found in company: ${company.getCompanyId}. Applying permissions")
          AssetVocabularyLocalServiceHelper.addVocabularyResources(
            vocabulary,
            addGroupPermissions = true,
            addGuestPermissions = true
          )
          AssetCategoryLocalServiceHelper
            .getVocabularyRootCategories(vocabulary.getVocabularyId)
            .foreach { cat =>
              if(cat.getCompanyId == 0) {
                log.info(s"Category '${cat.getName}' has incorrect company id. Fixing")
                cat.setCompanyId(company.getCompanyId)
                AssetCategoryLocalServiceHelper.updateAssetCategory(cat)
              }
              AssetCategoryLocalServiceHelper.addCategoryResources(
                cat,
                addGroupPermissions = true,
                addGuestPermissions = true
              )
            }
        } catch {
          case e: LNoSuchVocabularyException =>
            log.info(s"Vocabulary is not found in company: ${company.getCompanyId}. Skipping")

        }
      }

  }
}