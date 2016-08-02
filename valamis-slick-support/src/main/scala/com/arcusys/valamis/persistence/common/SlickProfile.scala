package com.arcusys.valamis.persistence.common

import slick.driver.JdbcProfile

trait SlickProfile {
  val driver: JdbcProfile
}
