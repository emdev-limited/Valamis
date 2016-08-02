package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.CertificateMember
import com.arcusys.valamis.member.model.MemberTypes

trait CertificateMemberRepository {

  def addMembers(certificateId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit

  def deleteMembers(certificateId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit

  def getMemberIds(certificateId: Long, memberType: MemberTypes.Value): Seq[Long]

  def getMembers(memberId: Long, memberType: MemberTypes.Value): List[CertificateMember]

  def delete(certificateId: Long): Unit

}
