package com.arcusys.valamis.tests.ui.lessonStudio.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.By
import scala.collection.JavaConverters._

/**
  * Created by vkudryashov on 11.10.16.
  */
class LessonList (driver: WebDriverArcusys) {

  val cssSelector = "#lessonStudioLessons .js-lesson-items"
  val listLocator = By.cssSelector(cssSelector)

  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)
  val itemSelector =   ".tile"

  def items: Seq[LessonStudioListItem] = {
    list.findElements(By.cssSelector(s"$itemSelector")).asScala.map(x =>
      LessonStudioListItem(x, driver)
    ).toSeq
  }

  def getItem(title: String): LessonStudioListItem = {
    items.filter(i => i.title == title).head
  }
  def existItem(title: String): Boolean = {
    items.exists(item => item.title == title)
  }
}
