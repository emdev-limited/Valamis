package com.arcusys.learn.liferay.services

import java.util.{Date, Locale}

import com.liferay.counter.service.CounterLocalServiceUtil
import com.liferay.portal.service.{GroupLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.util.PortalUtil
import com.liferay.portlet.asset.NoSuchVocabularyException
import com.liferay.portlet.asset.model.AssetVocabulary
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil

object AssetVocabularyLocalServiceHelper {
  def getGroupVocabulary(globalGroupId: Long, vocabularyName: String): AssetVocabulary = {
    AssetVocabularyLocalServiceUtil.getGroupVocabulary(globalGroupId, vocabularyName)
  }

  def addAssetVocabulary(companyId: Long, vocabularyName: String): AssetVocabulary = {
    val globalGroupId = GroupLocalServiceUtil.getCompanyGroup(companyId).getGroupId
    val defaultUserId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)

    try {
      GroupLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName)
    } catch {
      case e: NoSuchVocabularyException =>
        val newId = CounterLocalServiceUtil.increment()
        val vocabulary = AssetVocabularyLocalServiceUtil.createAssetVocabulary(newId)
        val locale = PortalUtil.getSiteDefaultLocale(globalGroupId)
        val date = new Date()
        val titles = new java.util.HashMap[Locale, String]()
        titles.put(locale, vocabularyName)
        vocabulary.setCompanyId(companyId)
        vocabulary.setGroupId(globalGroupId)
        vocabulary.setTitleMap(titles)
        vocabulary.setDescriptionMap(titles)
        vocabulary.setUserId(defaultUserId)
        vocabulary.setCreateDate(date)
        vocabulary.setModifiedDate(date)
        vocabulary.setName(vocabularyName)

        val voc = AssetVocabularyLocalServiceUtil.addAssetVocabulary(vocabulary)
        addVocabularyResources(vocabulary, addGroupPermissions = true, addGuestPermissions = true)
        voc
    }
  }

  def addVocabularyResources(vocabulary: AssetVocabulary,
                             addGroupPermissions: Boolean,
                             addGuestPermissions: Boolean): Unit = {
    AssetVocabularyLocalServiceUtil.addVocabularyResources(vocabulary, addGroupPermissions, addGuestPermissions)
  }
}
