package com.arcusys.valamis.core

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

trait SlickDBInfo {
  def slickDriver: JdbcDriver
  def slickProfile: JdbcProfile

  def databaseDef: JdbcBackend#DatabaseDef
}