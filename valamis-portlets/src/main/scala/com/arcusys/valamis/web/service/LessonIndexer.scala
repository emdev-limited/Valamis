package com.arcusys.valamis.web.service

import java.util

import com.arcusys.learn.liferay.LiferayClasses.{LDocument, LDocumentImpl, LSearchContext}
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.model.ValamisBaseIndexer
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{GetterUtilHelper, PortletName, SearchEngineUtilHelper}
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.web.configuration.ioc.Configuration

object LessonIndexer {
  val PortletId: String = PortletName.LessonViewer.key
}

class LessonIndexer extends ValamisBaseIndexer[Lesson] {
  private lazy val lessonService = Configuration.inject[LessonService](None)
  private lazy val fileService = Configuration.inject[FileService](None)
  private lazy val courseService = Configuration.inject[CourseService](None)

  override def getPortletId: String = LessonIndexer.PortletId

  protected def proxyDelete(lesson: Lesson) {
    for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(getClassName, lesson.id))
      deleteDocument(asset.getCompanyId, asset.getPrimaryKey)

  }

  private def getSearchContentForPackage(lessonId: Long): String = {
    val content = fileService.getFileContentOption("data/" + lessonId + "/data/" + SearchEngineUtilHelper.SearchContentFileName)
    content.fold("")(new String(_, SearchEngineUtilHelper.SearchContentFileCharset))
  }

  protected def proxyGetDocument(lesson: Lesson): LDocument = {
    val lessonId = lesson.id
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(getClassName, lessonId)

    val document = new LDocumentImpl
    document.addUID(LessonIndexer.PortletId, asset.getPrimaryKey)
    document.addKeyword(FieldHelper.COMPANY_ID, asset.getCompanyId)
    document.addKeyword(FieldHelper.ENTRY_CLASS_NAME, getClassName)
    document.addKeyword(FieldHelper.ENTRY_CLASS_PK, lessonId)
    document.addKeyword(FieldHelper.PORTLET_ID, LessonIndexer.PortletId)
    document.addDate(FieldHelper.MODIFIED_DATE, asset.getModifiedDate) // Should be set for LR7 (check in OpenSearch while searching).
    document.addKeyword(FieldHelper.GROUP_ID, asset.getGroupId)
    document.addKeyword(FieldHelper.SCOPE_GROUP_ID, asset.getGroupId)
    document.addKeyword(FieldHelper.CONTENT, getSearchContentForPackage(lessonId))
    document.addText(FieldHelper.DESCRIPTION, lesson.description)
    document.addText(FieldHelper.TITLE, lesson.title)
    document
  }

  protected def proxyReindex(lesson: Lesson) {
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(getClassName, lesson.id)
    SearchEngineUtilHelper.updateDocument(getSearchEngineId, assetEntry.getCompanyId, getDocument(lesson))
  }

  protected def doReindex(className: String, classPK: Long) {
    reindexByLesson(classPK)
  }

  protected def doReindex(ids: Array[String]) {
    val companyId: Long = GetterUtilHelper.getLong(ids(0))
    reindexByCompany(companyId)
  }

  override protected def getPortletId(searchContext: LSearchContext): String = LessonIndexer.PortletId

  protected def reindexByLesson(lessonId: Long) {
    val documents = new util.ArrayList[LDocument]
    lessonService
      .getLesson(lessonId)
      .foreach(lesson => documents.add(getDocument(lesson)))

    for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(getClassName, lessonId))
      SearchEngineUtilHelper.updateDocuments(getSearchEngineId, asset.getCompanyId, documents)
  }

  protected def reindexByCompany(companyId: Long) {
    reindexKBArticles(companyId, 0, 0)
  }

  protected def reindexKBArticles(companyId: Long, startKBArticleId: Long, endKBArticleId: Long) {
    val indexer = getSearchEngineId
    courseService.getByCompanyId(companyId).toStream
      .flatMap(course => lessonService.getAllVisible(course.getGroupId))
      .filter(lesson => lesson.isVisible.getOrElse(true))
      .foreach(lesson => SearchEngineUtilHelper.updateDocument(indexer, companyId, getDocument(lesson)))
  }
}