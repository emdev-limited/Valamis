package com.arcusys.valamis.tests.ui.curriculumManager

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.curriculumManager.page.{CertificateDialog, CurriculumManagerPage}

class CertificateTests(val driver: WebDriverArcusys) extends UITestSuite  {

  ignore should "be able to create and delete certificate" in {
    val page = new CurriculumManagerPage(driver)
    page.open()

    val title = "My Certificate"

    page.createCertificate(title, "")

    val list = page.certificateList
    assert(list.existItem(title), "New certificate does not exist in the list")

    val certificate = list.getItem(title)

    certificate.title shouldEqual title
    certificate.description shouldEqual ""
    assert(certificate.usersAndGoals.contains("0 Students"))
    assert(certificate.usersAndGoals.contains("0 Goals"))
    certificate.logoStyle shouldEqual ""

    assert(certificate.isUnpublished, "New certificate must be unpublished")

    page.deleteCertificate(certificate)

    assert(!list.existItem(title), "New certificate still exists in the list")
  }

  ignore should "be able to set certificate image" in {  // requires at least one item in liferay media gallery
    val page = new CurriculumManagerPage(driver)
    page.open()

    val title = "My Certificate"

    page.createCertificate(title, "")

    val list = page.certificateList
    val certificate = list.getItem(title)
    certificate.editButton.click()

    page.setCertificateImage()

    assert(certificate.logoStyle != "", "Logo background is empty")

    page.deleteCertificate(certificate)
  }

  ignore should "be able to save all settings" in {
    val page = new CurriculumManagerPage(driver)
    page.open()

    val title = "My Certificate"
    page.createCertificate(title, "")
    val list = page.certificateList
    val certificate = list.getItem(title)

    page.setAndCheckSettings(certificate)

    page.deleteCertificate(certificate)
  }

  it should "be able to add course as goal" in {
    val page = new CurriculumManagerPage(driver)
    val dialog = new CertificateDialog(driver)
    page.open()

    val title = "Certificate with Course Goals"
    page.createCertificateAndContinue(title, "")
    dialog.addGoalCourse()

  }

}
