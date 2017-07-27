package com.arcusys.valamis.web.servlet.public

import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating
import com.escalatesoft.subcut.inject.NewBindingModule

import scala.collection.mutable

/**
  * Created by pkornilov on 2/21/17.
  */
class LessonServletRatingTest extends LessonServletBaseTest {

  val ratingService = mock[RatingService[Lesson]]
  _bindingModule <~ new NewBindingModule(fn = implicit module => {
    module.bind[RatingService[Lesson]] toSingle ratingService
  })

  def initMocks(): Unit = {
    val ratings = mutable.Map[Long, Double]()
    (ratingService.updateRating _).expects(userId, *, *).onCall {
      (_: Long, score: Double, lessonId: Long) =>
        ratings(lessonId) = score
        Rating(score, score, 1)
    }

    (ratingService.getRating(_: Long, _: Long)).expects(userId, *).onCall {
      (_: Long, lessonId: Long) =>
        ratings.get(lessonId).map { score =>
          Rating(score, score, 1)
        }.getOrElse(Rating(0, 0, 0))
    }

    (ratingService.deleteRating _).expects(userId, *).onCall {
      (_: Long, lessonId: Long) =>
        ratings.remove(lessonId)
        Rating(0, 0, 0)
    }.noMoreThanOnce()
  }

  test("should fail if score is missing") {
    createSampleLesson()
    val url = "/lessons/1/rating"
    put(url) {
      status shouldBe 400
      parse(body) shouldBe parse(
        """
          |{
          | "code":-1,
          | "message":"Key score could not be found."
          |}
        """.stripMargin)
    }
  }

  test("should fail if lesson doesn't exist") {
    val url = "/lessons/1/rating"
    put(url) {
      status shouldBe 404
      parse(body) shouldBe noLessonResponse(1)
    }
  }

  test("should set rating for a lesson") {
    initMocks()
    createSampleLesson()

    val expected = parse(
      """
        |{
        | "score":2.5,
        | "average":2.5,
        | "total":1
        |}
        |
      """.stripMargin)

    val url = "/lessons/1/rating"
    put(s"$url?score=2.5") {
      status shouldBe 200
      parse(body) shouldBe expected
    }
    getWithStatusAssert(url, 200)(parse(body)) shouldBe expected
  }


  test("should remove rating for a lesson") {
    initMocks()
    createSampleLesson()

    val expected = parse(
      """
        |{
        | "score":0.0,
        | "average":0.0,
        | "total":0
        |}
        |
      """.stripMargin)

    val url = "/lessons/1/rating"
    put(s"$url?score=2.5") {
      status shouldBe 200
    }

    delete(url) {
      status shouldBe 200
      parse(body) shouldBe expected
    }

    getWithStatusAssert(url, 200)(parse(body)) shouldBe expected
  }

}
