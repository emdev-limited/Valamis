package com.arcusys.valamis.tests.ui.valamisAdministration.page
import com.arcusys.valamis.tests.ui.base.WebDriverArcusys
import org.openqa.selenium.{WebElement, By}
/**
 * Created by pnevalainen on 15.10.2015.
 */
class BadgesTab (driver: WebDriverArcusys){
  val badgesTab = "#badgesTabMenu"
  val cssSelector = s"a[href='$badgesTab']"
  val tabLocator = By.cssSelector(cssSelector)
  lazy val tabLink = driver.getVisibleElementAfterWaitBy(tabLocator)
  lazy val tabDiv = driver.getVisibleElementAfterWaitBy(By.cssSelector(badgesTab))
  val ids = Array("issuerName", "issuerUrl","issuerEmail")

  def openTab = {
    tabLink
  }
  def isExist: Boolean = {
    tabDiv.isEnabled && tabDiv.isDisplayed
  }
  def saveButton = {
  /*
    println("----------------------------");
    println(tabDiv.getAttribute("outerHTML"))
    println("----------------------------");
    */
    tabDiv.findElement(By.cssSelector("button.button.big.primary"))

  }
  def setInput(id: String = "issuerName", txt: String = "test content"): Unit = {
      val input = getInputElement(id)
      input.clear()
      input.sendKeys(txt)
  }
  def getInputElement(id: String = "issuerName"):WebElement = {
    tabDiv.findElement(By.cssSelector(s"#$id"))
  }
  def clearInputs: Unit = {
    ids.foreach(getInputElement(_).clear())
  }
  def inputDataExists(id: String = "issuerName", txt: String = "test content"): Boolean = {
    tabDiv.findElement(By.cssSelector(s"#$id")).getAttribute("value") == txt
  }

}
