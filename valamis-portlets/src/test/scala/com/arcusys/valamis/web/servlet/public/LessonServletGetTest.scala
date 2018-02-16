package com.arcusys.valamis.web.servlet.public

import com.arcusys.valamis.lesson.model.{Lesson, LessonLimit, LessonType}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import org.json4s.JValue

/**
  * Created by pkornilov on 2/3/17.
  */
class LessonServletGetTest extends LessonServletBaseTest {

  test("get lessons") {
    lessons foreach createLesson(courseId)
    lessons foreach createLesson(otherCourseId)

    val tincanLessons = parse(s"""
                     |[
                     |  {
                     |    "id": 1,
                     |    "lessonType": "tincan",
                     |    "title": "Lesson 1",
                     |    "description": "Lesson 1 desc",
                     |    "courseId": 20147,
                     |    "visibility": "custom",
                     |    "beginDate": "2017-01-17T00:00:00Z",
                     |    "endDate": "2017-02-16T00:00:00Z",
                     |    "requiredReview": false,
                     |    "scoreLimit": 0.7,
                     |    "rerunLimits": {
                     |      "maxAttempts": 5
                     |    },
                     |    "link": "${lessonLink(1)}"
                     |  },
                     |  {
                     |    "id": 3,
                     |    "lessonType": "tincan",
                     |    "title": "Lesson 2",
                     |    "description": "Lesson 2 desc",
                     |    "courseId": 20147,
                     |    "visibility": "hidden",
                     |    "requiredReview": true,
                     |    "scoreLimit": 0.8,
                     |    "rerunLimits": {
                     |      "period": "P2D"
                     |    },
                     |    "link": "${lessonLink(3)}"
                     |  },
                     |  {
                     |    "id": 2,
                     |    "lessonType": "tincan",
                     |    "title": "Lesson 3",
                     |    "description": "Lesson 3 desc",
                     |    "courseId": 20147,
                     |    "visibility": "public",
                     |    "requiredReview": false,
                     |    "scoreLimit": 0.7,
                     |    "rerunLimits": {
                     |      "maxAttempts": 3,
                     |      "period": "P4M"
                     |    },
                     |    "link": "${lessonLink(2)}"
                     |  }
                     |]
                     |
         """.stripMargin)

    val scormLessons = parse(s"""
                     |[
                     |  {
                     |    "id": 4,
                     |    "lessonType": "scorm",
                     |    "title": "Lesson 4",
                     |    "description": "Lesson 4 desc",
                     |    "courseId": 20147,
                     |    "visibility": "public",
                     |    "requiredReview": false,
                     |    "scoreLimit": 0.7,
                     |    "link": "${lessonLink(4)}"
                     |  }
                     |]
         """.stripMargin)

    val allLessons = tincanLessons ++ scormLessons

    //get without course id returns 400
    getWithStatusAssert("/lessons/", 400) {}

    //get with wrong lesson type returns 400
    getWithStatusAssert(s"/lessons?courseId=$courseId&lessonType=wrong", 400) {
      parse(body) shouldBe parse(
        """{
          | "code": -1,
          | "message": "Wrong lesson type: wrong"
          |}
          |""".stripMargin
      )
    }

    //get all lessons by courseId
    getWithStatusAssert(s"/lessons?courseId=$courseId", 200) {
      parse(body).withoutCreationDate shouldEqual allLessons
    }

    //get all tincan lessons by courseId
    getWithStatusAssert(s"/lessons?courseId=$courseId&lessonType=tincan", 200) {
      parse(body).withoutCreationDate shouldEqual tincanLessons
    }

    //get all scorm lessons by courseId
    getWithStatusAssert(s"/lessons?courseId=$courseId&lessonType=scorm", 200) {
      parse(body).withoutCreationDate shouldEqual scormLessons
    }

    //get tincan lessons by courseId with skipTake
    lessons.map(d => d.copy(lesson = d.lesson.copy(title = d.lesson.title + " new"))) foreach createLesson(courseId)
    getWithStatusAssert(s"/lessons?courseId=$courseId&lessonType=tincan&start=3&size=4", 200) {
      parse(body).extract[List[JValue]] map { v =>
        val id = (v \ "id").extract[Long]
        val title = (v \ "title").extract[String]
        val tpe = (v \ "lessonType").extract[String]
        s"$title ($id, $tpe)"
      } shouldEqual List(
        "Lesson 2 (3, tincan)",
        "Lesson 2 new (11, tincan)",
        "Lesson 3 (2, tincan)",
        "Lesson 3 new (10, tincan)"
      )
    }

  }

  test("get lesson by id") {
    lessons foreach createLesson(courseId)
    lessons foreach createLesson(otherCourseId)
    lessons foreach createLesson(courseId)

    val lesson9 = parse(s"""{
                   |    "id": 9,
                   |    "lessonType": "tincan",
                   |    "title": "Lesson 1",
                   |    "description": "Lesson 1 desc",
                   |    "courseId": 20147,
                   |    "visibility": "custom",
                   |    "beginDate": "2017-01-17T00:00:00Z",
                   |    "endDate": "2017-02-16T00:00:00Z",
                   |    "requiredReview": false,
                   |    "scoreLimit": 0.7,
                   |    "rerunLimits": {
                   |      "maxAttempts": 5
                   |    },
                   |    "link": "${lessonLink(9)}"
                   |  }""".stripMargin)

    val lesson3 = parse(s"""{
                     |    "id": 2,
                     |    "lessonType": "tincan",
                     |    "title": "Lesson 3",
                     |    "description": "Lesson 3 desc",
                     |    "courseId": 20147,
                     |    "visibility": "public",
                     |    "requiredReview": false,
                     |    "scoreLimit": 0.7,
                     |    "rerunLimits": {
                     |      "maxAttempts": 3,
                     |      "period": "P4M"
                     |    },
                     |    "link": "${lessonLink(2)}"
                     |  }""".stripMargin)

    //get lesson by id
    getWithStatusAssert("/lessons/2", 200) {//Lesson 3 has id = 2
      parse(body).withoutCreationDate shouldBe lesson3
    }

    getWithStatusAssert("/lessons/9", 200) {
      parse(body).withoutCreationDate shouldBe lesson9
    }

    getWithStatusAssert("/lessons/15", 404) {
      parse(body) shouldBe noLessonResponse(15)
    }

  }

  private val lessons = Seq(
    LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Tincan,
        title = "Lesson 1",
        description = "Lesson 1 desc",
        logo = Some("logo.png"),
        courseId = -1,
        isVisible = None, //custom visibility
        beginDate = Some(new DateTime("2017-01-17T00:00:00Z")),
        endDate = Some(new DateTime("2017-02-16T00:00:00Z")),
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = false,
        scoreLimit = 0.7
      ),
      limit = Some(
        LessonLimit(
          lessonId = -1,
          passingLimit = Some(5),
          rerunInterval = None,
          rerunIntervalType = PeriodTypes.UNLIMITED
        )
      ),
      members = Seq()
    ),
    LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Tincan,
        title = "Lesson 3",
        description = "Lesson 3 desc",
        logo = None,
        courseId = -1,
        isVisible = Some(true), //public visibility
        beginDate = None,
        endDate = None,
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = false,
        scoreLimit = 0.7
      ),
      limit = Some(
        LessonLimit(
          lessonId = -1,
          passingLimit = Some(3),
          rerunInterval = Some(4),
          rerunIntervalType = PeriodTypes.MONTH
        )
      ),
      members = Seq()
    ),
    LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Tincan,
        title = "Lesson 2",
        description = "Lesson 2 desc",
        logo = None,
        courseId = -1,
        isVisible = Some(false), //hidden
        beginDate = None,
        endDate = None,
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = true,
        scoreLimit = 0.8
      ),
      limit = Some(
        LessonLimit(
          lessonId = -1,
          passingLimit = None,
          rerunInterval = Some(2),
          rerunIntervalType = PeriodTypes.DAYS
        )
      ),
      members = Seq()
    ),
    LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Scorm,
        title = "Lesson 4",
        description = "Lesson 4 desc",
        logo = None,
        courseId = -1,
        isVisible = Some(true), //public visibility
        beginDate = None,
        endDate = None,
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = false,
        scoreLimit = 0.7
      ),
      limit = None,
      members = Seq()
    )
  )

}
