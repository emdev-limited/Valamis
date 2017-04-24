package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{WebElement, By}

class Notification(driver: WebDriverArcusys) {
  val notificationLocator = By.cssSelector("#toast-container .toast")
  val notificationSuccessLocator = By.cssSelector("#toast-container .toast.toast-success") // todo is there a way to concat selectors?
  val notificationErrorLocator = By.cssSelector("#toast-container .toast.toast-error")
  val notificationInfoLocator = By.cssSelector("#toast-container .toast.toast-info")
  val confirmationQuestionLocator = By.cssSelector(".confirmation-question")
  lazy val notification = driver.getVisibleElementAfterWaitBy(notificationLocator)

  def isSucceed: Boolean = {
    notification.getAttribute("class").contains("toast-success")
  }

  def isFailed: Boolean = {
    notification.getAttribute("class").contains("toast-error")
  }

  def isConfirmation: Boolean = {
    notification.getAttribute("class").contains("toast-info") && notification.findElement(confirmationQuestionLocator).isDisplayed
  }

  def isExist: Boolean = {
    notification.isEnabled && notification.isDisplayed
  }

  def yesButton: WebElement = {
    val button = notification.findElement(By.cssSelector(".js-confirmation"))
    assert(button.isEnabled && button.isDisplayed, "Cannot find yes button")
    button
  }
  def click(): Unit = {
    notification.findElement(By.cssSelector(".toast-message")).click()
  }

}
