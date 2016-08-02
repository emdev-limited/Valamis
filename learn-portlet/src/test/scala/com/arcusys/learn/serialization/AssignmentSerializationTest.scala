package com.arcusys.learn.serialization

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.serializer.AssignmentSerializer
import com.arcusys.valamis.model.RangeResult
import org.joda.time.DateTime
import org.scalatest.FunSpec

class AssignmentSerializationTest extends FunSpec with AssignmentSerializer {
  describe("AssignmentSerializer"){
    it("should convert assignment JSON to Assignment"){

      val count = 2
      val assignment1 = Assignment(1,
        "name1",
        "body1",
        Some(new DateTime("2016-06-09T03:00:00Z")),
        CourseResponse(111, "group1", ""),
        1,
        0,
        Seq())

      val assignment2 = Assignment(2,
        "name2",
        "body2",
        None,
        CourseResponse(222, "group2", ""),
        2,
        1,
        Seq(
          AssignmentUserInfo(
            UserInfo(11, "user1", ""),
            UserSubmission(11, new DateTime("2016-06-02T00:00:00Z"), UserStatuses.WaitingForSubmission, None)),
          AssignmentUserInfo(
            UserInfo(22, "user2", "", organizations = Set("org1", "org2")),
            UserSubmission(22, new DateTime("2016-05-25T00:00:00Z"), UserStatuses.WaitingForEvaluation, None)),
          AssignmentUserInfo(
            UserInfo(33, "user3", ""),
            UserSubmission(33, new DateTime("2016-05-30T00:00:00Z"), UserStatuses.Completed, Some(40)))))

      val assignments = RangeResult(count, Seq(assignment1, assignment2))

      val organizationsString =
        """[{"name":"org1"}, {"name":"org2"}]"""
      val userString =
        s"""[{"id":11, "name":"user1", "email":"", "picture":"", "url":"", "organizations": [],
          | "date":"2016-06-02T00:00:00Z", "status":"WaitingForSubmission"},
          | {"id":22, "name":"user2", "email":"", "picture":"", "url":"", "organizations": $organizationsString,
          | "date":"2016-05-25T00:00:00Z", "status":"WaitingForEvaluation"},
          | {"id":33, "name":"user3", "email":"", "picture":"", "url":"", "organizations": [],
          | "date":"2016-05-30T00:00:00Z", "status":"Completed", "grade":0.4}]""".stripMargin
      val assignment1String =
        s"""{"id":1, "title":"name1", "body":"body1", "deadline":"2016-06-09T03:00:00Z",
          | "course":{"id":111, "title":"group1", "url":""},
          | "submittedCount":1, "completedCount":0, "users":"[]"}""".stripMargin
      val assignment2String =
        s"""{"id":2, "title":"name2", "body":"body2", "course":{"id":222, "title":"group2", "url":""},
           | "submittedCount":2, "completedCount":1, "users": $userString}""".stripMargin
      val assignmentsString = s"""{"assignments": [$assignment1String, $assignment2String], "count": $count}""".stripMargin

      assert(deserializeAssignment(assignment1String) == assignment1)
      assert(deserializeAssignment(assignment2String) == assignment2)
      assert(deserializeAssignmentList(assignmentsString) == assignments)
    }
  }
}