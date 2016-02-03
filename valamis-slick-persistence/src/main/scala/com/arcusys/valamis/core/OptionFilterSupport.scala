package com.arcusys.valamis.core

import scala.slick.driver.JdbcProfile

trait OptionFilterSupport {
  protected val driver: JdbcProfile
  import driver.simple._

  def optionFilter[T:BaseColumnType](column: Column[Option[T]], id: Option[T]): Column[Option[Boolean]] = {
    id match {
      case Some(idValue) => column === idValue
      case None => column.isEmpty
    }
  }
}
