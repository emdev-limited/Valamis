package com.arcusys.valamis.tests.ui.courseManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

/**
  * Created by vkudryashov on 10.10.16.
  */
case class CourseManagerListItem(listItem: WebElement, driver: WebDriverArcusys) {

  private val titleLocator = By.cssSelector(".title")
  private val descriptionLocator = By.cssSelector(".description")
  private val actionsButtonLocator = By.cssSelector(".actions")
  private val editInfoButtonLocator = By.cssSelector(".js-course-info-edit")
  private val editMembersButtonLocator = By.cssSelector(".js-course-members-edit")
  private val deleteButtonLocator = By.cssSelector(".js-course-delete")

  def title: String = {
    listItem.findElement(titleLocator).getText
  }

  def description: String = {
    listItem.findElement(descriptionLocator).getText
  }

  def status: String = {
    listItem.findElement(By.cssSelector(".course-type")).getText
  }

  def tag: String = {
    listItem.findElement(By.cssSelector(".course-tags")).getText
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

  def editMembersButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(editMembersButtonLocator)
  }

  def deleteButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(deleteButtonLocator)
  }

  def toSetRating(rating: String): Unit = {
    listItem.findElement(By.cssSelector(s"[data-value='$rating']")).click()
  }

  def rating: String = {
    val test = listItem.findElement(By.cssSelector(".js-rating-current")).getText
    test
  }
}
