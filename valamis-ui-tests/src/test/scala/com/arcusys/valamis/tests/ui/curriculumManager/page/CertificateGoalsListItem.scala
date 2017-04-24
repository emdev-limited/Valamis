package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

case class CertificateGoalsListItem(listItem: WebElement, driver: WebDriverArcusys) {

  private val titleLocator = By.cssSelector("td:nth-child(3)")
  private val deleteButtonLocator = By.cssSelector(".js-goal-delete")

  def title: String = {
    listItem.findElement(titleLocator).getText
  }

  def deleteButton: WebElement = {
    listItem.findElement(deleteButtonLocator)
  }
}
