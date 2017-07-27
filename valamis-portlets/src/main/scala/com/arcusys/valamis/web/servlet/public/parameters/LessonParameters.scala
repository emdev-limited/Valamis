package com.arcusys.valamis.web.servlet.public.parameters

import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.web.servlet.public.LessonServlet
import com.arcusys.valamis.web.servlet.public.model.request.MemberRequest
import org.json4s.JsonAST.JString
import org.json4s.CustomSerializer

/**
  * Created by pkornilov on 1/30/17.
  */
trait LessonParameters extends BaseParameters { self: LessonServlet =>

  def lessonType = params.getAs[String]("lessonType") map {
    case "tincan" => LessonType.Tincan
    case "scorm" => LessonType.Scorm
    case tpe => haltWithBadRequest("Wrong lesson type: " + tpe)
  }

  def memberType = params.as[String]("memberType") match {
    case "role" => MemberTypes.Role
    case "user" => MemberTypes.User
    case "userGroup" => MemberTypes.UserGroup
    case "organization" => MemberTypes.Organization
    case v => haltWithBadRequest(s"Wrong memberType value: " + v)
  }

  def ratingScore = params.as[Double]("score")

}
