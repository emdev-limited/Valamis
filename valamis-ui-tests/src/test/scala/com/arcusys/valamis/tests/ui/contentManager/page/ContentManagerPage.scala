package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.page.DeleteDialog
import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import com.arcusys.valamis.tests.ui.base.Urls
import org.openqa.selenium.{By, WebElement}

/**
  * Created by Igor Borisov on 13.07.15.
  */
class ContentManagerPage(driver: WebDriverArcusys) {

  val newButtonLocator = By.cssSelector("#contentManagerToolbar .js-add-new-content")
  val selectAllButtonLocator = By.cssSelector("#contentManagerToolbar .js-select-all")

  def open(): Unit = driver.get(Urls.contentManagerUrl)

  def createCategory(title: String): Unit = {
    newCategoryButton.click()
    val dialog = newCategoryDialog

    assert(dialog.isExist, "New category dialog does not exist")

    dialog.setCategoryTitle(title)

    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def newCategoryButton: WebElement = {
    driver.getVisibleElementAfterWaitBy(newButtonLocator).click()
    driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-add-category"))
  }

  def newCategoryDialog: NewCategoryDialog = new NewCategoryDialog(driver)

  def createContent(title: String, text: String = "test content"): Unit = {
    newContentButton.click()

    val dialog = newContentDialog

    assert(dialog.isExist, "New content dialog does not exist")

    dialog.setContentTitle(title)
    dialog.setContent(text)
    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def newContentButton: WebElement = {
    driver.getVisibleElementAfterWaitBy(newButtonLocator).click()
    driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-add-content"))
  }

  def newContentDialog: NewContentDialog = new NewContentDialog(driver)

  def editContent(content: ContentListItem, title: String, text: String = "edit content"): Unit = {
    content.editButton.click()

    val dialog = newContentDialog

    assert(dialog.isExist, "Edit content dialog does not exist")

    assert(dialog.title.getAttribute("value") == content.title, "Invalid content title")
    dialog.setContentTitle(title)
    dialog.setContent(text)
    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def createQuestion(title: String, text: String = "test content", questionType: String): Unit = {
    newQuestionButton.click()

    val dialog = newQuestionDialog

    assert(dialog.isExist, "Question dialog does not exist")

    questionType match {
      case "choiceType" => dialog.choiceQuestionType
      case "shortType" => dialog.shortQuestionType
      case "numericType" => dialog.numericQuestionType
      case "positioningType" => dialog.positioningQuestionType
      case "matchingType" => dialog.matchingQuestionType
      case "essayType" => dialog.essayQuestionType
      case "categorizationType" => dialog.categorizationQuestionType
    }

    dialog.setQuestionName(title)
    dialog.setContent(text)
    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def newQuestionButton: WebElement = {
    driver.getVisibleElementAfterWaitBy(newButtonLocator).click()
    driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-add-question"))
  }

  def newQuestionDialog: NewQuestionDialog = new NewQuestionDialog(driver)

  def deleteItem(contentItem: ContentListItem): Unit = {
    contentItem.deleteButton.click()
    val dialog = deleteItemDialog

    driver.waitForElementVisibleBy(dialog.dialogLocator)

    assert(dialog.isExist, "New content dialog for delete does not exist")
    dialog.yesButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def deleteItemDialog: DeleteDialog = new DeleteDialog(driver)

  def selectAll(): Unit = driver.getVisibleElementAfterWaitBy(selectAllButtonLocator).click()

  def contentTree: ContentTree = new ContentTree(driver)

  def contentList: ContentList = new ContentList(driver)
}