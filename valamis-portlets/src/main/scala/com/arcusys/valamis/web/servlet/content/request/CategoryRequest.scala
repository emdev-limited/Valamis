package com.arcusys.valamis.web.servlet.content.request

import com.arcusys.valamis.web.servlet.request.{BaseRequest, Parameter}
import org.scalatra.ScalatraServlet

object CategoryRequest extends BaseRequest {

  val NewCourseId = "newCourseID"
  val CategoryId = "categoryId"
  val CategoryIds = "categoryIDs"

  val Id = "id"
  val ParentId = "parentId"
  val Categories = "categories"
  val Questions = "questions"
  val Title = "title"
  val Description = "description"
  val Index = "index"
  val DndMode = "dndMode"
  val TargetId = "targetId"
  val ItemType = "itemType"
  val CopyFromId = "copyFromId"


  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(controller: ScalatraServlet) {
    implicit val _controller = controller

    def action = {
      Parameter(Action).option match {
        case Some(value) => CategoryActionType.withName(value.toUpperCase)
        case None        => None
      }
    }

    def copyFromId = Parameter(CopyFromId).intOption

    def itemType = Parameter(ItemType).required

    def categoryId = Parameter(CategoryId).intOption

    def categoryIds = Parameter(CategoryIds).multiLong

    def courseId = Parameter(CourseId).longRequired

    @deprecated
    def courseIdInt = Parameter(CourseId).intOption

    def newCourseId = Parameter(NewCourseId).longRequired

    @deprecated
    def newCourseIdInt = Parameter(NewCourseId).intOption

    def parentId = Parameter(ParentId).longOption
    @deprecated
    def parentIdInt = Parameter(ParentId).intOption

    def id = Parameter(Id).intRequired

    def categoryIdSet = Parameter(Categories).multiRequired.map(x => x.toInt)

    def questionsIdSet = Parameter(Questions).multiRequired.map(x => x.toInt)

    def title = Parameter(Title).required

    def description = Parameter(Description).option.getOrElse("")

    def index = Parameter(Index).intRequired

    def targetId = Parameter(TargetId).intRequired

  }

}
