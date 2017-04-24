package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.By
import scala.collection.JavaConverters._

class CertificateGoalsList(driver: WebDriverArcusys) {

  val cssSelector = "#editGoals .js-certificate-goals"
  val listLocator = By.cssSelector(cssSelector)

  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)

  val itemSelector = "tr"

  def items: Seq[CertificateGoalsListItem] = {
    list.findElements(By.cssSelector(s"$itemSelector")).asScala.map(x =>
      CertificateGoalsListItem(x, driver)
    ).toSeq
  }

  def getItem(title:String): CertificateGoalsListItem = {
    items.filter(i => i.title == title).head
  }

  def existItem(title: String): Boolean = {
    items.exists(item => item.title == title)
  }

}
