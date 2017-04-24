package com.arcusys.valamis.tests.ui.base


/**
 * Created by Igor Borisov on 13.07.15.
 */
trait UITestBase {
  val driver: WebDriverArcusys
  protected val adminLogin = "AutoTest@liferay.com"
  protected val adminPassword = "e,jhrf"
}
