package com.arcusys.valamis.tests.ui.contentManager

import com.arcusys.valamis.tests.ui.base.{WebDriverArcusys, UITestSuite}
import com.arcusys.valamis.tests.ui.contentManager.page.ContentManagerPage
import org.scalatest.Ignore

/**
 * Created by Igor Borisov on 13.07.15.
 */
class CategoryTests(val driver: WebDriverArcusys) extends UITestSuite {

  "Content Manager" should "be able to create category" in {
    val page = new ContentManagerPage(driver)
    page.open()

    val title = "CreateCategory"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createCategory(title)

    assert(tree.existItem(title), "New category does not exist in the tree")

    val list = page.contentList

    assert(list.existsCategory(title), "New category does not exist in the content list")

    val category = list.getCategoryItem(title)

    category.title shouldEqual title

    page.deleteItem(category)
  }

  it should "be able to delete category" in {
    val page = new ContentManagerPage(driver)
    page.open()

    val title = "DeleteCategory"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createCategory(title)

    assert(tree.existItem(title), "Category for delete does not exist in the tree")

    val list = page.contentList

    assert(list.existsCategory(title), "Category for delete does not exist in the list")

    val category = list.getCategoryItem(title)

    category.title shouldEqual title

    page.deleteItem(category)

    assert(!list.existsCategory(title), "Category for delete should not be in list")
    assert(!tree.existItem(title), "Category for delete should not be in tree")
  }

  //TODO issue with removing nested categories
  ignore should "be able to create nested category" in {

    val page = new ContentManagerPage(driver)
    page.open

    val parentCategoryTitle = "Category1"
    val childCategoryTitle = "Category11"

    val tree = page.contentTree
    val list = page.contentList
    tree.activateRootItem()

    page.createCategory(parentCategoryTitle)

    assert(tree.existItem(parentCategoryTitle), s"Content tree does not have item $parentCategoryTitle")

    tree.activateItem(parentCategoryTitle)

    page.createCategory(childCategoryTitle)

    tree.expandItem(parentCategoryTitle)

    assert(tree.existItem(childCategoryTitle), s"Content tree does not have item $childCategoryTitle")
    assert(list.existsCategory(childCategoryTitle), s"Content list does not have item $childCategoryTitle")

    tree.activateRootItem()
    page.deleteItem(list.getCategoryItem(parentCategoryTitle))
  }

  //TODO issue with removing nested categories
  ignore should "be able to create nested nested category" in {

    val page = new ContentManagerPage(driver)
    page.open

    val parentCategoryTitle = "Category 2"
    val childCategoryTitle1 = "Category 21"
    val childCategoryTitle2 = "Category 211"

    val tree = page.contentTree
    val list = page.contentList
    tree.activateRootItem()

    page.createCategory(parentCategoryTitle)

    assert(tree.existItem(parentCategoryTitle), s"Content tree does not have item $parentCategoryTitle")

    tree.activateItem(parentCategoryTitle)

    page.createCategory(childCategoryTitle1)

    tree.expandItem(parentCategoryTitle)

    assert(tree.existItem(childCategoryTitle1))


    tree.activateItem(childCategoryTitle1)

    page.createCategory(childCategoryTitle2)

    tree.expandItem(childCategoryTitle1)

    assert(tree.existItem(childCategoryTitle2))

    tree.activateRootItem()
    page.deleteItem(list.getCategoryItem(parentCategoryTitle))
  }
}
