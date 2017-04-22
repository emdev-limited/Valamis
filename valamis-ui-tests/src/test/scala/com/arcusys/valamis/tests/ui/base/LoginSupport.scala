package com.arcusys.valamis.tests.ui.base

import com.codeborne.selenide.Selenide._
import com.codeborne.selenide.Condition._
import com.codeborne.selenide.Selectors._
import org.openqa.selenium.By


/**
  * Created by Igor Borisov on 13.07.15.
  */
trait LoginSupport extends UITestBase {

  def loginAsAdmin() {
    login(adminLogin, adminPassword)
  }

  def logout() {
    driver.get(Urls.baseUrl + "/c/portal/logout")
  }


  private def login(name: String, password: String) {


    driver.get(Urls.baseUrl + "/c/portal/login")

    var loginField = $(By.xpath(".//input[contains(@id, 'LoginPortlet_login')]"))
    var passwordField = $(By.xpath(".//input[contains(@id, 'LoginPortlet_password')]"))
    var loginForm = $(by("type", "submit"))

    if (!loginField.exists()) {

      loginField = $(By.id("_58_login"))
      passwordField = $(By.id("_58_password"))
      loginForm = $(By.id("_58_fm"))
    }

    assert(loginField.exists(), "Field for login not found")
    assert(passwordField.exists(), "Field for password not found")
    assert(loginForm.exists(), "Button for login not found")

    loginField.clear()
    loginField.sendKeys(name)
    passwordField.clear()
    passwordField.sendKeys(password)
    loginForm.submit()

    $(".dashboard-page").waitUntil(not(visible), 1000, 10)
  }
}
