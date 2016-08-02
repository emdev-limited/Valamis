package com.arcusys.valamis.certificate.model

import org.joda.time.DateTime

case class Assignment(id: Long,
                      title: String,
                      body: String,
                      deadline: Option[DateTime],
                      course: CourseResponse,
                      submittedCount: Int,
                      completedCount: Int,
                      users: Seq[AssignmentUserInfo])

case class UserInfo(id: Long,
                    name: String,
                    email: String,
                    picture: String = "",
                    pageUrl: String = "",
                    organizations: Set[String] = Set())

case class AssignmentUserInfo(userInfo: UserInfo,
                              submission: UserSubmission)

case class UserSubmission(userId: Long,
                          date: DateTime,
                          status: UserStatuses.Value,
                          grade: Option[Int])


case class Submission(id: Long,
                      assignmentId: Long,
                      body: String)

case class CourseResponse(id: Long,
                          title: String,
                          url: String)

object AssignmentStatuses extends Enumeration {
  val Draft = Value
  val Published = Value
}

object UserStatuses extends Enumeration {
  val WaitingForSubmission = Value
  val WaitingForEvaluation = Value
  val Completed = Value
}

object AssignmentJsonFields {
  val Assignments = "assignments"
  val Body = "body"
  val CompletedCount = "completedCount"
  val Count = "count"
  val Course = "course"
  val Date = "date"
  val Deadline = "deadline"
  val Email = "email"
  val Grade = "grade"
  val GroupId = "groupId"
  val Id = "id"
  val Name = "name"
  val Organizations = "organizations"
  val Picture = "picture"
  val Status = "status"
  val SubmittedCount = "submittedCount"
  val Title = "title"
  val Url = "url"
  val UserIds = "userIds"
  val Users = "users"
}

object AssignmentMessageFields {
  val Action = "action"
  val AssignmentId = "assignmentId"
  val GroupId = "groupId"
  val Order = "order"
  val Skip = "skip"
  val SortBy = "sortBy"
  val Status = "status"
  val Take = "take"
  val TitlePattern = "titlePattern"
  val UserId = "userId"
}

object AssignmentMessageActionType extends Enumeration {
  val AssignmentUsers = Value
  val ById = Value
  val Check = Value
  val EvaluationDate = Value
  val List = Value
  val SubmissionStatus = Value
  val UserAssignments = Value
}