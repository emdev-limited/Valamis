package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.CertificateMember
import com.arcusys.valamis.certificate.storage.CertificateMemberRepository
import com.arcusys.valamis.certificate.storage.schema.CertificateMemberTableComponent
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class CertificateMemberRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateMemberRepository
    with CertificateMemberTableComponent
    with SlickProfile {

  import driver.simple._


  override def addMembers(certificateId: Long,
                          memberIds: Seq[Long],
                          memberType: MemberTypes.Value): Unit = {

    val members = memberIds.map(CertificateMember(certificateId, _, memberType))
    deleteMembers(certificateId, memberIds, memberType)
    db.withTransaction { implicit s =>
      certificateMembers ++= members
    }
  }

  override def deleteMembers(certificateId: Long,
                             memberIds: Seq[Long],
                             memberType: MemberTypes.Value): Unit = {
    db.withTransaction { implicit s =>
      certificateMembers
        .filter(m => m.certificateId === certificateId && m.memberType === memberType && (m.memberId inSet memberIds))
        .delete
    }
  }

  override def getMemberIds(certificateId: Long, memberType: MemberTypes.Value): Seq[Long] = {
    db.withSession { implicit s =>
      certificateMembers.filter(m => m.certificateId === certificateId && m.memberType === memberType)
        .map(_.memberId)
        .list
    }
  }

  override def getMembers(memberId: Long, memberType: MemberTypes.Value): List[CertificateMember] = {
    db.withSession { implicit s =>
      certificateMembers.filter(m => m.memberId === memberId && m.memberType === memberType)
        .list
    }
  }

  override def delete(certificateId: Long):Unit ={
    db.withTransaction { implicit s =>
      certificateMembers
        .filter(_.certificateId === certificateId)
        .delete
    }
  }
}
