package com.arcusys.learn.liferay.service

import java.util

import com.arcusys.learn.ioc.InjectableFactory
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.service.utils.PortletKeys
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{GetterUtilHelper, SearchEngineUtilHelper}
import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter}
import com.arcusys.valamis.certificate.storage.CertificateRepository

object CertificateIndexer {
  val PortletId: String = PortletKeys.ValamisCertificate
  private final val ClassNames: Array[String] = Array[String](classOf[Certificate].getName)
}

class CertificateIndexer extends ValamisBaseIndexer with InjectableFactory {
  private lazy val certificateRepository = inject[CertificateRepository]

  def getClassNames: Array[String] = CertificateIndexer.ClassNames

  def getPortletId: String = CertificateIndexer.PortletId

  protected def doDelete(obj: Object) {
    val cert = toCertificate(obj)
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(classOf[Certificate].getName, cert.id)
    deleteDocument(assetEntry.getCompanyId, assetEntry.getCompanyId)
  }

  protected def doGetDocument(obj: Object) = {
    val cert = toCertificate(obj)
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(classOf[Certificate].getName, cert.id)
    val document = new LDocumentImpl
    document.addUID(PackageIndexer.PORTLET_ID, asset.getPrimaryKey)
    document.addKeyword(FieldHelper.COMPANY_ID, asset.getCompanyId)
    document.addKeyword(FieldHelper.ENTRY_CLASS_NAME, cert.getClass.getName)
    document.addKeyword(FieldHelper.ENTRY_CLASS_PK, cert.id)
    document.addKeyword(FieldHelper.PORTLET_ID, PackageIndexer.PORTLET_ID)
    document.addKeyword(FieldHelper.GROUP_ID, asset.getGroupId)
    // cert.summary.foreach( summary => document.addText(FieldHelper.CONTENT, HtmlUtil.extractText(summary)))
    document.addText(FieldHelper.DESCRIPTION, asset.getSummary)
    document.addText(FieldHelper.TITLE, asset.getTitle)
    document
  }

  protected def doReindex(obj: Object) {
    val cert = toCertificate(obj)
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(classOf[Certificate].getName, cert.id)
    SearchEngineUtilHelper.updateDocument(getSearchEngineId, asset.getCompanyId, getDocument(obj))
  }

  protected def doReindex(className: String, classPK: Long) {
    val cert = certificateRepository.getById(classPK)
    reindex(cert)
  }

  protected def doReindex(ids: Array[String]) {
    val companyId: Long = GetterUtilHelper.getLong(ids(0))
    reindex(companyId)
  }

  protected def getPortletId(searchContext: LSearchContext): String = CertificateIndexer.PortletId

  protected def reindex(cert: Certificate) {
    val documents = new util.ArrayList[LDocument]
    documents.add(getDocument(cert))
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(classOf[Certificate].getName, cert.id)
    SearchEngineUtilHelper.updateDocuments(getSearchEngineId, asset.getCompanyId, documents)
  }

  protected def reindex(companyId: Long) {
    reindexKBArticles(companyId, 0, 0)
  }

  protected def reindexKBArticles(companyId: Long, startKBArticleId: Long, endKBArticleId: Long) {
    val documents = new java.util.ArrayList[LDocument]
    for (cert <- certificateRepository.getBy(new CertificateFilter(companyId))) {
      val document = doGetDocument(cert)
      documents.add(document)
    }
    SearchEngineUtilHelper.updateDocuments(getSearchEngineId, companyId, documents)
  }

  protected def toCertificate(obj: Object): Certificate = {
    obj match {
      case s: Certificate => s
      case a: LAssetEntry => certificateRepository.getById(a.getClassPK) //.getOrElse(obj.asInstanceOf[Certificate])
      case _ => obj.asInstanceOf[Certificate]
    }
  }
}

