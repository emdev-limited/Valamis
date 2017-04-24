package com.arcusys.valamis.tests.ui.lessonStudio.page

import org.openqa.selenium.{By, Keys, WebElement}
import org.openqa.selenium.interactions.Action
import com.arcusys.valamis.tests.ui.base.WebDriverArcusys

/**
  * Created by vkudryashov on 30.08.16.
  */
class NewLessonDialog(driver: WebDriverArcusys)  {
  val cssSelector = "#valamisAppModalRegion.portlet-body"
  val dialogLocator = By.cssSelector(cssSelector)
  lazy val TagList = By.cssSelector(".selectize-input.items")
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)

  def isExist: Boolean = dialog.isEnabled && dialog.isDisplayed

  def setLessonTitle(LessonTitle: String):Unit = {
    val input = dialog.findElement(By.cssSelector(".js-lesson-title"))
    input.clear()
    input.sendKeys(LessonTitle)
  }
  def setLessonDescription(LessonDescription: String):Unit = {
    val input = dialog.findElement(By.cssSelector(".js-lesson-description"))
    input.clear()
    input.sendKeys(LessonDescription)
  }
  def choiceTag(Tag: String): Unit = {
    lazy val choiceTag = By.xpath(s".//div[@class='selectize-dropdown-content']/div[.='${Tag}']")
    dialog.findElement(TagList).click()
    dialog.findElement(choiceTag).click()
   }
  def saveButton: WebElement = {
  dialog.findElement(By.cssSelector(".js-save-lesson"))
  }
}
