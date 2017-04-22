package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{WebElement, By}

case class CertificateListItem(listItem: WebElement, driver: WebDriverArcusys) {

  private val titleLocator = By.cssSelector(".title")
  private val descriptionLocator = By.cssSelector(".description")
  private val actionsButtonLocator = By.cssSelector(".actions")
  private val editButtonLocator = By.cssSelector(".js-edit-certificate")
  private val deleteButtonLocator = By.cssSelector(".js-delete-certificate")
  private val activateButtonLocation = By.cssSelector(".js-activate-certificate")

  def title: String = {
    listItem.findElement(titleLocator).getText
  }

  def description: String = {
    listItem.findElement(descriptionLocator).getText
  }

  def usersAndGoals: String = {
    listItem.findElement(By.cssSelector(".categories")).getText
  }

  def logoStyle: String = {
    listItem.findElement(By.cssSelector(".image")).getAttribute("style")
  }

  def actionsButton: WebElement = {
    listItem.findElement(actionsButtonLocator)
  }

  def editButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(editButtonLocator)
  }

  def deleteButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(deleteButtonLocator)
  }

  def isUnpublished: Boolean = {
    listItem.getAttribute("class").contains("unpublished")
  }
  def activateButton: WebElement = {
    actionsButton.click()
    actionsButton.findElement(activateButtonLocation)
  }

}
