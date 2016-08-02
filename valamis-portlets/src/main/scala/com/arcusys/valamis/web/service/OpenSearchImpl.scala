package com.arcusys.valamis.web.service

import com.arcusys.learn.liferay.model.ValamisBaseOpenSearchImpl
import com.arcusys.valamis.lesson.model.Lesson

class OpenSearchImpl extends ValamisBaseOpenSearchImpl {
  val SEARCH_PATH = "/c/valamis/open_search"
  val TITLE = "Valamis Search: "

  override def getSearchPath = SEARCH_PATH

  override def getPortletId = LessonIndexer.PortletId

  override def getTitle(keywords: String) = TITLE + keywords

  override def getClassName: String = classOf[Lesson].getName
}
