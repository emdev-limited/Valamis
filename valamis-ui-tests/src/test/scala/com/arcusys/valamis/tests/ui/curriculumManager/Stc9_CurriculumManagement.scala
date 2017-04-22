package com.arcusys.valamis.tests.ui.curriculumManager

import com.arcusys.valamis.tests.ui.base.page.DeleteDialog
import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.curriculumManager.page.{CertificateDialog, CertificateListItem, CurriculumManagerPage}

/**
  * Created by vkudryashov on 04.10.16.
  */
class Stc9_CurriculumManagement(val driver: WebDriverArcusys) extends UITestSuite {

  it should "be able to create certificate with 1 learner, 2 goals, logo" in {

    val title = "KING of stc!"
    val description = "certificate for test"
    val page = new CurriculumManagerPage(driver)
    val dialog = new CertificateDialog(driver)
    val userName = "Selenium AutoTest"

    page.open()

    page.createCertificateAndContinue(title, description)
    dialog.addGoalCourse()
    dialog.addGoalLesson()
    dialog.continueButton.click()
    dialog.addGoalMember(userName)
    dialog.saveButton.click()
    page.waitForSuccess()

    val list = page.certificateList
    val certificate = list.getItem(title)

    certificate.title shouldEqual title
    certificate.description shouldEqual description
    assert(certificate.usersAndGoals.contains("1 Learners"))
    assert(certificate.usersAndGoals.contains("2 Goals"))
    assert(certificate.logoStyle != "", "Logo background is empty")
    assert(certificate.isUnpublished, "New certificate must be unpublished")
  }

  it should "be able to publish certificate" in {

    val title = "KING of stc!"
    val page = new CurriculumManagerPage(driver)

    val list = page.certificateList
    val certificate = list.getItem(title)
    certificate.activateButton.click()
    page.waitForSuccess()
    assert(!certificate.isUnpublished, "certificate unpublished")
  }

  it should "be able to delete certificate" in {

    val title = "KING of stc!"
    val page = new CurriculumManagerPage(driver)

    val list = page.certificateList

    assert (list.existItem(title), "Certificate for delete not found")
    val certificate = list.getItem(title)
    certificate.deleteButton.click()
    val deleteDialog = new DeleteDialog(driver)
    deleteDialog.yesButton.click()
    page.waitForSuccess()
  }
}
