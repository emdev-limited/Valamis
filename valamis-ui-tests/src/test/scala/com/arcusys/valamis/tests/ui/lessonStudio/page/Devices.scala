package com.arcusys.valamis.tests.ui.lessonStudio.page

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import org.openqa.selenium.{By, WebElement}

/**
  * Created by vkudryashov on 30.09.16.
  */
class Devices (val driver: WebDriverArcusys) extends UITestSuite{

  lazy val dialog = driver.getVisibleElementAfterWaitBy(By.xpath(".//*[@class='bbm-modal bbm-modal--open']"))

  def isExist: Boolean = dialog.isEnabled && dialog.isDisplayed

  def selectDevice(DeviceName : String): Unit = {
    dialog.findElement(By.xpath(s".//*[text()='$DeviceName']/..")).click()
  }
  def continue : WebElement = dialog.findElement(By.cssSelector(".js-button-continue"))
}
