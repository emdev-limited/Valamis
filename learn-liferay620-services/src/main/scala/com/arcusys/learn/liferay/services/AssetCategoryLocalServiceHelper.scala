package com.arcusys.learn.liferay.services

import java.util.Locale

import com.arcusys.learn.liferay.LiferayClasses._
import com.liferay.counter.service.CounterLocalServiceUtil
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.util.OrderByComparator
import com.liferay.portal.service.{ClassNameLocalServiceUtil, GroupLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.util.PortalUtil
import com.liferay.portlet.asset.model.{AssetEntryModel, AssetEntry}
import com.liferay.portlet.asset.service.{AssetEntryLocalServiceUtil, AssetTagLocalServiceUtil, AssetCategoryLocalServiceUtil}

import scala.collection.JavaConverters._

object AssetCategoryLocalServiceHelper {
  lazy val groupClassNameId = ClassNameLocalServiceUtil.getClassNameId("com.liferay.portal.model.Group")

  def getVocabularyRootCategories(vocabularyId: Long, start: Int, end: Int) = {
    val orderByComparator: OrderByComparator = null
    AssetCategoryLocalServiceUtil.getVocabularyRootCategories(vocabularyId, start, end, orderByComparator)
  }

  def getVocabularyRootCategories(vocabularyId: Long): Seq[LAssetCategory] = {
    val orderByComparator: OrderByComparator = null
    AssetCategoryLocalServiceUtil
      .getVocabularyRootCategories(vocabularyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, orderByComparator)
      .asScala
  }

  def getCourseCategories(courseId: Long): Seq[LAssetCategory] = {
    getCourseEntryIds(courseId).flatMap(AssetCategoryLocalServiceUtil.getAssetEntryAssetCategories(_).asScala)
  }

  def getCourseEntryIds(courseId: Long): Seq[Long] = {
    val dq = AssetEntryLocalServiceUtil.dynamicQuery()
    dq.add(RestrictionsFactoryUtil.eq("classNameId", groupClassNameId))
    dq.add(RestrictionsFactoryUtil.eq("classPK", courseId))

    dq.setProjection(ProjectionFactoryUtil.projectionList()
      .add(ProjectionFactoryUtil.property("entryId")))

    AssetEntryLocalServiceUtil.dynamicQuery(dq).asScala.map(_.asInstanceOf[Long])
  }

  def addAssetCategory(companyId: Long, name: String) = {
    val vocabularyName = "ValamisPackageTags"
    val newId = CounterLocalServiceUtil.increment()
    val category = AssetCategoryLocalServiceUtil.createAssetCategory(newId)

    val globalGroupId = GroupLocalServiceHelper.getCompanyGroup(companyId).getGroupId
    val assetVocabularyId = GroupLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName).getVocabularyId

    val locale = PortalUtil.getSiteDefaultLocale(globalGroupId)

    val titles = new java.util.HashMap[Locale, String]()
    titles.put(locale, name)

    category.setVocabularyId(assetVocabularyId)
    category.setGroupId(globalGroupId)
    category.setTitleMap(titles)
    category.setDescriptionMap(titles)
    category.setUserId(UserLocalServiceUtil.getDefaultUserId(companyId))
    category.setParentCategoryId(0)
    category.setName(name)

    AssetCategoryLocalServiceUtil.updateAssetCategory(category)
  }
}
