package com.arcusys.valamis.persistence.common

import com.arcusys.slick.drivers.{DB2Driver, OracleDriver, SQLServerDriver}
import slick.ast.ColumnOption
import slick.driver._

object DbNameUtils {

  val NameSizeLimit = 30

  def checkLengthAndReturn(name: String) = {
    assert(name.length <= NameSizeLimit, s"Name '$name' is too long: ${name.length}")
    name
  }

  def tblName(str: String): String = checkLengthAndReturn(s"LEARN_$str")

  def fkName(str: String) = checkLengthAndReturn(s"FK_$str")

  def idxName(str: String) = checkLengthAndReturn(s"IDX_$str")

  def pkName(str: String) = checkLengthAndReturn(s"PK_$str")

  def likePattern(str: String) = s"%$str%"

  // UUID not supported in Postgres < 4.3
  def uuidKeyLength = "char(36)"



  def varCharMax(implicit driver: JdbcProfile) = driver match {
    case driver: MySQLDriver => "text"
    case driver: PostgresDriver => "varchar(10485760)"
    case driver: SQLServerDriver => s"varchar(max)"
    case driver: OracleDriver => s"VARCHAR2(4000)"
    case driver: DB2Driver => "CLOB"
    case _ => "varchar(2147483647)"
  }

  def varCharPk(implicit driver: JdbcProfile) = driver match {
    case driver: MySQLDriver => "varchar(255)"
    case driver: PostgresDriver => "varchar(10485760)"
    case driver: SQLServerDriver => s"varchar(max)"
    case _ => "varchar(255)"
  }

  def binaryOptions[T](implicit driver: JdbcProfile): List[ColumnOption[T]] = {
    val O = driver.columnOptions
    driver match {
      case MySQLDriver => List(O.SqlType("LONGBLOB"))
      case PostgresDriver => List(O.SqlType("bytea"))
      case DB2Driver => List(O.SqlType("BLOB(134217728)"))//128MB
      case _ => List()
    }
  }

  val idName = "ID"
}
