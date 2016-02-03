package com.arcusys.learn.liferay.service

import java.util

import com.arcusys.learn.ioc.InjectableFactory
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.service.utils.PortletKeys
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{GetterUtilHelper, SearchEngineUtilHelper}
import com.arcusys.valamis.lesson.model.BaseManifest
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lesson.tincan.model.TincanManifest

object PackageIndexer {
  val PORTLET_ID: String = PortletKeys.ValamisPackage
  private final val CLASS_NAMES: Array[String] = Array(classOf[TincanManifest].getName, classOf[Manifest].getName, classOf[BaseManifest].getName)
}

class PackageIndexer extends ValamisBaseIndexer with InjectableFactory {
  lazy val packageService = inject[ValamisPackageService]

  def getClassNames: Array[String] = PackageIndexer.CLASS_NAMES

  def getPortletId: String = PackageIndexer.PORTLET_ID

  protected def addReindexCriteria(dynamicQuery: LDynamicQuery, companyId: Long) {
  }

  protected def doDelete(obj: Object) {
    val pkg = toBaseManifest(obj)
    for(asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id))
      deleteDocument(asset.getCompanyId, asset.getPrimaryKey)
  }

  protected def doGetDocument(obj: Object) = {
    val pkg = toBaseManifest(obj)
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(pkg.getClass.getName, pkg.id)

    val document = new LDocumentImpl
    document.addUID(PackageIndexer.PORTLET_ID, asset.getPrimaryKey)
    document.addKeyword(FieldHelper.COMPANY_ID, asset.getCompanyId)
    document.addKeyword(FieldHelper.ENTRY_CLASS_NAME, pkg.getClass.getName)
    document.addKeyword(FieldHelper.ENTRY_CLASS_PK, pkg.id)
    document.addKeyword(FieldHelper.PORTLET_ID, PackageIndexer.PORTLET_ID)
    document.addKeyword(FieldHelper.GROUP_ID, asset.getGroupId)
    //      pkg.summary.foreach( summary => document.addText(FieldHelper.CONTENT, HtmlUtil.extractText(summary)))
    document.addText(FieldHelper.DESCRIPTION, asset.getSummary)
    document.addText(FieldHelper.TITLE, asset.getTitle)
    document
  }

  protected def doReindex(obj: Object) {
    val pkg = toBaseManifest(obj)

    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(pkg.getClass.getName, pkg.id)
    SearchEngineUtilHelper.updateDocument(getSearchEngineId, assetEntry.getCompanyId, getDocument(pkg))
  }

  protected def doReindex(className: String, classPK: Long) {
    val pkg = packageService.getPackage(className, classPK)
    reindex(pkg)
  }

  protected def doReindex(ids: Array[String]) {
    val companyId: Long = GetterUtilHelper.getLong(ids(0))
    reindex(companyId)
  }

  protected def getPortletId(searchContext: LSearchContext): String = PackageIndexer.PORTLET_ID

  protected def reindex(pkg: BaseManifest) {
    val documents = new util.ArrayList[LDocument]
    documents.add(getDocument(pkg))

    for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id))
      SearchEngineUtilHelper.updateDocuments(getSearchEngineId, asset.getCompanyId, documents)
  }

  protected def reindex(companyId: Long) {
    reindexKBArticles(companyId, 0, 0)
  }

  protected def reindexKBArticles(companyId: Long, startKBArticleId: Long, endKBArticleId: Long) {
    val packages = packageService.getAll
    .filter { pkg =>
      AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id)
        .exists(_.getCompanyId == companyId)
    }

    val documents = new java.util.ArrayList[LDocument]
    for (pkg <- packages) {
      val document = doGetDocument(pkg)
      documents.add(document)
    }
    SearchEngineUtilHelper.updateDocuments(getSearchEngineId, companyId, documents)
  }

  protected def toBaseManifest(obj: Object): BaseManifest = {
    obj match {
      case s: Manifest => s
      case t: TincanManifest => t
      case a: LAssetEntry => packageService.getPackage(a.getClassPK)
      case _ => obj.asInstanceOf[Manifest]
    }
  }
}