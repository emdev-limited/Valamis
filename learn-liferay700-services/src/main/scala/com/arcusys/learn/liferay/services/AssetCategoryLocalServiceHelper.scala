package com.arcusys.learn.liferay.services

import java.util.Locale

import com.arcusys.learn.liferay.LiferayClasses._
import com.liferay.asset.kernel.service.{AssetCategoryLocalServiceUtil, AssetEntryLocalServiceUtil}
import com.liferay.counter.kernel.service.CounterLocalServiceUtil
import com.liferay.portal.kernel.dao.orm.{ProjectionFactoryUtil, QueryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.kernel.model.Group
import com.liferay.portal.kernel.service.{ClassNameLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.kernel.util.{OrderByComparator, PortalUtil}

import scala.collection.JavaConverters._
import scala.util.Try

object AssetCategoryLocalServiceHelper {
  lazy val groupClassNameId = ClassNameLocalServiceUtil.getClassNameId(classOf[Group])

  def getVocabularyRootCategories(vocabularyId: Long): Seq[LAssetCategory] = {
    val orderByComparator: OrderByComparator[LAssetCategory] = null
    AssetCategoryLocalServiceUtil
      .getVocabularyRootCategories(vocabularyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, orderByComparator)
      .asScala
  }

  def getAssetCategory(categoryId: Long): Option[LAssetCategory] =
    Try(AssetCategoryLocalServiceUtil.fetchAssetCategory(categoryId)).toOption

  def getAssetEntryAssetCategories(entryId: Long): Seq[LAssetCategory] =
    AssetCategoryLocalServiceUtil.getAssetEntryAssetCategories(entryId).asScala

  def getCourseCategories(courseId: Long): Seq[LAssetCategory] = {
    getCourseEntryIds(courseId).flatMap(AssetCategoryLocalServiceUtil.getAssetEntryAssetCategories(_).asScala)
  }

  def getCourseEntryIds(courseId: Long): Seq[Long] = {
    val dq = AssetEntryLocalServiceUtil.dynamicQuery()
    dq.add(RestrictionsFactoryUtil.eq("classNameId", groupClassNameId))
    dq.add(RestrictionsFactoryUtil.eq("classPK", courseId))

    dq.setProjection(ProjectionFactoryUtil.projectionList()
      .add(ProjectionFactoryUtil.property("entryId")))

    AssetEntryLocalServiceUtil.dynamicQuery[Long](dq).asScala
  }

  def addAssetCategory(companyId: Long, name: String): LAssetCategory = {
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
    category.setCompanyId(companyId)

    val cat = AssetCategoryLocalServiceUtil.addAssetCategory(category)
    addCategoryResources(cat, addGroupPermissions = true, addGuestPermissions = true)
    cat
  }

  def addCategoryResources(category: LAssetCategory,
                           addGroupPermissions: Boolean,
                           addGuestPermissions: Boolean): Unit = {
    AssetCategoryLocalServiceUtil.addCategoryResources(category, addGroupPermissions, addGuestPermissions)
  }

  def updateAssetCategory(category: LAssetCategory): Unit = {
    AssetCategoryLocalServiceUtil.updateAssetCategory(category)
  }
}
