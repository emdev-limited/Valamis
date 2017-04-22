package com.arcusys.valamis.tests.ui.lessonStudio.page

import com.arcusys.valamis.tests.ui.base.page.DeleteDialog
import com.arcusys.valamis.tests.ui.base.{Urls, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.curriculumManager.page.Notification
import org.openqa.selenium.{By, WebElement}

/**
  * Created by vkudryashov on 29.08.16.
  */
class LessonStudioPage(driver: WebDriverArcusys) {

  def open(): Unit = driver.get(Urls.lessonStudioUrl)
  def newLessonButton: WebElement = driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-create-lesson"))
  def dialog: NewLessonDialog = new NewLessonDialog(driver)
  def lessonStudioPage: LessonStudioPage = new LessonStudioPage(driver)

  def createNewLesson(LessonTitle: String, LessonDescription: String, Tag: String): Unit = {

    newLessonButton.click()
    assert(dialog.isExist, "Dialog for add lessons does not exist")
    dialog.setLessonTitle(LessonTitle)
    dialog.setLessonDescription(LessonDescription)
    dialog.choiceTag(Tag)
    dialog.saveButton.click()
    lessonStudioPage.waitForSuccess()
  }

  def deleteLesson(title: String): Unit = {

    val page = lessonStudioPage
    val list = lessonList
    val lesson = list.getItem(title)
    def deleteDialog(): DeleteDialog = new DeleteDialog(driver)

    lesson.deleteButton.click()
    assert(deleteDialog().isExist, "New dialog for delete does not exist")
    deleteDialog().yesButton.click()
    page.waitForSuccess()
  }

  def cloneLesson(title: String): Unit = {
    val page = lessonStudioPage
    val list = lessonList
    val lesson = list.getItem(title)

    lesson.cloneButton.click()
    page.waitForSuccess()
  }

  def lessonList: LessonList = new LessonList(driver)

  def waitForSuccess(): Unit = {
    val result = new Notification(driver)
    driver.waitForElementVisibleBy(result.notificationSuccessLocator)
    assert(result.isExist && result.isSucceed, "not succeed saving")
    result.click()
    driver.waitForElementInvisibleBy(result.notificationSuccessLocator)
  }
}