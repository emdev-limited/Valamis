package com.arcusys.valamis.tests.ui.lessonStudio.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

/**
  * Created by vkudryashov on 11.10.16.
  */
case class LessonStudioListItem (listItem: WebElement, driver: WebDriverArcusys) {

  private val titleLocator = By.cssSelector(".title")
  private val descriptionLocator = By.cssSelector(".description")
  private val actionsButtonLocator = By.cssSelector(".actions")
  private val editInfoButtonLocator = By.cssSelector(".js-lesson-edit")
  private val composeLocator = By.cssSelector(".js-lesson-compose")
  private val deleteLocator = By.cssSelector(".js-lesson-delete")
  private val publishLessonLocator = By.cssSelector(".js-lesson-publish")
  private val exportLocator = By.cssSelector(".js-lesson-export")
  private val cloneLocator = By.cssSelector(".js-lesson-clone")

  def title: String = {
    listItem.findElement(titleLocator).getText
  }

  def tag: String = {
    listItem.findElement(By.cssSelector(".categories:nth-child(2)")).getText
  }

  def description: String = {
    listItem.findElement(descriptionLocator).getText
  }

  def pageCount: String = {
    listItem.findElement(By.cssSelector(".categories:nth-child(4)")).getText
  }

  def status: String = {
    listItem.findElement(By.cssSelector(".status .val-label")).getText
  }

  def version: String = {
    listItem.findElement(By.cssSelector(".status span:nth-child(2)")).getText
  }

  def logoStyle: String = {
    listItem.findElement(By.cssSelector(".image")).getAttribute("style")
  }

  def actionsButton: WebElement = {
    listItem.findElement(actionsButtonLocator)
  }

  def editInfoButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(editInfoButtonLocator)
  }

  def composeButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(composeLocator)
  }

  def deleteButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(deleteLocator)
  }

  def publishButton: WebElement = {
    actionsButton.clear()
    actionsButton.findElement(publishLessonLocator)
  }

  def exportButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(exportLocator)
  }

  def cloneButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(cloneLocator)
  }
}
