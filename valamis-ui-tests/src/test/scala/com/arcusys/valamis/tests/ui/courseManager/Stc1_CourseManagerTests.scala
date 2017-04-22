package com.arcusys.valamis.tests.ui.courseManager

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.courseManager.page.{CourseManagerPage, NewCoursePage}


/**
  * Created by vkudryashov on 17.08.16.
  */
class Stc1_CourseManagerTests(val driver: WebDriverArcusys) extends UITestSuite {

  val title = "newAutotestCourse"
  val description = "System test case #1"
  val categoryName = "system test"
  val UrlName = title
  val rating = "4"

  it should "be able to add new course" in {
    val page = new CourseManagerPage(driver)
    page.open()
    page.createNewCourse(title, description, categoryName, UrlName)

    val list = page.courseManagerList
    val course = list.getItem(title)

    course.toSetRating(rating)

    assert(course.title == title, "The course has wrong title")
    assert(course.description == description, "The course has wrong description")
    assert(course.logoStyle == "", "Logo background is not empty")
    assert(course.status == "OPEN", "The course has wrong status")
    assert(course.rating == rating, "The course has wrong rating")
  }

  it should "be able to open new course" in {
    val page = new NewCoursePage(driver)
    page.open(title)
    assert(page.alert.size() == 0, "New course was not opened")
  }
  it should "be able to delete new course" in {
    val page = new CourseManagerPage(driver)
    page.open()
    page.deleteCourse(title)
  }
}
