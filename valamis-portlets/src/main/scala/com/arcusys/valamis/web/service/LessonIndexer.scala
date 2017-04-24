package com.arcusys.valamis.web.service

import java.util

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.FieldHelper
import com.arcusys.learn.liferay.model.ValamisBaseIndexer
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{GetterUtilHelper, PortletName, SearchEngineUtilHelper}
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.web.configuration.ioc.InjectableFactory

object LessonIndexer {
  val PortletId: String = PortletName.LessonViewer.key

  val LessonClassName = "com.arcusys.valamis.lesson.model.Lesson"

  private final val ClassNames: Array[String] = Array(LessonClassName)
}

class LessonIndexer extends ValamisBaseIndexer with InjectableFactory {
  lazy val lessonService = inject[LessonService]
  lazy val fileService = inject[FileService]
  lazy val courseService = inject[CourseService]

  override def getClassName: String = LessonIndexer.LessonClassName

  override def getClassNames: Array[String] = LessonIndexer.ClassNames

  override def getPortletId: String = LessonIndexer.PortletId

  protected def addReindexCriteria(dynamicQuery: LDynamicQuery, companyId: Long) {
  }

  protected def doDelete(obj: Object) {
    val lessonId = getLessonId(obj)

    for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(LessonIndexer.LessonClassName, lessonId))
      deleteDocument(asset.getCompanyId, asset.getPrimaryKey)

  }

  private def getSearchContentForPackage(lessonId: Long): String = {
    val content = fileService.getFileContentOption("data/" + lessonId + "/data/" + SearchEngineUtilHelper.SearchContentFileName)
    content.fold("")(new String(_,SearchEngineUtilHelper.SearchContentFileCharset))
  }

  protected def doGetDocument(obj: Object): LDocument =  {
    val lessonId = getLessonId(obj)
    val lesson = lessonService.getLessonRequired(lessonId)
    val asset = AssetEntryLocalServiceHelper.getAssetEntry(LessonIndexer.LessonClassName, lessonId)

    val document = new LDocumentImpl
    document.addUID(LessonIndexer.PortletId, asset.getPrimaryKey)
    document.addKeyword(FieldHelper.COMPANY_ID, asset.getCompanyId)
    document.addKeyword(FieldHelper.ENTRY_CLASS_NAME, LessonIndexer.LessonClassName)
    document.addKeyword(FieldHelper.ENTRY_CLASS_PK, lessonId)
    document.addKeyword(FieldHelper.PORTLET_ID, LessonIndexer.PortletId)
    document.addDate(FieldHelper.MODIFIED_DATE, asset.getModifiedDate)    // Should be set for LR7 (check in OpenSearch while searching).
    document.addKeyword(FieldHelper.GROUP_ID, asset.getGroupId)
    document.addKeyword(FieldHelper.SCOPE_GROUP_ID, asset.getGroupId)
    document.addKeyword(FieldHelper.CONTENT, getSearchContentForPackage(lessonId))
    document.addText(FieldHelper.DESCRIPTION, lesson.description)
    document.addText(FieldHelper.TITLE, lesson.title)
    document
  }

  protected def doReindex(obj: Object) {
    val lessonId = getLessonId(obj)

    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(LessonIndexer.LessonClassName, lessonId)
    SearchEngineUtilHelper.updateDocument(getSearchEngineId, assetEntry.getCompanyId, getDocument(obj))
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
    val asset = AssetEntryLocalServiceHelper.fetchAssetEntry(LessonIndexer.LessonClassName, lessonId)
    documents.add(getDocument(asset))

    for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(LessonIndexer.LessonClassName, lessonId))
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
      .flatMap(lesson => AssetEntryLocalServiceHelper.fetchAssetEntry(LessonIndexer.LessonClassName, lesson.id))
      .foreach(asset => SearchEngineUtilHelper.updateDocument(indexer, companyId, getDocument(asset)))
  }

  protected def getLessonId(obj: Object): Long = {
    obj match {
      case l: Lesson => l.id
      case a: LAssetEntry => a.getClassPK
    }
  }
}