package com.arcusys.valamis.slick.util

import java.io.InputStream
import java.util.Properties

object SlickTestProperties {
  def apply(dbName: String): SlickTestProperties = {
    val properties = new Properties()

    properties.load(propertiesStream)

    new SlickTestProperties(dbName, properties)
  }

  private def propertiesStream: InputStream = {
    lazy val properties = Option(this.getClass.getClassLoader.getResourceAsStream("db.properties"))
    lazy val h2Properties = Option(this.getClass.getClassLoader.getResourceAsStream("db_h2.properties"))

    properties orElse h2Properties getOrElse(throw new Exception("db properties not found"))
  }
}

class SlickTestProperties(dbName: String, properties: Properties) {

  lazy val jdbcDriver = properties.getProperty("driver")

  lazy val userName = properties.getProperty("username")
  lazy val password = properties.getProperty("password")
  lazy val mainJdbcUrl = properties.getProperty("url")

  lazy val aliveConnection = Option(properties.getProperty("aliveConnection")).contains("true")

  lazy val testJdbcUrl = properties.getProperty("testUrl")
    .replace("{{DB_NAME}}", dbName)

  lazy val initDbSql = Option(properties.getProperty("createDB"))
    .map(q => q.replace("{{DB_NAME}}", dbName))

  lazy val dropDbSql = Option(properties.getProperty("dropDB"))
    .map(q => q.replace("{{DB_NAME}}", dbName))

}
