package com.arcusys.learn.liferay.helpers

import java.util.Locale

import com.liferay.portlet.journal.model.JournalArticle

trait JournalArticleHelpers {
  def getMap(article: JournalArticle) = Map("articleID" -> article.getArticleId,
    "groupID" -> article.getGroupId.toString,
    "version" -> article.getVersion.toString,
    "availableLocales" -> article.getAvailableLocales.map(localeStr => {
      val localeSplit = localeStr.split('_')
      val locale = if (localeSplit.size > 1) new Locale(localeSplit(0), localeSplit(1)) else new Locale(localeSplit.head)
      localeStr -> Map("language" -> locale.getDisplayLanguage, "country" -> locale.getDisplayCountry)
    }).toMap,
    "titles" -> article.getAvailableLocales.map(locale => locale -> article.getTitle(locale)).toMap)
}
