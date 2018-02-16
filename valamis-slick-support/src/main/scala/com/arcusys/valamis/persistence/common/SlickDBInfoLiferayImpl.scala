package com.arcusys.valamis.persistence.common

import java.sql.{Connection, SQLException}
import javax.sql.DataSource

import slick.driver.{JdbcDriver, JdbcProfile}

class SlickDBInfoLiferayImpl(dataSource: DataSource) extends SlickDBInfo {

  lazy val slickDriver = getSlickDriver(dataSource)
  lazy val slickProfile: JdbcProfile = slickDriver
  lazy val databaseDef = slickDriver.profile.backend.Database.forDataSource(dataSource)


  private def getSlickDriver(dataSource: DataSource): JdbcDriver = {

    val connection = dataSource.getConnection
    val (dbName, dbMajorVersion) = try {
      val metaData = connection.getMetaData

      (metaData.getDatabaseProductName, metaData.getDatabaseMajorVersion)
    } finally {
      closeConnection(connection)
    }

    getSlickDriver(dbName, dbMajorVersion)
  }

  private def closeConnection(connection: Connection): Unit = {
    try {
      Option(connection).foreach(_.close())
    } catch {
      case ex: SQLException => ex.printStackTrace()
    }
  }

  private def getSlickDriver(dbName: String, dbMajorVersion: Int): JdbcDriver = {
    if (dbName.startsWith("HSQL")) {
      slick.driver.HsqldbDriver
    } else if (dbName.startsWith("Microsoft") && (dbMajorVersion >= 9)) {
      // "Microsoft SQL Server" "Microsoft SQL Server Database"
      com.arcusys.slick.drivers.SQLServerDriver
    } else if (dbName.startsWith("Oracle") && (dbMajorVersion >= 10)) {
      com.arcusys.slick.drivers.OracleDriver
    } else if (dbName.equals("H2")) {
      slick.driver.H2Driver
    } else if (dbName.equals("MySQL")) {
      slick.driver.MySQLDriver
    } else if (dbName.equals("PostgreSQL")) {
      slick.driver.PostgresDriver
    } else if (dbName.equals("Apache Derby")) {
      slick.driver.DerbyDriver
    } else if (dbName.contains("Access")) {
      slick.driver.AccessDriver
    } // else if (dbName.equals("ASE") && (dbMajorVersion == 15)) {
    //  dialect = new SybaseASE157Dialect()
    // } else if (dbName.equals("Ingres") || dbName.equals("ingres") || dbName.equals("INGRES")) {
    //  scala.slick.driver.
    // } else if (dbName.equals("Sybase SQL Server")) {
    //   scala.slick.driver.
    // } else if (dbName.equals("Adaptive Server Enterprise")) {
    //   scala.slick.driver.
    // } else if (dbName.equals("Informix Dynamic Server")) {
    //   scala.slick.driver.
    // }
    else if (dbName.startsWith("DB2") && (dbMajorVersion >= 9)) {
      // "DB2/NT" "DB2/LINUX"  "DB2/6000" "DB2/HPUX" "DB2/SUN" "DB2/LINUX390" "DB2/AIX64"
      com.arcusys.slick.drivers.DB2Driver
    } else {
      throw new scala.RuntimeException("Unsupport database: " + dbName + " " + dbMajorVersion)
    }
  }
}
