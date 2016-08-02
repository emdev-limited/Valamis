package com.arcusys.valamis.web.servlet.content

import com.arcusys.valamis.content.model.PlainText
import com.arcusys.valamis.content.service.PlainTextService
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.content.request.QuestionRequest
import com.arcusys.valamis.web.servlet.content.response.ContentResponseBuilder

import scala.util.Try

class PlainTextServlet extends BaseJsonApiController with ContentPolicy {

  lazy val plainTextService = inject[PlainTextService]

  get("/plaintext(/)(:id)") {
    val req = QuestionRequest(this)
    ContentResponseBuilder.toResponse(plainTextService.getPlainTextNodeById(req.id))
  }

  post("/plaintext/add(/)") {
    val req = QuestionRequest(this)
    val newPlainText = plainTextService.create(new PlainText(
      None,
      req.categoryId,
      req.title,
      req.text,
      req.courseId
    ))
    ContentResponseBuilder.toResponse(newPlainText)
  }

  post("/plaintext/update/:id(/)") {
    val id = Try(params.as[Long]("id")) getOrElse pass()
    val req = QuestionRequest(this)
    plainTextService.update(id, req.title, req.text)
  }

  post("/plaintext/delete/:id(/)") {
    val id = Try(params.as[Long]("id")) getOrElse pass()
    plainTextService.delete(id)
  }


  post("/plaintext/move/:id(/)") {
    val req = QuestionRequest(this)
    plainTextService.moveToCategory(req.id, req.parentId, req.courseId)
  }

  post("/plaintext/moveToCourse(/)") {
    val req = QuestionRequest(this)
    req.contentIds
      .foreach(id => plainTextService.moveToCourse(id, req.newCourseId.get, moveToRoot = true))
  }
}
