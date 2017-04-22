package com.arcusys.valamis.tests.ui.contentManager

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.contentManager.page.ContentManagerPage

/**
 * Created by Igor Borisov on 13.07.15.
 */
class ContentTests(val driver: WebDriverArcusys) extends UITestSuite {

  "Content Manager" should "be able to create content" in {

    val page = new ContentManagerPage(driver)
    page.open

    val title = "CreateContent"
    val text = "Content text from test"

    val tree = page.contentTree
    tree.activateRootItem

    page.createContent(title, text)

    assert(!tree.existItem(title), "Content should not be in tree")

    val list = page.contentList

    assert(list.existsContent(title), "Content should be in list")

    val content = list.getContentItem(title)

    content.title shouldEqual title


    page.deleteItem(content)
  }

  it should "be able to delete content" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "DeleteContent"
    val text = "Content text from delete test"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createContent(title, text)

    assert(!tree.existItem(title), "Content for delete should not be in tree")

    val list = page.contentList

    assert(list.existsContent(title))

    val content = list.getContentItem(title)

    content.title shouldEqual title

    page.deleteItem(content)
    assert(!list.existsContent(title), "Content for delete should be deleted")
  }

  ignore should "be able to edit content title" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "EditContent"
    val newTitle = "EditContentEdited"
    val text = "Content text from edit test"
    val newText = "Edited content text from edit test"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createContent(title, text)

    assert(!tree.existItem(title), "Content item for test should not be in tree")

    val list = page.contentList

    assert(list.existsContent(title))

    val content = list.getContentItem(title)

    content.title shouldEqual title

    assert(content.editButton.isDisplayed && content.editButton.isEnabled, "Cannot find \"Edit\" button")

    page.editContent(content, newTitle, newText)

    assert(!list.existsContent(title))
    assert(list.existsContent(newTitle))

    val editedContent = list.getContentItem(newTitle)
    editedContent.title shouldEqual newTitle

    page.deleteItem(editedContent)
    assert(!list.existsContent(newTitle), "Content for delete should be deleted")
  }
}
