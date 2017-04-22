package com.arcusys.valamis.tests.ui.base

import java.net.URL

import com.arcusys.valamis.tests.ui.contentManager.{CategoryTests, ContentTests, QuestionsTests}
import com.arcusys.valamis.tests.ui.curriculumManager.{CertificateTests, Stc9_CurriculumManagement}
import com.arcusys.valamis.tests.ui.courseManager.Stc1_CourseManagerTests
import com.arcusys.valamis.tests.ui.lessonStudio.{Stc2_CreateLesson, Stc3_CloneLesson, Stc4_ComposeLesson}
import com.arcusys.valamis.tests.ui.valamisAdministration.ValamisAdministrationTests
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.remote.{CapabilityType, DesiredCapabilities, RemoteWebDriver}
import org.scalatest.{BeforeAndAfterAll, Suites}
import com.codeborne.selenide.{Configuration, WebDriverRunner}
import org.openqa.selenium.{NoAlertPresentException, UnhandledAlertException, WebDriverException}


/**
  * Created by Igor Borisov on 13.07.15.
  */
class UITestMainSuite extends Suites with LoginSupport with BeforeAndAfterAll {


  System.setProperty("webdriver.chrome.driver", "valamis-ui-tests/src/test/resources/chromedriver")
  Configuration.reportsFolder = "/project/valamis/valamis/valamis-ui-tests/target/report/"

  val optionsChrome = new ChromeOptions()
  optionsChrome.addArguments("start-maximized")

  val dc = DesiredCapabilities.chrome()
  dc.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true)
  dc.setCapability(ChromeOptions.CAPABILITY, optionsChrome)

  val driver = new RemoteWebDriver(new URL("http://10.93.84.66:5555/wd/hub"), dc) with WebDriverArcusys

  WebDriverRunner.setWebDriver(driver)




  override def beforeAll() {
    loginAsAdmin()
  }

  override val nestedSuites = collection.immutable.IndexedSeq(

    new CategoryTests(driver),
    new Stc1_CourseManagerTests (driver),
    new Stc2_CreateLesson(driver),
    new Stc3_CloneLesson(driver),
//    new stc4_ComposeLesson(driver),
    new Stc9_CurriculumManagement(driver),
//    new CertificateTests(driver),
    new ContentTests(driver),
//    new QuestionsTests(driver),
    new ValamisAdministrationTests(driver)
  )

  override def afterAll() {
    driver.close()
    driver.quit()
  }
}
