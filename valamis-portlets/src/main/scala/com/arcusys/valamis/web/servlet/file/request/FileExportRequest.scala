package com.arcusys.valamis.web.servlet.file.request

import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest, Parameter}

object FileExportRequest extends BaseRequest {

  val CompanyId = "companyID"
  val CategoryId = "categoryID"
  val PlainTextId = "plainTextId"

  val ExportAll = "EXPORTALL"
  val Export = "EXPORT"
  val Download = "DOWNLOAD"
  val ContentType = "contentType"

  val ExportExtension = ".zip"

  val SlideSet = "SLIDE_SET"
  val Lesson = "lesson"
  val Question = "question"
  val Certificate = "certificate"
  val Package = "package"

  val Id = "id"

  val PublishType = "publishType"

  val Theme = "theme"
  val RandomOrdering = "randomOrdering"
  val QuestionsCount = "questionsCount"
  val ScoreLimit = "scoreLimit"

  def apply(scalatra: FileUploading) = new Model(scalatra)

  class Model(scalatra: FileUploading) extends BaseCollectionFilteredRequestModel(scalatra) {
    implicit val request = scalatra.request
    def action = Parameter(Action).required
    def contentType = Parameter(ContentType).required
    def courseId = Parameter(CourseId).intRequired
    def companyId = Parameter(CompanyId).intRequired
    def ids = Parameter(Id).multiLong
    def categoryIds = Parameter(CategoryId).multiLong
    def plainTextIds = Parameter(PlainTextId).multiLong
    def id = Parameter(Id).intRequired
    def theme = Parameter(Theme).option
    def randomOrdering = Parameter(RandomOrdering).booleanOption.getOrElse(false)
    def questionPerUser = Parameter(QuestionsCount).intOption
    def scoreLimit = Parameter(ScoreLimit).doubleOption
  }

}
