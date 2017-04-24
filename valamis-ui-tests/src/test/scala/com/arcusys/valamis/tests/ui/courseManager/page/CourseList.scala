package com.arcusys.valamis.tests.ui.courseManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}
import scala.collection.JavaConverters._

/**
  * Created by vkudryashov on 10.10.16.
  */
class CourseList(driver: WebDriverArcusys) {

  val cssSelector = "#allCourseCourseList .js-courses-list"
  val listLocator = By.cssSelector(cssSelector)

  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)
  val itemSelector = ".tile"

  def items: Seq[CourseManagerListItem] = {
    list.findElements(By.cssSelector(s"$itemSelector")).asScala.map(x =>
      CourseManagerListItem(x, driver)
    ).toSeq
  }

  def getItem(title: String): CourseManagerListItem = {
    items.filter(i => i.title == title).head
  }
  def existItem(title: String): Boolean = {
    items.exists(item => item.title == title)
  }
}
