package com.arcusys.valamis.updaters.version330.schema3312

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait CertificateTableComponent { self: SlickProfile =>

  import driver.api._

  class CertificateTable(tag: Tag) extends Table[Long](tag, tblName("CERTIFICATE")) {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    override def *  = id
  }

  val certificates = TableQuery[CertificateTable]
}