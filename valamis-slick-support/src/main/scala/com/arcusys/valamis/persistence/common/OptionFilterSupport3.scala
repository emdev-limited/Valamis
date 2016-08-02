package com.arcusys.valamis.persistence.common

import slick.driver.JdbcProfile

trait OptionFilterSupport3 {
  protected val driver: JdbcProfile
  import driver.api._

  def optionFilter[T:BaseColumnType](column: Rep[Option[T]], id: Option[T]): Rep[Option[Boolean]] = {
    id match {
      case Some(idValue) => column === idValue
      case None => column.isEmpty
    }
  }
}
