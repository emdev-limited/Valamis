package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{WebElement, By}
import scala.collection.JavaConverters._

/**
 * Created by Igor Borisov on 13.07.15.
 */
case class ContentList(driver: WebDriverArcusys){

  val cssSelector = "#categoryContentView .js-content-list"
  val listLocator = By.cssSelector(cssSelector)

  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)

  val questionSelector = ".question"
  val categorySelector = ".category"
  val itemTitleSelector = ".js-content-entity"

  val itemSelector = ".lesson-item-li"

  def contentTitle : WebElement = {
    list.findElement(By.cssSelector(s"$questionSelector $itemTitleSelector"))
  }

  def categoryTitle : WebElement = {
    list.findElement(By.cssSelector(s"$categorySelector $itemTitleSelector"))
  }

  def items: Seq[ContentListItem] = {
    list.findElements(By.cssSelector(s"$itemSelector")).asScala.map(x =>
      ContentListItem(x, driver)
    ).toSeq
  }

  def getItem(title:String): ContentListItem = {
    items.filter(i => i.title == title).head
  }

  def getCategoryItem(title:String): ContentListItem = {
    items.filter(i => i.title == title && i.isCategory).head
  }

  def getQuestionItem(title:String): ContentListItem = {
    items.filter(i => i.title == title && i.isQuestion).head
  }

  def getContentItem(title:String): ContentListItem = {
    items.filter(i => i.title == title && i.isContent).head
  }

  def existsCategory(title:String): Boolean = {
    items.exists(item => item.title == title && item.isCategory )
  }

  def existsContent(title:String): Boolean = {
    items.exists(item => item.title == title && item.isContent )
  }

  def existsQuestion(title:String): Boolean = {
    items.exists(item => item.title == title && item.isQuestion )
  }

  private def existItem(title: String, typeSelector: String): Boolean = {
    items.exists(item => item.title == title )
  }

  def selectedItems:Seq[ContentListItem] = {
    items.filter(item => item.isSelected)
  }
}