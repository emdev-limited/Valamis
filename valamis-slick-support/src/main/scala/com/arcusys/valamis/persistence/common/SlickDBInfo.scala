package com.arcusys.valamis.persistence.common

import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

trait SlickDBInfo {
  def slickDriver: JdbcDriver
  def slickProfile: JdbcProfile

  def databaseDef: JdbcBackend#DatabaseDef
}