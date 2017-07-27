package com.arcusys.learn.liferay.model

import java.util.Locale
import javax.portlet.PortletURL

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.util.{StringUtilHelper, ValidatorHelper}
import com.liferay.portal.kernel.search.BaseIndexer

import scala.reflect.{ClassTag, _}


abstract class ValamisBaseIndexer[T: ClassTag] extends BaseIndexer {

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

  protected def doGetSummary(document: LDocument, locale: Locale, snippet: String, portletURL: PortletURL): LSummary = {
    val title = document.get(FieldHelper.TITLE)
    val content = {
      if (ValidatorHelper.isNull(snippet) && ValidatorHelper.isNull(document.get(FieldHelper.DESCRIPTION))) StringUtilHelper.shorten(document.get(FieldHelper.CONTENT), 200)
      else if (ValidatorHelper.isNull(snippet)) document.get(FieldHelper.DESCRIPTION)
      else snippet
    }

    val resourcePrimKey = document.get(FieldHelper.ENTRY_CLASS_PK)
    portletURL.setParameter("resourcePrimKey", resourcePrimKey)
    new LSummary(title, content, portletURL)
  }

  def getClassName: String = classTag[T].runtimeClass.getName

  override def getClassNames: Array[String] = Array(getClassName)


  override def doGetDocument(o: Object): LDocument = proxyGetDocument(o.asInstanceOf[T])

  override def doReindex(o: Object): Unit = proxyReindex(o.asInstanceOf[T])

  override def doDelete(o: Object): Unit = proxyDelete(o.asInstanceOf[T])

  protected def proxyGetDocument(t: T): LDocument

  protected def proxyReindex(t: T): Unit

  protected def proxyDelete(t: T): Unit
}
