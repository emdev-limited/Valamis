package com.arcusys.valamis.tests.ui.valamisAdministration.page

import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{By, WebElement}

/**
 * Created by pnevalainen on 15.10.2015.
 */
class TincanTab (driver: WebDriverArcusys){
  val tab = "#tincanTabMenu"
  val cssSelector = s"a[href='$tab']"
  val tabLocator = By.cssSelector(cssSelector)
  lazy val tabLink = driver.getVisibleElementAfterWaitBy(tabLocator)
  lazy val tabDiv = driver.getVisibleElementAfterWaitBy(By.cssSelector(tab))
  //div -> div-col content-col -> input

  val ids = Array("tincanInternalLrsAddress", "tincanEndpoint","tincanLrsBasicCredentialsLoginName","tincanLrsBasicCredentialsPassword","tincanLrsOAuthCredentialsLoginName","tincanLrsOAuthCredentialsPassword")

  val lrsTypes = Array("lrsInternal" , "lrsExternal")

  lazy val tincanLrsInternalPanel = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsInternalPanel"))
  lazy val inputsInternal = tincanLrsInternalPanel.findElement(By.cssSelector(s"#${ids(0)}"))

  lazy val tincanLrsExternalPanel = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsExternalPanel"))
  lazy val inputsExternal = tincanLrsExternalPanel.findElement(By.cssSelector(s"#${ids(1)}"))

  lazy val tincanAuthPanel = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanAuthPanel"))
  val authTypes = Array("tincanBasicAuth", "tincanOAuth")

  lazy val tincanLrsBasicCredentialsPanel = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsBasicCredentialsPanel"))
  lazy val inputsBasicName = tincanLrsBasicCredentialsPanel.findElement(By.cssSelector(s"#${ids(2)}"))

  lazy val tincanLrsBasicCredentialsPanelPassword = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsBasicCredentialsPanelPassword"))
  lazy val inputsBasicPW = tincanLrsBasicCredentialsPanelPassword.findElement(By.cssSelector(s"#${ids(3)}"))

  lazy val tincanLrsOAuthCredentialsPanel = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsOAuthCredentialsPanel"))
  lazy val inputsOAuthName = tincanLrsOAuthCredentialsPanel.findElement(By.cssSelector(s"#${ids(4)}"))

  lazy val tincanLrsOAuthCredentialsPanelPassword = driver.getVisibleElementAfterWaitBy(By.cssSelector("#tincanLrsOAuthCredentialsPanelPassword"))
  lazy val inputsOAuthPW = tincanLrsOAuthCredentialsPanelPassword.findElement(By.cssSelector(s"#${ids(5)}"))


  def getTab = {
    tabLink
  }
  def selectOption(i:Int, g:String): WebElement ={
    var optionId = ""
      g match{
      case "lrsTypes"  => optionId = lrsTypes(i)
      case "authTypes" => optionId = authTypes(i)
      case _ => optionId = null
    }
    tabDiv.findElement(By.cssSelector(s"#$optionId"))
  }
  def isExist: Boolean = {
    /*println("----------------------------");
    println(tabDiv.getAttribute("outerHTML"))
    println("----------------------------");*/
    tabDiv.isEnabled && tabDiv.isDisplayed
  }
  def saveButton = {
    tabDiv.findElement(By.cssSelector("#TincanSaveLrsSettings.button.big.primary"))
  }
  def setInput(id: String = "issuerName", txt: String = "test content"): Unit = {
      val input = getElement(id)
      input.clear()
      input.sendKeys(txt)
  }
  def getElement(id: String = "issuerName"):WebElement = {
    tabDiv.findElement(By.cssSelector(s"#$id"))
  }
  def clearInputs: Unit = {
    ids.foreach(getElement(_).clear())
  }
  def inputDataExists(id: String = "issuerName", txt: String = "test content"): Boolean = {
    tabDiv.findElement(By.cssSelector(s"#$id")).getAttribute("value") == txt
  }
  def isElementExistI:Boolean = {
    tincanLrsInternalPanel.isDisplayed && tincanLrsInternalPanel.isEnabled
  }
  def isDivsExist(divs: Array[String]): Boolean = {
    divs.foreach(x => if(!(isDivExist(x))) return false)
    true
  }
  def isDivExist(f:String): Boolean = {
    val div = getElement(f)
    div.isEnabled && div.isDisplayed
  }

  def enterLrsAddress(txt: String):Unit = {
    enterDataInsideDiv(tincanLrsInternalPanel, txt)
  }
  def enterEndpoint(txt: String):Unit = {
    enterDataInsideDiv(tincanLrsExternalPanel, txt)
  }
  def enterDataInsideDiv(div: WebElement, txt: String):Unit = {
    val input = div.findElement(By.cssSelector(".div-col.content-col")).findElement(By.cssSelector("input"))
    if(ids contains input.getAttribute("id")){
      input.clear()
      input.sendKeys(txt)
    }else assert(false, "Input does not exist on div")//:" + div.getAttribute("outerHTML")+ " div exist " + div.isDisplayed + "/" + div.isEnabled)
  }
  def enterDataToInput(elem: WebElement, txt: String):Unit = {
    val input = elem
      input.clear()
      input.sendKeys(txt)
  }
  def isData1(txt: String):Boolean = {
    inputDataExistsE(inputsInternal, txt)
  }
  def isData2(txt: String):Boolean = {
    inputDataExistsE(inputsExternal, txt)

  }
  def inputDataExistsE(element: WebElement, txt: String = "test content"): Boolean = {
    val input = element
    input.getAttribute("value") == txt
  }
  def enterBasic(name: String, pw: String)= {
    enterDataToInput(inputsBasicName, name)
    enterDataToInput(inputsBasicPW, pw)
  }
  def enterOAuth(name: String, pw: String)= {
    enterDataToInput(inputsOAuthName, name)
    enterDataToInput(inputsOAuthPW, pw)
  }
  def isBasic(name: String, pw: String): Boolean= {
    inputDataExistsE(inputsBasicName, name) && inputDataExistsE(inputsBasicPW, pw)
  }
  def isOAuth(name: String, pw: String): Boolean= {
    inputDataExistsE(inputsOAuthName, name) && inputDataExistsE(inputsOAuthPW, pw)
  }
}