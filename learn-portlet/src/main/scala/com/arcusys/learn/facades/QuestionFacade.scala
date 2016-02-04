package com.arcusys.learn.facades

import java.io.{File, InputStream}

import com.arcusys.learn.export.question.{QuestionExportProcessor, QuestionImportProcessor, QuestionMoodleImportProcessor}
import com.arcusys.learn.models.ImportResult
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class QuestionFacade(implicit val bindingModule: BindingModule)
  extends QuestionFacadeContract
  with Injectable {

  override def exportAllQuestionsBase(courseId: Long): InputStream = {
    new QuestionExportProcessor().exportAll(courseId)
  }

  override def importQuestions(file: File, courseID: Int): Unit = {
    new QuestionImportProcessor().importItems(file, courseID)
  }

  override def importMoodleQuestions(file: File, courseID: Int): ImportResult = {
    new QuestionMoodleImportProcessor(courseID).importItems(file)
  }

  override def exportQuestions(categoryIds: Seq[Long], questionIds: Seq[Long], plainTextIds: Seq[Long], courseID: Option[Long]): InputStream = {
    new QuestionExportProcessor().exportIds(categoryIds, questionIds, plainTextIds, courseID)
  }

}