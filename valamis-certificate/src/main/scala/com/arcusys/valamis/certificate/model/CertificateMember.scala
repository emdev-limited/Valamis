package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.member.model.MemberTypes

case class CertificateMember (certificateId: Long, memberId: Long, memberType: MemberTypes.Value)
