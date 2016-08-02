package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.certificate.model.CertificateMember
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

trait CertificateMemberTableComponent
  extends CertificateTableComponent
    with TypeMapper{ self: SlickProfile =>
  import driver.simple._

  implicit val memberTypeMapper = enumerationIdMapper(MemberTypes)

  class CertificateMemberTable(tag: Tag) extends Table[CertificateMember](tag, tblName("CERTIFICATE_MEMBER")) {
    def certificateId = column[Long]("CERTIFICATE_ID")
    def memberId = column[Long]("MEMBER_ID")
    def memberType = column[MemberTypes.Value]("MEMBER_TYPE")

    def * = (certificateId, memberId, memberType) <> (CertificateMember.tupled, CertificateMember.unapply)
  }

  val certificateMembers = TableQuery[CertificateMemberTable]
}
