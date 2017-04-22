package com.arcusys.valamis.tests.ui.base.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.By

/**
 * Created by mromanova on 02.11.15.
 */
class DeleteDialog(driver: WebDriverArcusys) {

  val dialogLocator = By.cssSelector("#toast-container")
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def yesButton = {
   val button = dialog.findElement(By.cssSelector(".js-confirmation"))
   assert(button.isEnabled && button.isDisplayed, "Cannot find yes button")
   button
  }
}