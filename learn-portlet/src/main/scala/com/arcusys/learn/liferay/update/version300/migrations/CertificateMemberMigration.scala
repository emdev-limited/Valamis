package com.arcusys.learn.liferay.update.version300.migrations


import com.arcusys.learn.liferay.update.version300.certificate.{CertificateMemberTableComponent, CertificateStateTableComponent}
import com.arcusys.valamis.certificate.model.CertificateMember
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateMemberMigration(val db: JdbcBackend#DatabaseDef,
                                 val driver: JdbcProfile)
extends CertificateMemberTableComponent
  with CertificateStateTableComponent
  with SlickProfile {

  import driver.simple._


  def getOldData(implicit session: JdbcBackend#Session) = {
    certificateStates.list
  }

  def toNewData(entity: CertificateState): CertificateMember = {
    CertificateMember(
      entity.certificateId,
      entity.userId,
      MemberTypes.User
    )
  }

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      certificateMembers.ddl.create
      val newRows = getOldData map toNewData
      if (newRows.nonEmpty) certificateMembers ++= newRows
    }
  }

}
