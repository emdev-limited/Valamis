package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

/**
 * Created by Igor Borisov on 13.07.15.
 */

case class ContentTreeItem(item: WebElement, driver: WebDriverArcusys){

  private val titleLocator = By.cssSelector(".js-tree-item-title")
  private val expandItemLocator = By.cssSelector(".js-tree-item-icon")

  def title: String = {
   val elem = item.findElement(titleLocator)
    elem.getText
  }

  def expand = {
    val icon = item.findElement(expandItemLocator)
    assert(icon.isDisplayed, s"Expand icon is not enabled for category $title")
    assert(icon.isEnabled, s"Expand icon is not enabled for category $title")

    icon.click()
  }

  def activate = item.click()
}