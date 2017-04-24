package com.arcusys.valamis.tests.ui.lessonStudio.page

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import org.openqa.selenium.By

/**
  * Created by vkudryashov on 30.09.16.
  */
class ComposePage(val driver: WebDriverArcusys) extends UITestSuite {

  lazy val page = driver.getVisibleElementAfterWaitBy(By.cssSelector(".slideset-editor"))
  def isExist: Boolean = page.isEnabled && page.isDisplayed
 }
