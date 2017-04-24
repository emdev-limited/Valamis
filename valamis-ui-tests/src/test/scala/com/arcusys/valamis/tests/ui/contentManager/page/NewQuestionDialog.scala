package com.arcusys.valamis.tests.ui.contentManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{JavascriptExecutor, By}

/**
 * Created by mromanova on 05.11.15.
 */
class NewQuestionDialog(driver: WebDriverArcusys) {
  val cssSelector = "#valamisAppModalRegion .val-modal .modal-content"
  val dialogLocator = By.cssSelector(cssSelector)
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)
  val questionType = By.cssSelector(".js-question-type")

  val choiceType = By.xpath(".//*[@id='SCORMQuestionType']/option[1]")
  val shortType = By.xpath(".//*[@id='SCORMQuestionType']/option[2]")
  val numericType = By.xpath(".//*[@id='SCORMQuestionType']/option[3]")
  val positioningType = By.xpath(".//*[@id='SCORMQuestionType']/option[4]")
  val matchingType = By.xpath(".//*[@id='SCORMQuestionType']/option[5]")
  val essayType = By.xpath(".//*[@id='SCORMQuestionType']/option[6]")
  val categorizationType = By.xpath(".//*[@id='SCORMQuestionType']/option[7]")

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def choiceQuestionType = {
      driver.getVisibleElementAfterWaitBy(questionType).click()
      driver.getVisibleElementAfterWaitBy(choiceType).click()
  }

  def shortQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(shortType).click()
  }

  def numericQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(numericType).click()
  }

  def positioningQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(positioningType).click()
  }

  def matchingQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(matchingType).click()
  }

  def essayQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(essayType).click()
  }

  def categorizationQuestionType = {
    driver.getVisibleElementAfterWaitBy(questionType).click()
    driver.getVisibleElementAfterWaitBy(categorizationType).click()
  }

  def questionName = {
    dialog.findElement(By.cssSelector(".js-title"))
  }

  def setQuestionName(title: String) = {
    val input = dialog.findElement(By.cssSelector(".js-title"))
    assert(input.isEnabled && input.isDisplayed)
    input.clear()
    input.sendKeys(title)
  }

  def setContent(content: String) = {
    driver.waitForElementVisibleBy(By.cssSelector("#cke_contentManagerQuestionTextView"))
    driver.asInstanceOf[JavascriptExecutor]
      .executeScript(s"CKEDITOR.instances.contentManagerQuestionTextView.setData('$content');")
  }

  def saveButton = {
    dialog.findElement(By.cssSelector(".js-saveQuestion"))
  }
}
