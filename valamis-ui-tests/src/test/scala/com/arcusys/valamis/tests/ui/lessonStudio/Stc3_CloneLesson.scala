package com.arcusys.valamis.tests.ui.lessonStudio

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.lessonStudio.page.LessonStudioPage
import org.openqa.selenium.By

/**
  * Created by vkudryashov on 29.09.16.
  */
class Stc3_CloneLesson(val driver: WebDriverArcusys) extends UITestSuite {

  val title = "TestLessonForClone"
  val cloneTitle = title + " copy 1"
  val cloneTitle2 = title + " copy 2"
  val description = "Description of the test lesson"
  val tag = "system test"

  it should "be able to clone lesson" in {

    val page = new LessonStudioPage(driver)

    page.open()
    page.createNewLesson(title, description, tag)

    val list =  page.lessonList
    val lesson = list.getItem(title)

    assert(lesson.title == title, "The lesson has wrong title")
    assert(lesson.tag == tag, "The lesson has wrong tag")
    assert(lesson.description == description, "The lesson has wrong description")
    assert(lesson.pageCount == "1 Pages", "The lesson has wrong page count")
    assert(lesson.status == "DRAFT", "The lesson has wrong status")
    assert(lesson.version == "Version 1.0", "The lesson has wrong version")

    page.cloneLesson(title)

    assert(list.existItem(cloneTitle), "The lesson not cloned")
    val lessonClone = list.getItem(cloneTitle)

    assert(lessonClone.title == cloneTitle, "The lesson has wrong title")
    assert(lessonClone.tag == tag, "The lesson has wrong tag")
    assert(lessonClone.description == description, "The lesson has wrong description")
    assert(lessonClone.pageCount == "1 Pages", "The lesson has wrong page count")
    assert(lessonClone.status == "DRAFT", "The lesson has wrong status")
    assert(lessonClone.version == "Version 1.0", "The lesson has wrong version")

    page.deleteLesson(title)
    assert(!list.existItem(title), "New lesson still exists in the list")
  }

  it should "be able to clone lesson's clone" in {

    val page = new LessonStudioPage(driver)
    val list =  page.lessonList

    page.cloneLesson(cloneTitle)

    assert(list.existItem(cloneTitle2), "The lesson not cloned")
    val lessonClone2 = list.getItem(cloneTitle2)

    assert(lessonClone2.title == cloneTitle2, "The lesson has wrong title")
    assert(lessonClone2.tag == tag, "The lesson has wrong tag")
    assert(lessonClone2.description == description, "The lesson has wrong description")
    assert(lessonClone2.pageCount == "1 Pages", "The lesson has wrong page count")
    assert(lessonClone2.status == "DRAFT", "The lesson has wrong status")
    assert(lessonClone2.version == "Version 1.0", "The lesson has wrong version")

    page.deleteLesson(cloneTitle)
    assert(!list.existItem(cloneTitle), "Lesson clone still exists in the list")
    page.deleteLesson(cloneTitle2)
    assert(!list.existItem(cloneTitle2), "Lesson clone2 still exists in the list")
  }
}
