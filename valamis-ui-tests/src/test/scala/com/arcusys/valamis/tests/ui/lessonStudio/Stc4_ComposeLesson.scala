package com.arcusys.valamis.tests.ui.lessonStudio

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.lessonStudio.page.{ComposePage, Devices, LessonStudioPage}

/**
  * Created by vkudryashov on 30.09.16.
  */
class Stc4_ComposeLesson(val driver: WebDriverArcusys) extends UITestSuite {

  val title = "Lesson for test compose"
  val description = "Description of the test lesson"
  val tag = "system test"
  val page = new LessonStudioPage(driver)

  it should "be able to compose lesson" in {
    page.open()
    page.createNewLesson(title, description, tag)

    val list =  page.lessonList
    val lesson = list.getItem(title)

    lesson.composeButton.click()

    val composePage = new ComposePage(driver)
    assert(composePage.isExist, "lesson compose don't opened")
  }

  it should "be able to select devices" in {
    val devices = new Devices(driver)
    assert(devices.isExist, "device dialog not exist")
    devices.selectDevice("Phone")
    devices.selectDevice("Tablet")
    devices.continue.click()
  }
}
