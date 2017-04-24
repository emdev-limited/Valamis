package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{WebElement, By}
import scala.collection.JavaConverters._

/**
  * Created by Igor Borisov on 13.07.15.
  */
class ContentTree(driver: WebDriverArcusys) {

  val cssSelector = "#contentManagerContentsView .val-tree"
  val treeLocator = By.cssSelector(cssSelector)

  def tree = driver.getVisibleElementAfterWaitBy(treeLocator)

  val rootSelector = "li.js-root"
  val rootElemSelector = "li.js-root .js-tree-root-item"
  val childrenSelector = ".tree-items"
  val categorySelector = ".category .js-tree-category"
  val itemTitleSelector = ".js-tree-item-title"
  val itemSelector = ".js-tree-item"
  val itemIconSelector = ".js-tree-item-icon"

  def getByTitle(title: String):ContentTreeItem = {
    assert(existItem(title), s"Contents tree does not contain category $title")

    val treeItems = items.filter(item => item.title == title).seq

    assert(treeItems.nonEmpty, "Tree is empty")

    val item = treeItems.head

    item
  }

  def getRootItem: ContentTreeItem = {
    val root = tree.findElement(By.cssSelector(rootElemSelector))
    assert(root.isEnabled, "Root category is not enabled")

    ContentTreeItem(root, driver)
  }

  def items: Seq[ContentTreeItem] = {
    assert(tree.isDisplayed, "tree is not displayed")
    assert(tree.isEnabled, "tree is not enabled")
    tree.findElements(By.cssSelector(s"$rootSelector $itemSelector")).asScala.map(x =>
      ContentTreeItem(x, driver)
    ).toSeq
  }

  def existItem(title: String): Boolean = {
    items.exists(item => item.title == title)
  }

  def activateRootItem(): Unit = {
    getRootItem.activate
  }

  def activateItem(title: String): Unit = {
    val item = getByTitle(title)
    item.activate
  }

  def expandItem(title: String): Unit = {
    val item = getByTitle(title)
    item.expand
  }
}
