package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

/**
 * Created by Igor Borisov on 13.07.15.
 */

case class ContentListItem(listItem: WebElement, driver: WebDriverArcusys){

  private val titleLocator = By.cssSelector(".lesson-item-title")
  private val deleteButtonLocator = By.cssSelector(".js-delete-content")
  private val editButtonLocator = By.cssSelector(".js-edit-content")
  private val itemSelectedSelector = ".js-select-entity"

  def title: String = {
   val elem = listItem.findElement(titleLocator)
    elem.getText
  }

  def isCategory: Boolean = listItem.getAttribute("class").contains("category")

  def isQuestion: Boolean = listItem.getAttribute("class").contains("question")

  def isContent: Boolean = listItem.getAttribute("class").contains("plaintext")

  def isSelected: Boolean = {
    val checked = listItem.findElement(By.cssSelector(itemSelectedSelector)).getAttribute("checked")
    checked == "true"
  }

  def editButton: WebElement = listItem.findElement(editButtonLocator)
  def deleteButton: WebElement = listItem.findElement(deleteButtonLocator)

}