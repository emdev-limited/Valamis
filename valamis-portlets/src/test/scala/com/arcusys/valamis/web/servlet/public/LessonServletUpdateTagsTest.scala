package com.arcusys.valamis.web.servlet.public

import com.arcusys.learn.liferay.LiferayClasses.LAssetEntry
import com.arcusys.valamis.tag.model.ValamisTag

import scala.collection.mutable

/**
  * Created by pkornilov on 2/14/17.
  */
class LessonServletUpdateTagsTest extends LessonServletBaseTest {

  private val _tags = Seq(
    ValamisTag(1, "Tag 1"),
    ValamisTag(2, "Tag 2"),
    ValamisTag(3, "Tag 3")
  )

  val tagByName = _tags groupBy(_.text) map { case (name, tags) =>
    (name, tags.head)
  }

  val tagById = _tags groupBy(_.id) map { case (id, tags) =>
    (id, tags.head)
  }

  def initMocks(): Unit = {
    val lessonTagMap = mutable.Map[Long, Seq[ValamisTag]]()

    (tagServiceMock.getOrCreateTagIds _).expects(*, companyId).onCall { (names: Seq[String], cId: Long) =>
      names map tagByName map (_.id)
    }.noMoreThanTwice()

    (tagServiceMock.setTags _).expects(*, *).onCall { (lessonId: Long, tagIds: Seq[Long]) =>
      lessonTagMap(lessonId) = tagIds map tagById
    }.noMoreThanTwice()

    (tagServiceMock.getByItemId _).expects(*).onCall { id: Long =>
      lessonTagMap.getOrElse(id, Seq())
    }.noMoreThanTwice()

    (assetHelperStub.getEntry _).when(*).onCall { classPK: Long =>
      if (classPK == 1) {
        val entry = mock[LAssetEntry]
        (entry.getPrimaryKey _).expects().returns(classPK)
        Some(entry)
      } else {
        None
      }
    }
  }

  test("should change tags for lessons") {
    initMocks()
    createSampleLesson()

    checkTagUpdate(
      lessonId = 1,
      bodyStr =
        """
          |{
          |  "tags": ["Tag 1", "Tag 3"]
          |}
        """.stripMargin,
      expectedStatus = 204,

      getTagResponse =
        """
          |[
          |  {
          |    "id": 1,
          |    "text": "Tag 1"
          |  },
          |  {
          |    "id": 3,
          |    "text": "Tag 3"
          |  }
          |]
          |
         """.stripMargin)

    checkTagUpdate(
      lessonId = 1,
      bodyStr =
        """
          |{
          |  "tags": []
          |}
        """.stripMargin,
      expectedStatus = 204,

      getTagResponse = "[]"
    )

  }

  test("should fail for empty body") {
    createSampleLesson()

    val url = "/lessons/1/tags"
    put(url, body = "") {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  test("should fail without 'tags' field") {
    createSampleLesson()

    val url = "/lessons/1/tags"
    put(url, body = """{
                  "tagss": ["Tag 1", "Tag 3"]
                }""".stripMargin) {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  test("should fail with bad json") {
    createSampleLesson()

    val url = "/lessons/1/tags"
    put(url, body = """[1, 2, 3]""".stripMargin) {
      statusAssert("PUT", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  test("should fail for non existing lesson") {
    val url = s"/lessons/1/tags"
    put(url,
      body = "{\"tags\": []}") {
      statusAssert("PUT", url, 404)
      parse(body) shouldBe noLessonResponse(1)
    }
  }

  test("should fail for lesson without asset entry") {
    initMocks()
    createSampleLesson()
    createSampleLesson()
    val url = s"/lessons/2/tags"
    put(url,
      body = "{\"tags\": []}") {
      statusAssert("PUT", url, 404)
      parse(body) shouldBe parse(
        """
          |{
          |  "code": -1,
          |  "message": "There is no asset entry for lesson with id: 2"
          |}
          |
        """.stripMargin)
    }
  }

  private def checkTagUpdate(lessonId: Long, bodyStr: String,
                             expectedStatus: Int, getTagResponse: String) {
    val url = s"/lessons/$lessonId/tags"
    put(url,
      body = bodyStr) {
      statusAssert("PUT", url, expectedStatus)
    }
    get(url) {
      statusAssert("GET", url, 200)
      parse(body) shouldEqual parse(getTagResponse)
    }
  }

}
