package com.arcusys.valamis.tests.ui.valamisAdministration.page

/**
 * Created by pnevalainen on 14.10.2015.
 */

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import com.arcusys.valamis.tests.ui.base.Urls
import org.openqa.selenium.{WebElement, By}
import scala.collection.JavaConverters._

class ValamisAdministrationPage(driver: WebDriverArcusys) {
  def open:Unit = driver.get(Urls.valamisAdministrationUrl)
  val mainMenu = "adminTabs"
  val badgesTab = "#badgesTabMenu"
  val tabMenu = By.id(mainMenu)
  val cssSelector = s"#$mainMenu > li"
  val listLocator = By.cssSelector(cssSelector)
  val dBadgesTab = s"a[href='$badgesTab']"
  lazy val list = driver.getVisibleElementAfterWaitBy(listLocator)
  
  def bT = new BadgesTab(driver)
  def tT = new TincanTab(driver)

  def openBadgesTab = {
    bT.openTab
  }

  def createAllBadgeData(m: Map[String, String]) = {
    bT.ids.foreach(x => createBadgeData(x, m(x)))
  }
  def createBadgeData(to: String, text: String = "test content") = {
    val dialog = bT
    assert(dialog.isExist)
    dialog.setInput(to,text)
  }
  def inputBadgeDataExists(m: Map[String, String]): Boolean = {
    val dialog = bT
    dialog.ids.foreach(x => if(!dialog.inputDataExists(x, m(x))) return false)
    true
  }
  def saveBadgeData = {
    val dialog = bT
    dialog.saveButton.click()
  }
  def items: Seq[WebElement] = {
    list.findElements(By.cssSelector(s"a")).asScala.toSeq
  }
  def openTincanTab = {
    tT.getTab.click()
  }
  def selectLrsInternal:Unit={
    tT.selectOption(0, "lrsTypes").click()
  }
  def selectLrsExternal:Unit={
    tT.selectOption(1, "lrsTypes").click()
  }
  def isLrsInternal:Boolean ={
    tT.selectOption(0, "lrsTypes").isSelected
  }
  def isLrsExternal:Boolean ={
    tT.selectOption(1, "lrsTypes").isSelected
  }
  def isAuthBasic:Boolean ={
    tT.selectOption(0, "authTypes").isSelected
  }
  def isAuthO:Boolean ={
    tT.selectOption(1, "authTypes").isSelected
  }
  def createLrsInternalData(text: String = "test content") = {
    //val dialog = tT.lrsInternal
    //assert(dialog.isDivsExist)
    //dialog.enterLrsAddress(text)
    //assert(tT.isElementExistI)
    tT.enterLrsAddress(text)
  }
  def isLrsInternalData(text: String = "test content"):Boolean = {
    //val dialog = tT.lrsInternal
    //assert(dialog.isDivsExist)
    //dialog.isData(text)
    tT.isData1(text)
  }
  def isLrsExtEndpoint(text: String = "test content"):Boolean = {
    tT.isData2(text)
  }
  def createLrsExtEndpointData(end: String = "test content") = {
    //assert(tT.isElementExist)
    tT.enterEndpoint(end)
  }
  def createLrsExtBasicData(end:String, name: String = "test content", pw: String = "test content") = {
    selectLrsExternal
    createLrsExtEndpointData(end)
    tT.selectOption(0, "authTypes").click()
    tT.enterBasic(name, pw)
  }
  def createLrsExtOAuthData(end:String, name: String = "test content", pw: String = "test content") = {
    selectLrsExternal
    createLrsExtEndpointData(end)
    tT.selectOption(1, "authTypes").click()
    tT.enterOAuth(name, pw)
  }
  def isLrsExtBasicData(end:String, name: String = "test content", pw: String = "test content"):Boolean = {
    isLrsExternal && isLrsExtEndpoint(end) && isAuthBasic && tT.isBasic(name, pw)
  }
  def isLrsExtOAuthData(end:String, name: String = "test content", pw: String = "test content"): Boolean= {
    isLrsExternal && isLrsExtEndpoint(end) && isAuthO && tT.isOAuth(name, pw)
  }

  def saveTincan={
    tT.saveButton.click()
  }
  def clearAllTincanData = {
    selectLrsInternal
    createLrsInternalData("")

    selectLrsExternal

    tT.selectOption(1, "authTypes").click()
    createLrsExtOAuthData("", "", "")

    tT.selectOption(0, "authTypes").click()
    createLrsExtBasicData("", "", "")

    selectLrsInternal

  }

}
