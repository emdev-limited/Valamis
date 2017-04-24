package com.arcusys.valamis.certificate.serializer

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.model.RangeResult
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

object AssignmentSerializer {
  implicit def jvalue2longextractable(jv: JValue) = new LongExtractableJsonAstNode(jv)

  class LongExtractableJsonAstNode(jv: JValue) {
    def extractLong(implicit formats: Formats, mf: scala.reflect.Manifest[Long]): Long = jv match {
      //under Liferay 7 JString is returned instead of JInt,
      //see com.liferay.portal.kernel.json.JSONObject.put(String key, long value)
      case v @ JString(_) => v.extract[String].toLong
      case v @ JInt(_) => v.extract[Long]
    }
  }

}

trait AssignmentSerializer {

  import AssignmentSerializer.jvalue2longextractable

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  def deserializeAssignment(json: String): Assignment = extractAssignmentFields(parse(json))

  def deserializeAssignmentList(json: String): RangeResult[Assignment] = {
    val jsonValue = parse(json)
    val count = (jsonValue \ AssignmentJsonFields.Count).extract[Int]
    val assignments = for {
      assignment <- (jsonValue \ AssignmentJsonFields.Assignments).children
    } yield extractAssignmentFields(assignment)

    RangeResult(count, assignments)
  }

  def deserializeUserList(json: String): RangeResult[AssignmentUserInfo] = {
    val jsonValue = parse(json)
    val count = (jsonValue \ AssignmentJsonFields.Count).extract[Int]
    val userSubmissions = extractUserList(jsonValue \ AssignmentJsonFields.Users)

    RangeResult(count, userSubmissions)
  }

  def deserializeUserSubmissionList(json: String): List[UserSubmission] = {
    val jsonValue = parse(json)
    for {
      submission <- (jsonValue \ AssignmentJsonFields.Submissions).children
    } yield extractUserSubmissionFields(submission)
  }

  private def extractAssignmentFields(jValue: JValue): Assignment = {
    val id = (jValue \ AssignmentJsonFields.Id).extractLong
    val title = (jValue \ AssignmentJsonFields.Title).extract[String]
    val body = (jValue \ AssignmentJsonFields.Body).extract[String]
    val deadlineString = jValue \ AssignmentJsonFields.Deadline match {
      case JNothing | JNull => None
      case value: JValue => Option(value.extract[String])
      case _ => throw new IllegalArgumentException(AssignmentJsonFields.Deadline)
    }
    val deadline = deadlineString.map(ISODateTimeFormat.dateTimeNoMillis().parseDateTime)
    val submittedCount = (jValue \ AssignmentJsonFields.SubmittedCount).extract[Int]
    val completedCount = (jValue \ AssignmentJsonFields.CompletedCount).extract[Int]
    val userSubmissions = for {
      userSubmission <- (jValue \ AssignmentJsonFields.Users).children
    } yield extractUserInfoFields(userSubmission)

    val courseResponse = extractCourseFields(jValue \ AssignmentJsonFields.Course)

    Assignment(id,
      title,
      body,
      deadline,
      courseResponse,
      submittedCount,
      completedCount,
      userSubmissions)
  }

  private def extractCourseFields(jValue: JValue): CourseResponse = {
    val id = (jValue \ AssignmentJsonFields.Id).extractLong
    val title = (jValue \ AssignmentJsonFields.Title).extract[String]
    val url = (jValue \ AssignmentJsonFields.Url).extract[String]

    CourseResponse(id, title, url)
  }

  private def extractUserList(userList: JValue): Seq[AssignmentUserInfo] = {
    for(jValue <- userList.children)
      yield extractUserInfoFields(jValue)
  }

  private def extractUserSubmissionFields(jValue: JValue): UserSubmission = {
    val id = (jValue \ AssignmentJsonFields.Id).extractLong
    val date = ISODateTimeFormat.dateTimeNoMillis()
      .parseDateTime((jValue \ AssignmentJsonFields.Date).extract[String])
    val status = jValue \ AssignmentJsonFields.Status match {
      case value: JValue => UserStatuses.withName(value.extract[String])
      case _ => throw new IllegalArgumentException(AssignmentJsonFields.Status)
    }
    val grade = jValue \ AssignmentJsonFields.Grade match {
      case JNothing | JNull => None
      case value: JValue => Option(value.extract[Float])
      case _ => throw new IllegalArgumentException(AssignmentJsonFields.Grade)
    }
    UserSubmission(id, date, status, grade.map(g => Math.floor(g * 100).toInt))
  }

  private def extractUserInfoFields(jValue: JValue): AssignmentUserInfo = {
    val id = (jValue \ AssignmentJsonFields.Id).extractLong
    val name = (jValue \ AssignmentJsonFields.Name).extract[String]
    val email = (jValue \ AssignmentJsonFields.Email).extract[String]
    val picture = (jValue \ AssignmentJsonFields.Picture).extract[String]
    val pageUrl = (jValue \ AssignmentJsonFields.Url).extract[String]
    val organizations = for {
      org <- (jValue \ AssignmentJsonFields.Organizations).children
    } yield (org \ AssignmentJsonFields.Name).extract[String]

    AssignmentUserInfo(
      UserInfo(id, name, email, picture, pageUrl, organizations.toSet),
      extractUserSubmissionFields(jValue)
    )
  }
}