package com.arcusys.learn.liferay.model

import java.util.Locale
import javax.portlet.{PortletRequest, PortletResponse}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.util.{SearchEngineUtilHelper, StringUtilHelper, ValidatorHelper}
import com.liferay.portal.kernel.search.BaseIndexer

import scala.reflect.{ClassTag, _}

abstract class ValamisBaseIndexer[T: ClassTag] extends BaseIndexer[T] {


  override def postProcessSearchQuery(searchQuery: LBooleanQuery, searchContext: LSearchContext) {
    addSearchTerm(searchQuery, searchContext, FieldHelper.CONTENT, true)
    addSearchTerm(searchQuery, searchContext, FieldHelper.TITLE, true)
  }

  override def search(searchContext: LSearchContext): LHits = {
    val hits = super.search(searchContext)
    val queryTerms: Array[String] = hits.getQueryTerms
    hits.setQueryTerms(queryTerms)
    hits
  }

  protected def doGetSummary(document: LDocument,
                             locale: Locale,
                             snippet: String,
                             portletRequest: PortletRequest,
                             portletResponse: PortletResponse): LSummary = {
    val title = document.get(FieldHelper.TITLE)
    val content = {
      if (ValidatorHelper.isNull(snippet) && ValidatorHelper.isNull(document.get(FieldHelper.DESCRIPTION))) StringUtilHelper.shorten(document.get(FieldHelper.CONTENT), 200)
      else if (ValidatorHelper.isNull(snippet)) document.get(FieldHelper.DESCRIPTION)
      else snippet
    }

    new LSummary(title, content)
  }

  override def getClassName: String = classTag[T].runtimeClass.getName

  /**
    * In Liferay 7 getPoprtletId is deprecated (BaseIndex.java) and #deleteDocument
    * uses getClassName() to add UID for document.
    * Valamis uses portletId.
    *
    * @param companyId
    * @param uidNumber is a last part of UID.
    */
  override protected def deleteDocument(companyId: Long, uidNumber: String) {
    val document = new LDocumentImpl()
    document.addUID(getPortletId(), uidNumber)
    SearchEngineUtilHelper.deleteDocument(getSearchEngineId, companyId, document)
  }

  override def doGetDocument(o: T): LDocument = proxyGetDocument(o)

  override def doReindex(o: T): Unit = proxyReindex(o)

  override def doDelete(o: T): Unit = proxyDelete(o)

  protected def proxyGetDocument(t: T): LDocument

  protected def proxyReindex(t: T): Unit

  protected def proxyDelete(t: T): Unit
}
