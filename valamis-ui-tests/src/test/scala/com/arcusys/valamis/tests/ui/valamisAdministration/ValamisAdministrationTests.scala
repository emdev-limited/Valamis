package com.arcusys.valamis.tests.ui.valamisAdministration

/**
 * Created by pnevalainen on 14.10.2015.
 */

import com.arcusys.valamis.tests.ui.base.{WebDriverArcusys, UITestSuite}
import com.arcusys.valamis.tests.ui.valamisAdministration.page.ValamisAdministrationPage

class ValamisAdministrationTests(val driver: WebDriverArcusys) extends UITestSuite {
  "Valamis Administration" should "be able to save badge data" in {
    val page = new ValamisAdministrationPage(driver)
    page.open
    page.openBadgesTab.click()
    //println(">>BadgesTests")
    val mData = Map("issuerName"->"Content text from test", "issuerUrl" -> "http://localhost:8080", "issuerEmail" -> "test@liferay.com")

    page.createAllBadgeData(mData)

    page.saveBadgeData

    page.open
    page.openBadgesTab.click()

    assert(page.inputBadgeDataExists(mData), "Failed, inputted data does not match!")

    page.bT.clearInputs

    page.saveBadgeData
  }
  ignore should "be able to save Tincan settings data" in {
    //println(">>Tincan settings")

    val page = new ValamisAdministrationPage(driver)
    val testData1 = Array("Test.Data","Test2.Data/", "Basic.Username", "BasicPW", "OAuth.Username", "OAuthPW")
    page.open
    page.openTincanTab
    page.selectLrsInternal
    page.createLrsInternalData(testData1(0))
    page.saveTincan
    assert(page.isLrsInternal)
    assert(page.isLrsInternalData(testData1(0)))
    page.clearAllTincanData

    //createLrsExtEndpointData,createLrsExtBasic , isLrsExtEndpoint, clearAllTincanData2

    page.selectLrsExternal
    //println("create lrs external data, basic")
    page.createLrsExtBasicData(testData1(1), testData1(2), testData1(3))
    page.saveTincan
    page.open
    page.openTincanTab
    assert(page.isLrsExtBasicData(testData1(1), testData1(2), testData1(3)), "Inputted data does not match, basic")

    page.clearAllTincanData

    page.selectLrsExternal
    //println("create lrs external data, oauth")
    page.createLrsExtOAuthData(testData1(1), testData1(4), testData1(5))
    page.saveTincan
    page.open
    page.openTincanTab
    assert(page.isLrsExtOAuthData(testData1(1), testData1(4), testData1(5)), "Inputted data does not match, oauth")
    page.clearAllTincanData



  }
  it should "be able to save Google API settings data" in {
    //println(">>Google API Tests")


  }
  it should "be able to save Optional data" in {
    //println(">>Optional Tests")


  }
  it should "be able to save Licence data" in {
    //println(">>Licence Tests")


  }
}
