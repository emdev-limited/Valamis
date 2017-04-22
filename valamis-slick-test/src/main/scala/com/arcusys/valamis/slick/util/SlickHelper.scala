package com.arcusys.valamis.slick.util

import com.arcusys.slick.drivers.{SQLServerDriver, DB2Driver}
import slick.driver.{H2Driver, JdbcDriver, MySQLDriver, PostgresDriver}

object SlickHelper {
  def getSlickDriver(jdbcDriver: String): JdbcDriver = {

    // we should warm up driver class
    Class.forName(jdbcDriver)

    jdbcDriver match {
      case "org.h2.Driver" => H2Driver
      case "org.postgresql.Driver" => PostgresDriver
      case "com.ibm.db2.jcc.DB2Driver" => DB2Driver
      case "com.mysql.jdbc.Driver" => MySQLDriver
      case "net.sourceforge.jtds.jdbc.Driver" => SQLServerDriver
    }
  }
}
