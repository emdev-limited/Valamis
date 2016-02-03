package com.arcusys.learn.liferay.service

import com.arcusys.learn.liferay.LiferayClasses.LHitsOpenSearchImpl
import com.arcusys.learn.liferay.util.IndexerRegistryUtilHelper
import com.arcusys.valamis.lesson.model.BaseManifest

class OpenSearchImpl extends LHitsOpenSearchImpl {
  val SEARCH_PATH = "/c/valamis/open_search"
  val TITLE = "Valamis Search: "

  def getSearchPath = SEARCH_PATH

  def getPortletId = PackageIndexer.PORTLET_ID

  def getTitle(keywords: String) = TITLE + keywords

  override def getIndexer = IndexerRegistryUtilHelper.getIndexer(classOf[BaseManifest].getName)
}
