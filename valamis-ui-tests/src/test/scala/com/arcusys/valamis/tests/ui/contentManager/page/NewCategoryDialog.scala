package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.By

/**
 * Created by Igor Borisov on 13.07.15.
 */
class NewCategoryDialog(driver: WebDriverArcusys){
  val cssSelector = "#valamisAppModalRegion .val-modal.content-manager-new-category .modal-content"
  val dialogLocator = By.cssSelector(cssSelector)
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def setCategoryTitle(title: String): Unit = {
    val input = dialog.findElement(By.cssSelector(".js-category-title"))
    input.clear()
    input.sendKeys(title)
  }

  def saveButton = {
    dialog.findElement(By.cssSelector(".js-saveCategory"))
  }
}
