package com.arcusys.valamis.slide.service.export

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.slide.model.{Slide, SlideElement, SlideEntityType, SlideSet}
import org.scalatest.{FunSuite, Matchers}

import scala.xml._

/**
  * Created by mminin on 21/10/2016.
  */

class TincanManifestBuilderTest extends FunSuite with Matchers {

  val courseId = 2344
  val noneCategoryId = None

  val slideSet = SlideSet(
    id = 233,
    title = "my lesson",
    courseId = 123,
    activityId = "http://test.org/course/142"
  )
  val someElement = SlideElement(
    id = 147,
    slideId = 233,
    slideEntityType = SlideEntityType.Question,
    content = "some content"
  )

  val someSlide = Slide(id = 1, title = "some slide", slideSetId = slideSet.id)

  test("non question slides") {
    val slideEmpty = someSlide.copy(id = 45, title = "slide 3")
    val slideWithText = someSlide.copy(id = 46, title = "slide 4", topSlideId = Some(slideEmpty.id),
      slideElements = Seq(SlideElement(
        id = 147,
        slideId = 46,
        slideEntityType = SlideEntityType.Text,
        content = "some content"
      )))

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slideEmpty, slideWithText), questions = Nil)
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity id="http://test.org/course/142"
                    type="course">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity id="http://test.org/course/142/slide_45"
                    type="module">
            <name>slide 3</name>
            <description lang="en-US">slide 3</description>
          </activity>
          <activity id="http://test.org/course/142/slide_46"
                    type="module">
            <name>slide 4</name>
            <description lang="en-US">slide 4</description>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml should equal(expectedXml)
  }

  test("choice slide with single correct answer") {

    val question = ChoiceQuestion(Some(23),
      noneCategoryId,
      title = "choiceQuestion_23",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      forceCorrectCount = false,
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerText(Some(1), question.id, courseId, body = "opt.1", isCorrect = false, 1),
      AnswerText(Some(2), question.id, courseId, body = "opt.2", isCorrect = true, 2),
      AnswerText(Some(3), question.id, courseId, body = "opt.3", isCorrect = false, 3)
    )

    val slide = someSlide.copy(id = 47,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task23_345">
            <name>choiceQuestion_23</name>
            <description lang="en-US">choiceQuestion_23</description>
            <interactionType>choice</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>opt.2</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("choice slide with many correct answers") {

    val question = ChoiceQuestion(Some(23),
      noneCategoryId,
      title = "choiceQuestion_23",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      forceCorrectCount = false,
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerText(Some(1), question.id, courseId, body = "opt.1", isCorrect = true, 1),
      AnswerText(Some(2), question.id, courseId, body = "opt.2", isCorrect = false, 2),
      AnswerText(Some(3), question.id, courseId, body = "opt.3", isCorrect = true, 3)
    )

    val slide = someSlide.copy(id = 47,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task23_345">
            <name>choiceQuestion_23</name>
            <description lang="en-US">choiceQuestion_23</description>
            <interactionType>choice</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>opt.1</correctResponsePattern>
              <correctResponsePattern>opt.3</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("numeric question slide") {

    val question = NumericQuestion(Some(24),
      noneCategoryId,
      title = "choiceQuestion_23",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerRange(Some(1), question.id, courseId, rangeFrom = 5, rangeTo = 10)
    )

    val slide = someSlide.copy(id = 48,
      slideElements = Seq(someElement.copy(id = 342, content = question.id.get.toString))
    )


    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task24_342">
            <name>choiceQuestion_23</name>
            <description lang="en-US">choiceQuestion_23</description>
            <interactionType>numeric</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>5.0[:]10.0</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("numeric question slide with many answers") {

    val question = NumericQuestion(Some(24),
      noneCategoryId,
      title = "choiceQuestion_23",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerRange(Some(1), question.id, courseId, rangeFrom = 5, rangeTo = 10),
      AnswerRange(Some(1), question.id, courseId, rangeFrom = 15, rangeTo = 20)
    )

    val slide = someSlide.copy(id = 48,
      slideElements = Seq(someElement.copy(id = 341, content = question.id.get.toString))
    )


    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task24_341">
            <name>choiceQuestion_23</name>
            <description lang="en-US">choiceQuestion_23</description>
            <interactionType>numeric</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>5.0[:]10.0</correctResponsePattern>
            </correctResponsePatterns>
            <correctResponsePatterns>
              <correctResponsePattern>15.0[:]20.0</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml should equal(expectedXml)
  }

  test("text question slide") {

    val question = TextQuestion(Some(25),
      noneCategoryId,
      title = "choiceQuestion_23",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      isCaseSensitive = true,
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerText(Some(1), question.id, courseId, body = "opt.1", isCorrect = true, 1),
      AnswerText(Some(2), question.id, courseId, body = "opt.2", isCorrect = false, 2),
      AnswerText(Some(3), question.id, courseId, body = "opt.3", isCorrect = true, 3)
    )

    val slide = someSlide.copy(id = 488,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )


    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task25_345">
            <name>choiceQuestion_23</name>
            <description lang="en-US">choiceQuestion_23</description>
            <interactionType>fill-in</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>opt.1</correctResponsePattern>
              <correctResponsePattern>opt.3</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("matching question slide") {

    val question = MatchingQuestion(Some(25),
      noneCategoryId,
      title = "matching question 25",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerKeyValue(Some(1), question.id, courseId, key = "a", value = Some("1")),
      AnswerKeyValue(Some(2), question.id, courseId, key = "b", value = Some("2")),
      AnswerKeyValue(Some(3), question.id, courseId, key = "c", value = Some("3"))
    )

    val slide = someSlide.copy(id = 488,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task25_345">
            <name>matching question 25</name>
            <description lang="en-US">matching question 25</description>
            <interactionType>matching</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>a[.]1</correctResponsePattern>
              <correctResponsePattern>b[.]2</correctResponsePattern>
              <correctResponsePattern>c[.]3</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("sequencing question slide") {

    val question = PositioningQuestion(Some(25),
      noneCategoryId,
      title = "PositioningQuestion(Some(25))",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      forceCorrectCount = false,
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerText(Some(1), question.id, courseId, body = "opt.A", isCorrect = true, 1),
      AnswerText(Some(2), question.id, courseId, body = "opt.C", isCorrect = false, 3),
      AnswerText(Some(3), question.id, courseId, body = "opt.B", isCorrect = true, 2)
    )

    val slide = someSlide.copy(id = 12,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task25_345">
            <name>PositioningQuestion(Some(25))</name>
            <description lang="en-US">PositioningQuestion(Some(25))</description>
            <interactionType>sequencing</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>opt.A</correctResponsePattern>
              <correctResponsePattern>opt.B</correctResponsePattern>
              <correctResponsePattern>opt.C</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("categorization question slide") {
    val question = CategorizationQuestion(Some(25),
      noneCategoryId,
      title = "CategorizationQuestion",
      text = "Some text",
      explanationText = "",
      rightAnswerText = "",
      wrongAnswerText = "",
      courseId
    ).asInstanceOf[Question]

    val answers = Seq(
      AnswerKeyValue(Some(1), question.id, courseId, key = "a", value = Some("a1")),
      AnswerKeyValue(Some(2), question.id, courseId, key = "a", value = Some("a2")),
      AnswerKeyValue(Some(3), question.id, courseId, key = "b", value = Some("b1"))
    )

    val slide = someSlide.copy(id = 12,
      slideElements = Seq(someElement.copy(id = 345, content = question.id.get.toString))
    )

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, answers)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task25_345">
            <name>CategorizationQuestion</name>
            <description lang="en-US">CategorizationQuestion</description>
            <interactionType>matching</interactionType>
            <correctResponsePatterns>
              <correctResponsePattern>a[.]a1</correctResponsePattern>
              <correctResponsePattern>a[.]a2</correctResponsePattern>
              <correctResponsePattern>b[.]b1</correctResponsePattern>
            </correctResponsePatterns>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("essay question slide") {
    val question = EssayQuestion(Some(234),
      noneCategoryId,
      title = "EssayQuestion",
      text = "Some text",
      explanationText = "",
      courseId
    ).asInstanceOf[Question]

    val slide = someSlide.copy(id = 12,
      slideElements = Seq(someElement.copy(id = 34, content = question.id.get.toString))
    )


    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide), Seq((question, Nil)))
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course"
                    id="http://test.org/course/142">
            <name>my lesson</name>
            <description lang="en-US"></description>
            <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="cmi.interaction"
                    id="http://test.org/course/142/task234_34">
            <name>EssayQuestion</name>
            <description lang="en-US">EssayQuestion</description>
            <interactionType>other</interactionType>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }

  test("slide ordering") {
    val slide1 = someSlide.copy(id = 45, title = "slide 1")
    val slide11 = someSlide.copy(id = 145, title = "under 1", topSlideId = Some(slide1.id))
    val slide12 = someSlide.copy(id = 35, title = "under 11", topSlideId = Some(slide11.id))
    val slide2 = someSlide.copy(id = 46, title = "slide 2", leftSlideId = Some(slide1.id))
    val slide21 = someSlide.copy(id = 25, title = "under 2", topSlideId = Some(slide2.id))
    val slide3 = someSlide.copy(id = 48, title = "slide 3", leftSlideId = Some(slide2.id))

    val manifestXml = Utility.trim(XML.load(Source.fromString(
      TincanManifestBuilder.build(slideSet, Seq(slide1, slide2, slide3, slide11, slide12, slide21), Nil)
    )))

    val expectedXml = Utility.trim(
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          <activity type="course" id="http://test.org/course/142">
            <name>my lesson</name> <description lang="en-US"/> <launch lang="en-US">data/index.html</launch>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_45">
            <name>slide 1</name> <description lang="en-US">slide 1</description>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_145">
            <name>under 1</name> <description lang="en-US">under 1</description>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_35">
            <name>under 11</name> <description lang="en-US">under 11</description>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_46">
            <name>slide 2</name> <description lang="en-US">slide 2</description>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_25">
            <name>under 2</name> <description lang="en-US">under 2</description>
          </activity>
          <activity type="module" id="http://test.org/course/142/slide_48">
            <name>slide 3</name> <description lang="en-US">slide 3</description>
          </activity>
        </activities>
      </tincan>
    )

    manifestXml.toString should equal(expectedXml.toString)
  }
}
