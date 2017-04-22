package com.arcusys.valamis.tests.ui.courseManager.page

import com.arcusys.valamis.tests.ui.base.{Urls, WebDriverArcusys}
import org.openqa.selenium.{By, WebElement}
/**
  * Created by vkudryashov on 25.08.16.
  */
class NewCoursePage(driver: WebDriverArcusys) {

  lazy val alert =  driver.findElements(By.cssSelector(".alert.alert-error"))

  def open(title: String): Unit = {
    driver.get(Urls.baseUrl + "/web/" + title)

  }
}
