package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.services.{ AssetCategoryLocalServiceHelper, AssetEntryLocalServiceHelper, AssetVocabularyLocalServiceHelper, GroupLocalServiceHelper }
import com.arcusys.valamis.lesson.model.{PackageBase, BaseManifest, ValamisTag}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.storage.PlayerScopeRuleStorage
import com.arcusys.valamis.lesson.tincan.model.{TincanManifest, TincanPackage}
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.arcusys.valamis.model.ScopeType
import com.escalatesoft.subcut.inject.{ BindingModule, Injectable }
import com.liferay.portlet.asset.NoSuchVocabularyException
import com.liferay.portlet.asset.model.AssetCategory
import com.liferay.portlet.asset.service.{AssetEntryLocalServiceUtil, AssetCategoryLocalServiceUtil}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

/**
 * Created by Yuriy Gatilin on 29.01.15.
 */
class TagService(implicit val bindingModule: BindingModule) extends TagServiceContract with Injectable {
  val vocabularyName = "ValamisPackageTags"
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val playerScopeRuleRepository = inject[PlayerScopeRuleStorage]
  private lazy val tincanRepository = inject[TincanPackageStorage]
  private lazy val scormRepository = inject[ScormPackagesStorage]
  private lazy val scopePackageService = inject[ScopePackageService]


  def getAll(companyId: Long): Seq[ValamisTag] = {
    val globalGroupId = GroupLocalServiceHelper.getCompanyGroup(companyId).getGroupId
    try {
      val assetVocabulary = GroupLocalServiceHelper.getGroupVocabulary(globalGroupId, vocabularyName)
      val categories = AssetCategoryLocalServiceUtil.getVocabularyRootCategories(assetVocabulary.getVocabularyId, -1, -1, null)
        .asScala.map(extract)
      categories
    } catch {
      case e: NoSuchVocabularyException =>
        AssetVocabularyLocalServiceHelper.addAssetVocabulary(companyId, vocabularyName)
        getAll(companyId)
    }
  }

  def getPackagesTagsByCompany(companyId: Long): Seq[ValamisTag] ={
    AssetEntryLocalServiceUtil.getAssetEntries(-1, -1).asScala.filter {entry =>
      ((entry.getClassName == classOf[TincanManifest].getName
        || entry.getClassName == classOf[Manifest].getName)
        && entry.getCompanyId == companyId)
    } flatMap { e=>
      e.getCategories asScala
    } map extract distinct
  }

  def getPackagesTagsByCourse(courseId: Long): Seq[ValamisTag] = {
    packageService.getPackagesByCourse(courseId)
      .flatMap(getEntryTags)
      .distinct
  }

  def getPackagesTagsByPlayerId(playerId: String, companyId: Long, courseId: Long, pageId: String): Seq[ValamisTag] = {
    val scope = playerScopeRuleRepository.get(playerId).map(_.scope).getOrElse(ScopeType.Site)

    val date = new DateTime()
    lazy val courseIds = scopePackageService.getAllCourseIds(companyId)

    val (tPackages, sPackages) = scope match {
      case ScopeType.Instance => (
        tincanRepository.getInstanceScopeOnlyVisible(courseIds, None, date),
        scormRepository.getInstanceScopeOnlyVisible(courseIds, None, date)
        )
      case _ =>
        val scopeId = scope match {
          case ScopeType.Site => courseId.toString
          case ScopeType.Page => pageId
          case ScopeType.Player => playerId
        }
        (
          tincanRepository.getOnlyVisible(scope, scopeId, None, date),
          scormRepository.getOnlyVisible(scope, scopeId, None, date)
          )
    }

    val tTags = AssetEntryLocalServiceHelper.fetchAssetEntries(classOf[TincanManifest].getName, tPackages.map(_.id))
    val sTags = AssetEntryLocalServiceHelper.fetchAssetEntries(classOf[Manifest].getName, sPackages.map(_.id))

    (tTags ++ sTags).flatMap(_.getCategories.asScala).map(extract).distinct
  }

  def getEntryTags(pkg: PackageBase): Seq[ValamisTag] = {
    val className = pkg match {
      case p: TincanPackage => classOf[TincanManifest].getName
      case p: ScormPackage => classOf[Manifest].getName
      case _ => throw new UnsupportedOperationException();
    }
    AssetEntryLocalServiceHelper.fetchAssetEntry(className, pkg.id) match {
      case Some(asset) =>
        asset.getCategories.asScala.map(extract).toSeq
      case None => Seq()
    }
  }

  def getEntryTags(manifest: BaseManifest): Seq[ValamisTag] = {
    AssetEntryLocalServiceHelper.fetchAssetEntry(manifest.getClass.getName, manifest.id) match {
      case Some(asset) =>
        asset.getCategories.asScala.map(extract).toSeq
      case None => Seq()
    }
  }

  def assignTags(entryId: Long, tagsId: Seq[Long]) = {
    AssetEntryLocalServiceHelper.setAssetCategories(entryId, tagsId.toArray) // feel the difference between add/set
  }

  def unassignTags(entryId: Long, tagsId: Seq[Long]) = {
    AssetEntryLocalServiceHelper.removeAssetCategories(entryId, tagsId.toArray)
  }

  def getTagIds(rawIds: Seq[String], companyId: Long): Seq[Long] = {
    val existingTags = getAll(companyId)

    for (tag <- rawIds) yield {
      if (existingTags.exists(_.id.toString == tag))
        tag.toLong
      else {
        val assetCategory = AssetCategoryLocalServiceHelper.addAssetCategory(companyId, tag)
        assetCategory.getCategoryId
      }
    }
  }

  private def extract(c: AssetCategory) =
    ValamisTag(c.getCategoryId, c.getName)
}