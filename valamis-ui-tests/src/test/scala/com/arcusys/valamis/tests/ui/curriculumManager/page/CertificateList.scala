package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.By
import scala.collection.JavaConverters._

class CertificateList(driver: WebDriverArcusys) {

  val cssSelector = "#curriculumManagerAppRegion .js-certificate-items"
  val listLocator = By.cssSelector(cssSelector)

  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)

  val itemSelector = ".tile"

  def items: Seq[CertificateListItem] = {
    list.findElements(By.cssSelector(s"$itemSelector")).asScala.map(x =>
      CertificateListItem(x, driver)
    ).toSeq
  }

  def getItem(title:String): CertificateListItem = {
    items.filter(i => i.title == title).head
  }

  def existItem(title: String): Boolean = {
    items.exists(item => item.title == title)
  }
}
