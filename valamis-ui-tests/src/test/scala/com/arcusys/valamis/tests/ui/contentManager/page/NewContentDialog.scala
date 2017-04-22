package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{WebElement, JavascriptExecutor, By}

/**
 * Created by Igor Borisov on 13.07.15.
 */
class NewContentDialog(driver: WebDriverArcusys){
  val cssSelector = "#valamisAppModalRegion .val-modal .modal-content"
  val titleSelector = ".js-title"
  val dialogLocator = By.cssSelector(cssSelector)
  //val titleLocator = By.cssSelector(s"$cssSelector $titleSelector")
  val titleLocator = By.cssSelector(titleSelector)
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def title: WebElement = dialog.findElement(titleLocator)

  def setContentTitle(titleText: String):Unit = {
    val input = title
    input.clear()
    input.sendKeys(titleText)
  }

  def setContent(content: String):Unit = {
    driver.waitForElementVisibleBy(By.cssSelector("#cke_contentManagerQuestionTextView"))
    driver.asInstanceOf[JavascriptExecutor]
      .executeScript(s"CKEDITOR.instances.contentManagerQuestionTextView.setData('$content');")
  }

  def saveButton:WebElement = {
    dialog.findElement(By.cssSelector(".js-saveContent"))
  }
}
