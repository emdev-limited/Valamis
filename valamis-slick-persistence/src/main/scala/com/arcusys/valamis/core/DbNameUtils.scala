package com.arcusys.valamis.core

import com.arcusys.slick.drivers.SQLServerDriver

import scala.slick.ast.ColumnOption
import scala.slick.driver._

object DbNameUtils {
  def tblName(str: String) = s"LEARN_${str}"
  def fkName(str: String) = s"FK_$str"
  def idxName(str: String) = s"IDX_$str"
  def pkName(str: String) = s"PK_$str"

  def likePattern(str: String) = s"%${str}%"

  // UUID not supported in Postgres < 4.3
  def uuidKeyLength = "char(36)"



  def varCharMax(implicit driver: JdbcProfile) = driver match {
    case driver: MySQLDriver => "text"
    case driver: PostgresDriver => "varchar(10485760)"
    case driver: SQLServerDriver => s"varchar(max)"
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
      case MySQLDriver => List(O.DBType("LONGBLOB"))
      case PostgresDriver => List(O.DBType("bytea"))
      case _ => List()
    }
  }

  val idName = "ID"
}
