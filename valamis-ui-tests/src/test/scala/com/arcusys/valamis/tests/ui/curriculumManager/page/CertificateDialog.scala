package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{WebElement, By}

class CertificateDialog(driver: WebDriverArcusys) {
  val cssSelector = "#valamisAppModalRegion .modal-content"
  // todo need to be more specified
  val dialogLocator = By.cssSelector(cssSelector)
  lazy val dialog = driver.getVisibleElementAfterWaitBy(dialogLocator)
  val page = new CurriculumManagerPage(driver)

  val editDetailsTabSelector = "#editDetails"

  def isExist: Boolean = {
    dialog.isEnabled && dialog.isDisplayed
  }

  def setCertificateTitle(title: String): Unit = {
    val input = dialog.findElement(By.cssSelector(editDetailsTabSelector + " .js-certificate-title"))
    input.clear()
    input.sendKeys(title)
  }

  def setCertificateDescription(description: String): Unit = {
    val input = dialog.findElement(By.cssSelector(editDetailsTabSelector + " .js-certificate-description"))
    input.clear()
    input.sendKeys(description)
  }

  def setCertificateImage(): Unit = {
    dialog.findElement(By.cssSelector(".js-select-from-media-gallery")).click()
    val button = driver.getVisibleElementAfterWaitBy(By.cssSelector(".modal-content #galleryContainer .image-thumbnail"))
    button.click()
    driver.waitForElementInvisibleBy(By.cssSelector(".modal-content #galleryContainer"))
  }

  def saveButton: WebElement = {
    dialog.findElement(By.cssSelector(".js-submit-button"))
  }

  def continueButton: WebElement = {
    dialog.findElement(By.cssSelector(".js-save-continue"))
  }

  def setCertificateScope(): Unit = {
    dialog.findElement(By.cssSelector(".js-edit-scope")).click()
    val button = driver.getVisibleElementAfterWaitBy(By.cssSelector(".modal-content #liferaySiteSelectListRegion .js-toggle-site-button"))
    button.click()
    driver.waitForElementInvisibleBy(By.cssSelector(".modal-content #liferaySiteSelectListRegion"))
  }

  def clickOnElement(selector: String): Unit = {
    dialog.findElement(By.cssSelector(selector)).click()
  }

  def setElementText(selector: String, text: String): Unit = {
    val elem = dialog.findElement(By.cssSelector(selector))
    elem.clear()
    elem.sendKeys(text)
  }

  def setSelectElementValue(selector: String, text: String): Unit = {
    val typeSelect = new Select(dialog.findElement(By.cssSelector(selector)))
    typeSelect.selectByVisibleText(text)
  }

  def getElementText(selector: String): String = {
    val elem = dialog.findElement(By.cssSelector(selector))
    val text = elem.getText
    if (!text.isEmpty)
      text
    else
      elem.getAttribute("value")
  }

  def isElementSelected(selector: String): Boolean = {
    dialog.findElement(By.cssSelector(selector)).isSelected
  }

  def getSelectElementValue(selector: String): String = {
    val typeSelect = new Select(dialog.findElement(By.cssSelector(selector)))
    typeSelect.getFirstSelectedOption.getText
  }

  def addGoalCourse(): String = {
    dialog.findElement(By.cssSelector(".dropdown button")).click()
    dialog.findElement(By.cssSelector(".js-add-course")).click()
    val course = driver.getVisibleElementAfterWaitBy(By.cssSelector("tr:nth-child(2)"))
    val courseTitle = course.findElement(By.cssSelector("td:nth-child(1)")).getText
    val button = course.findElement(By.cssSelector(".js-toggle-site-button"))
    button.click()
    assert(button.getAttribute("class").contains("primary"))
    driver.findElement(By.cssSelector(".modal-content .js-addCourses")).click()
    driver.waitForElementInvisibleBy(By.cssSelector(".modal-content #liferaySiteSelectListRegion"))
    page.waitForSuccess()
    courseTitle
  }

  def addStatement(): Unit = {
    dialog.findElement(By.cssSelector(".dropdown button")).click() // todo dropdown button need to be more specified
    dialog.findElement(By.cssSelector(".js-add-statement")).click()
    val selectStatementButton = driver.getVisibleElementAfterWaitBy(By.cssSelector(".modal-content .js-select-statements")) // todo modal need to be more specified
    selectStatementButton.click()

    val course = driver.getVisibleElementAfterWaitBy(By.cssSelector(".modal-content #statementsList .js-statements-list tr")) // todo modal need to be more specified
    val verbName = course.findElement(By.cssSelector("td:nth-child(1)")).getText
    val objName = course.findElement(By.cssSelector("td:nth-child(2)")).getText
    val button = course.findElement(By.cssSelector(".js-select-statement"))
    button.click()
    assert(button.getAttribute("class").contains("primary"))
    driver.findElement(By.cssSelector(".modal-content .js-submit-button")).click() // todo need to be more specified
    driver.waitForElementInvisibleBy(By.cssSelector(".modal-content #statementsList"))
    val newStatement = driver.getVisibleElementAfterWaitBy(By.cssSelector(".modal-content .js-statements-list tr"))

    assert(!newStatement.findElement(By.cssSelector(".js-statement-object")).getAttribute("value").isEmpty)
    driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-submit-button")).click()
  }

  def addGoalLesson(): Unit = {
    dialog.findElement(By.cssSelector(".dropdown button")).click()
    dialog.findElement(By.cssSelector(".js-add-lesson")).click()
    val buttonLocation = driver.getVisibleElementAfterWaitBy(By.cssSelector("td:nth-child(2)"))
    val button = buttonLocation.findElement(By.cssSelector(".js-select-lesson"))
    button.click()
    assert(button.getAttribute("class").contains("primary"))
    val saveLocation = driver.getVisibleElementAfterWaitBy(By.xpath(".//*[text()='Add lessons']/.."))
    saveLocation.findElement(By.cssSelector(".js-submit-button")).click()
    page.waitForSuccess()
  }

  def addGoalMember(name: String): Unit = {
    lazy val addMemberButton = driver.getVisibleElementAfterWaitBy(By.cssSelector(".js-add-items"))
    addMemberButton.click()
    val button = driver.findElement(By.xpath(s".//*[text()=' $name ']/../*/button"))
    button.click()
    assert(button.getAttribute("class").contains("primary"))

    val add = driver.findElement(By.xpath(".//*[text()='Add']/../button"))
    add.click()
    page.waitForSuccess()
  }

}
