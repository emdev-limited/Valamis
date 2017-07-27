package com.arcusys.valamis.web.servlet.public

import com.arcusys.valamis.lesson.model.{Lesson, LessonLimit, LessonType}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

/**
  * Created by pkornilov on 2/3/17.
  */
class LessonServletUpdateTest extends LessonServletBaseTest {


  test("update lesson with limit adding") {
    createLesson(courseId)(LessonTestData(
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
      limit = None,
      members = Seq()
    ))

    testUpdate("""
                 |  "title": "New lesson title",
                 |  "description": "New lesson description",
                 |  "visibility": "hidden",
                 |  "beginDate": "2017-03-17T00:00:00Z",
                 |  "endDate": "2017-04-16T00:00:00Z",
                 |  "scoreLimit": 0.74,
                 |  "requiredReview": true,
                 |  "rerunLimits": {
                 |    "maxAttempts": 2,
                 |    "period": "P2D"
                 |  }
               """.stripMargin)


  }

  test("update lesson with limit removing") {

    createLesson(courseId)(LessonTestData(
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
      limit = Some(LessonLimit(1, Some(3), Some(4), PeriodTypes.DAYS)),
      members = Seq()
    ))

    testUpdate("""
                 |  "title": "New lesson title",
                 |  "description": "New lesson description",
                 |  "visibility": "public",
                 |  "beginDate": "2017-03-17T00:00:00Z",
                 |  "endDate": "2017-04-16T00:00:00Z",
                 |  "scoreLimit": 0.74,
                 |  "requiredReview": true
               """.stripMargin)
  }

  test("update lesson with limit changing") {

    createLesson(courseId)(LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Tincan,
        title = "Lesson 1",
        description = "Lesson 1 desc",
        logo = Some("logo.png"),
        courseId = -1,
        isVisible = Some(true), //public visibility
        beginDate = Some(new DateTime("2017-01-17T00:00:00Z")),
        endDate = Some(new DateTime("2017-02-16T00:00:00Z")),
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = false,
        scoreLimit = 0.7
      ),
      limit = Some(LessonLimit(1, None, Some(4), PeriodTypes.DAYS)),
      members = Seq()
    ))

    testUpdate("""
                 |  "title": "New lesson title",
                 |  "description": "New lesson description",
                 |  "visibility": "hidden",
                 |  "beginDate": "2017-03-17T00:00:00Z",
                 |  "endDate": "2017-04-16T00:00:00Z",
                 |  "scoreLimit": 0.74,
                 |  "requiredReview": true,
                 |  "rerunLimits": {
                 |    "maxAttempts": 3
                 |  }
               """.stripMargin)
  }

  test("update lesson with limit changing 2") {

    createLesson(courseId)(LessonTestData(
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
      limit = Some(LessonLimit(1, Some(3), None, PeriodTypes.UNLIMITED)),
      members = Seq()
    ))

    testUpdate("""
                 |  "title": "New lesson title",
                 |  "description": "New lesson description",
                 |  "visibility": "hidden",
                 |  "beginDate": "2017-03-17T00:00:00Z",
                 |  "endDate": "2017-04-16T00:00:00Z",
                 |  "scoreLimit": 0.74,
                 |  "requiredReview": true,
                 |  "rerunLimits": {
                 |    "period": "P3M"
                 |  }
               """.stripMargin)
  }

  test("fail for non existed lesson") {
    val url ="/lessons/1"
    put(url, body = """{
                              |  "title": "New lesson title",
                              |  "description": "New lesson description",
                              |  "visibility": "hidden",
                              |  "beginDate": "2017-03-17T00:00:00Z",
                              |  "endDate": "2017-04-16T00:00:00Z",
                              |  "scoreLimit": 0.74,
                              |  "requiredReview": true,
                              |  "rerunLimits": {
                              |    "period": "P3M"
                              |  }
                              |}
                            """.stripMargin) {
      statusAssert("PUT", url, 404)
    }

  }

  test("fail for bad period value") {
    createLesson(courseId)(LessonTestData(
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
      limit = None,
      members = Seq()
    ))
    val url ="/lessons/1"
    put(url, body = """{
                      |  "title": "New lesson title",
                      |  "description": "New lesson description",
                      |  "visibility": "hidden",
                      |  "beginDate": "2017-03-17T00:00:00Z",
                      |  "endDate": "2017-04-16T00:00:00Z",
                      |  "scoreLimit": 0.74,
                      |  "requiredReview": true,
                      |  "rerunLimits": {
                      |    "period": "PT3H"
                      |  }
                      |}
                    """.stripMargin) {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Unsupported period type: hours"
          | }
        """.stripMargin)
    }
  }

  test("fail for bad json") {
    createLesson(courseId)(LessonTestData(
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
      limit = None,
      members = Seq()
    ))
    val url ="/lessons/1"
    put(url, body = """{
                      |  "title": "New lesson title",
                      |  "description": "New lesson description",
                      |  "visibility": "hidden",
                      |  "beginDate": "2017-03-17T00:00:00Z",
                      |  "endDate": "2017-04-16T00:00:00Z",
                      |  "scoreLimit": 0.74,
                      |  "requiredReview": true
                      |  "rerunLimits": {
                      |    "period": "PT3H"
                      |  }
                      |}
                    """.stripMargin) {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  test("fail for invalid period format") {
    createLesson(courseId)(LessonTestData(
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
      limit = None,
      members = Seq()
    ))
    val url ="/lessons/1"
    put(url, body = """{
                      |  "title": "New lesson title",
                      |  "description": "New lesson description",
                      |  "visibility": "hidden",
                      |  "beginDate": "2017-03-17T00:00:00Z",
                      |  "endDate": "2017-04-16T00:00:00Z",
                      |  "scoreLimit": 0.74,
                      |  "requiredReview": true,
                      |  "rerunLimits": {
                      |    "period": "bad"
                      |  }
                      |}
                    """.stripMargin) {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  private def testUpdate(updateData: String): Unit = {
    val url = "/lessons/1"

    val constantPart =
      s"""
         | "id": 1,
         | "lessonType": "tincan",
         | "courseId": $courseId,
         | "link": "${lessonLink(1)}",
      """.stripMargin

    val expected = parse("{" + constantPart + updateData + "}")

    put(url, body = "{" + updateData + "}") {
      statusAssert("PUT", url, 200)
      parse(body).withoutCreationDate shouldBe expected
    }

    getWithStatusAssert(url, 200) {
      parse(body).withoutCreationDate shouldBe expected
    }
  }

}
