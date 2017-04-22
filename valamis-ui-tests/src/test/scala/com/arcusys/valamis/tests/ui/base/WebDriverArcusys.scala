package com.arcusys.valamis.tests.ui.base

import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebDriver}
import com.codeborne.selenide.Selenide._

/**
  * Created by Igor Borisov on 13.07.15.
  */
trait WebDriverArcusys extends WebDriver {
  private val waitForEvent = 10
  private val intervalBetweenCheck = 2

  def open(string: String): Unit = {
    WebDriverArcusys.this.get(string)
    assert(!$(By.cssSelector(".alert.alert-error")).exists(), "Page not be found")
    assert(!$(By.cssSelector(".alert.alert-danger")).exists(), "Page not be found")
  }

  def waitForElementVisibleBy(locator: By) {
    val webWait = new WebDriverWait(this, waitForEvent, intervalBetweenCheck)
    webWait.until(ExpectedConditions.visibilityOfElementLocated(locator))
  }

  def waitForElementInvisibleBy(locator: By) {
    val webWait = new WebDriverWait(this, waitForEvent, intervalBetweenCheck)
    webWait.until(ExpectedConditions.invisibilityOfElementLocated(locator))
  }

  def getVisibleElementAfterWaitBy(locator: By) = {
    waitForElementVisibleBy(locator)
    this.findElement(locator)
  }
}
