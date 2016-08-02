package com.arcusys.valamis.tag

import com.arcusys.learn.liferay.LiferayClasses.{LAssetCategory, LNoSuchVocabularyException}
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.tag.model.ValamisTag
import com.liferay.portal.kernel.dao.orm.QueryUtil

import scala.collection.JavaConverters._

class TagService[T: Manifest] {

  // T must have assetEntry
  // tags will be stored as assetEntry category

  val vocabularyName = "ValamisPackageTags"

  protected val className = manifest[T].runtimeClass.getName
  protected lazy val classNameId = ClassNameLocalServiceHelper.getClassNameId(className)

  def getAll(companyId: Long): Seq[ValamisTag] = {
    val globalGroupId = GroupLocalServiceHelper.getCompanyGroup(companyId).getGroupId
    val vocabularyId = try {
      AssetVocabularyLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName).getVocabularyId
    } catch {
      case e: LNoSuchVocabularyException =>
        AssetVocabularyLocalServiceHelper.addAssetVocabulary(companyId, vocabularyName).getVocabularyId
    }

    AssetCategoryLocalServiceHelper.getVocabularyRootCategories(vocabularyId).map(toTag)
  }

  def getByItemId(itemId: Long): Seq[ValamisTag] = {
    AssetEntryLocalServiceHelper.fetchAssetEntry(className, itemId)
      .map(_.getCategories.asScala).getOrElse(Nil)
      .map(toTag).distinct
  }

  def getByItemIds(itemIds: Seq[Long]): Seq[ValamisTag] = {
    itemIds.flatMap(id => getByItemId(id)).distinct
  }

  def setTags(assetId: Long, tagsId: Seq[Long]): Unit = {
    AssetEntryLocalServiceHelper.setAssetCategories(assetId, tagsId.toArray)
  }

  def removeTags(assetId: Long, tagsId: Seq[Long]): Unit = {
    AssetEntryLocalServiceHelper.removeAssetCategories(assetId, tagsId.toArray)
  }

  def getOrCreateTagIds(tagKeys: Seq[String], companyId: Long): Seq[Long] = {
    val existingTags = getAll(companyId)

    for (tagKey <- tagKeys) yield {
      if (existingTags.exists(_.id.toString == tagKey)) {
        // tagKey is id of exist tag
        tagKey.toLong
      } else {
        // tagKey is name of new tag
        val assetCategory = AssetCategoryLocalServiceHelper.addAssetCategory(companyId, tagKey)
        assetCategory.getCategoryId
      }
    }
  }

  def getItemIds(tagId: Long): Seq[Long] = {
    AssetEntryLocalServiceHelper.getAssetEntriesByCategory(tagId, classNameId).map(_.getClassPK).distinct
  }

  private def toTag(c: LAssetCategory) = ValamisTag(c.getCategoryId, c.getName)
}
