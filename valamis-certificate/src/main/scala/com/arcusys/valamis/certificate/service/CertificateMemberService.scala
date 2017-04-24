package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.certificate.model.{CertificateState, CertificateUserStatus}
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.model.{RangeResult, SkipTake}

trait CertificateMemberService {

  def addMembers(certificateId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit

  def removeMembers(certificateId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit

  def getMembers(certificateId: Long,
                 viewerType: MemberTypes.Value,
                 nameFilter: Option[String],
                 ascending: Boolean,
                 skipTake: Option[SkipTake]): RangeResult[Member]

  def getUserMembers(certificateId: Long,
                     nameFilter: Option[String],
                     ascending: Boolean,
                     skipTake: Option[SkipTake],
                     organizationId: Option[Long]):  RangeResult[CertificateUserStatus]

  def getAvailableMembers(certificateId: Long,
                          viewerType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member]

  def getAvailableUserMembers(certificateId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              organizationId: Option[Long]): RangeResult[LUser]

  def delete(certificateId: Long): Unit

}
