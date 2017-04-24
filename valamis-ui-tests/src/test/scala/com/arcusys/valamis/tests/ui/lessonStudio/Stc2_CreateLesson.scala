package com.arcusys.valamis.tests.ui.lessonStudio

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.lessonStudio.page.LessonStudioPage

/**
  * Created by vkudryashov on 29.08.16.
  */
class Stc2_CreateLesson(val driver: WebDriverArcusys) extends UITestSuite {

  val title = "TestLesson"
  val description = "Description of the test lesson"
  val tag = "system test"


  it should "be able to add new empty lesson" in {

    val page = new LessonStudioPage(driver)
    page.open()
    page.createNewLesson(title, description, tag)

    val list = page.lessonList
    val lesson = list.getItem(title)

    assert(lesson.title == title, "The lesson has wrong title")
    assert(lesson.tag == tag, "The lesson has wrong tag")
    assert(lesson.description == description, "The lesson has wrong description")
    assert(lesson.pageCount == "1 Pages", "The lesson has wrong page count")
    assert(lesson.status == "DRAFT", "The lesson has wrong status")
    assert(lesson.version == "Version 1.0", "The lesson has wrong version")
   }

  it should "be able to delete a new lesson" in {

    val page = new LessonStudioPage(driver)
    val list = page.lessonList

    page.deleteLesson(title)

    assert(!list.existItem(title), "New certificate still exists in the list")
  }
}
