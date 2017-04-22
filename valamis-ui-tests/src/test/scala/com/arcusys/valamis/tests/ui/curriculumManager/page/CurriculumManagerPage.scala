package com.arcusys.valamis.tests.ui.curriculumManager.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import com.arcusys.valamis.tests.ui.base.Urls
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{WebElement, By}

class CurriculumManagerPage(driver: WebDriverArcusys) {

  val newButtonLocator = By.cssSelector("#curriculumManagerToolbar .js-create-certificate")

  def open(): Unit = driver.open(Urls.curriculumManagerUrl)

  def createCertificate(title: String, description: String): Unit = {
    newCertificateButton.click()
    val dialog = certificateDialog

    dialog.setCertificateTitle(title)
    dialog.setCertificateDescription(description)
    dialog.setCertificateImage()
    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)

    waitForSuccess()
  }

  def createCertificateAndContinue(title: String, description: String): Unit = {
    newCertificateButton.click()
    val dialog = certificateDialog

    dialog.setCertificateTitle(title)
    dialog.setCertificateImage()
    dialog.setCertificateDescription(description)
    dialog.continueButton.click()

    val goalsTab = driver.getVisibleElementAfterWaitBy(By.cssSelector("#editCertificateTabs a[href='#editGoals']"))
    goalsTab.click()
    driver.waitForElementVisibleBy(By.cssSelector(".js-certificate-goals"))
  }

  def deleteCertificate(certificate: CertificateListItem): Unit = {
    certificate.deleteButton.click()

    val confirmation = new Notification(driver)
    driver.waitForElementVisibleBy(confirmation.notificationInfoLocator)
    assert(confirmation.isExist && confirmation.isConfirmation, "New dialog for delete does not exist")

    confirmation.yesButton.click()
    driver.waitForElementInvisibleBy(confirmation.notificationInfoLocator)

    waitForSuccess()
  }

  def setCertificateImage(): Unit = {
    val dialog = certificateDialog

    dialog.setCertificateImage()
    val hasDefaultLogo = dialog.dialog.findElement(By.cssSelector(".js-logo")).getAttribute("src").contains("certificate_cover.svg")
    assert(!hasDefaultLogo, "Certificate logo was not changed")

    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)

    waitForSuccess()
  }

  def setAndCheckSettings(certificate: CertificateListItem): Unit = {
    val description = "Best certificate ever"
    val badgeDescription = "Badge description"

    certificate.editButton.click()

    var dialog = certificateDialog

    dialog.setCertificateDescription(description)
    dialog.setCertificateScope()
    dialog.clickOnElement("#nonPermanentPeriod")
    dialog.setElementText(".js-plus-minus input", "5")
    dialog.setSelectElementValue(".js-valid-period-type", "Years")
    dialog.clickOnElement("label[for='openBadgeIntegration']")
    dialog.setElementText(".js-openbadges-description", badgeDescription)

    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)

    waitForSuccess()

    assert(certificate.description.contains(description))

    certificate.editButton.click()

    dialog = certificateDialog

    assert(dialog.getElementText(".js-certificate-title").contains(certificate.title))
    assert(dialog.getElementText(".js-certificate-description").contains(description))
    assert(!dialog.getElementText(".js-scope-name").contains("Instance scope"))
    assert(dialog.isElementSelected("#nonPermanentPeriod"))
    assert(dialog.getElementText(".js-plus-minus input").contains("5"))
    assert(dialog.getSelectElementValue(".js-valid-period-type").contains("Years"))
    assert(dialog.isElementSelected("#openBadgeIntegration"))
    assert(dialog.getElementText(".js-openbadges-description").contains(badgeDescription))

    dialog.saveButton.click()
    driver.waitForElementInvisibleBy(dialog.dialogLocator)
  }

  def addStatementAndCheck(): Unit = {
    val dialog = certificateDialog
    val statementTitle = dialog.addStatement()
    waitForSuccess()
  }

  def waitForSuccess(): Unit = {
    val result = new Notification(driver)
    driver.waitForElementVisibleBy(result.notificationSuccessLocator)
    assert(result.isExist && result.isSucceed, "not succeed saving")
    result.click()
    driver.waitForElementInvisibleBy(result.notificationSuccessLocator)
  }

  def newCertificateButton: WebElement = {
    driver.getVisibleElementAfterWaitBy(newButtonLocator)
  }

  def certificateDialog: CertificateDialog = {
    val dialog = new CertificateDialog(driver)
    assert(dialog.isExist, "Certificate dialog does not exist")
    dialog
  }

  def certificateList: CertificateList = new CertificateList(driver)
}

