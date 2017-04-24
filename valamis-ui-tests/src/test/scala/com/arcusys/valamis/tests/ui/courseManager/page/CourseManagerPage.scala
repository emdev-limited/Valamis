package com.arcusys.valamis.tests.ui.courseManager.page

import com.arcusys.valamis.tests.ui.base.page.DeleteDialog
import com.arcusys.valamis.tests.ui.base.{Urls, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.curriculumManager.page.Notification
import org.openqa.selenium.{By, WebElement}


/**
  * Created by vkudryashov on 17.08.16.
  */
class CourseManagerPage(driver: WebDriverArcusys) {

  def open(): Unit = driver.open(Urls.CourseManagerUrl)

  def newCourseDialog: NewCourseDialog = new NewCourseDialog(driver)

  def newCourseButton: WebElement = driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-new-course"))

  def createNewCourse(CourseTitle: String, CourseDescription: String, categoryName: String, UrlName: String): Unit = {

    val dialog = newCourseDialog
    newCourseButton.click()
    assert(dialog.isExist, "New course dialog does not exist")
    dialog.setCouseTitle(CourseTitle)
    dialog.setCourseDescription(CourseDescription)
    dialog.setURL(UrlName)
    dialog.choiceCategory(categoryName)
    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)

  }

  def courseManagerPage: CourseManagerPage = new CourseManagerPage(driver)

  def deleteCourse(title: String): Unit = {

    val page = new CourseManagerPage(driver)
    val list = page.courseManagerList
    val course = list.getItem(title)

    def deleteDialog(): DeleteDialog = new DeleteDialog(driver)

    course.deleteButton.click()
    assert(deleteDialog().isExist, "New dialog for delete does not exist")
    deleteDialog().yesButton.click()
    page.waitForSuccess()
  }

  def waitForSuccess(): Unit = {
    val result = new Notification(driver)
    driver.waitForElementVisibleBy(result.notificationSuccessLocator)
    assert(result.isExist && result.isSucceed, "not succeed saving")
    result.click()
    driver.waitForElementInvisibleBy(result.notificationSuccessLocator)
  }

  def courseManagerList: CourseList = new CourseList(driver)
}



