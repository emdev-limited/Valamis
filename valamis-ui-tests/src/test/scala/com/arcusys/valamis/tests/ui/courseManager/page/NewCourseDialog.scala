package com.arcusys.valamis.tests.ui.courseManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}



/**
  * Created by vkudryashov on 17.08.16.
  */
class NewCourseDialog(driver: WebDriverArcusys) {
  val cssSelector = "#valamisAppModalRegion .val-modal .modal-content"
  val CourseNameSelector = ".js-course-title"
  val dialogLocator = By.cssSelector(cssSelector)
  val CourseNameLocator = By.cssSelector(CourseNameSelector)
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)

  val categoriesList = By.cssSelector(".selectize-input.items")

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def setCouseTitle(CourseTitle: String):Unit = {
    val input = dialog.findElement(By.cssSelector(".js-course-title"))
    input.clear()
    input.sendKeys(CourseTitle)
  }
  def saveButton:WebElement = {
    dialog.findElement(By.cssSelector(".js-submit-button"))
  }

  def setCourseDescription(CourseDescription: String):Unit = {
    val input = dialog.findElement(By.cssSelector(".js-course-description"))
    input.clear()
    input.sendKeys(CourseDescription)
  }
  def closeButton:WebElement = {
    dialog.findElement(By.cssSelector(".modal-close"))
  }
  def uploadButton:WebElement = {
    dialog.findElement(By.cssSelector(".js-upload-image"))
  }
  def selectFromMediaGalleryButton: WebElement = {
    dialog.findElement(By.cssSelector(".js-select-from-media-gallery"))
  }
  def courseLogo: WebElement = {
    dialog.findElement(By.cssSelector(".js-logo.shift-left.val-img-logo.course"))
  }
  def setURL (UrlName: String): Unit = {
    val input = dialog.findElement(By.cssSelector(".js-course-friendly-url"))
    input.clear()
    input.sendKeys(UrlName)
  }

  def choiceCategory (categoryName : String): Unit = {
    val choiceCategories = By.xpath(s".//div[@class='selectize-dropdown-content']/div[.='${categoryName}']")
    dialog.findElement(categoriesList).click()
    dialog.findElement(choiceCategories).click()
  }
}

