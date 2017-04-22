package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.certificate.model.CertificateUserStatus
import com.arcusys.valamis.certificate.storage.{CertificateMemberRepository, CertificateStateRepository}
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.user.service.UserService

abstract class CertificateMemberServiceImpl extends CertificateMemberService {

  def certificateMemberRepository: CertificateMemberRepository

  def memberService: MemberService

  def certificateStateRepository: CertificateStateRepository

  def userService: UserService

  override def addMembers(certificateId: Long,
                          memberIds: Seq[Long],
                          memberType: MemberTypes.Value): Unit = {

    certificateMemberRepository.addMembers(certificateId, memberIds, memberType)
  }

  override def getMembers(certificateId: Long,
                          memberType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member] = {

    lazy val companyId = CompanyHelper.getCompanyId
    val memberIds = certificateMemberRepository.getMemberIds(certificateId, memberType)

    if (memberIds.isEmpty) {
      RangeResult(0, Nil)
    }
    else {
      memberService.getMembers(memberIds, true, memberType, companyId, nameFilter, ascending, skipTake)
    }
  }

  override def getAvailableMembers(certificateId: Long,
                                   memberType: MemberTypes.Value,
                                   nameFilter: Option[String],
                                   ascending: Boolean,
                                   skipTake: Option[SkipTake]): RangeResult[Member] = {
    val companyId = CompanyHelper.getCompanyId
    val memberIds = certificateMemberRepository.getMemberIds(certificateId, memberType)

    memberService.getMembers(memberIds, false, memberType, companyId, nameFilter, ascending, skipTake)

  }

  override def removeMembers(certificateId: Long,
                             memberIds: Seq[Long],
                             memberType: MemberTypes.Value): Unit = {
    certificateMemberRepository.deleteMembers(certificateId, memberIds, memberType)
  }

  override def getUserMembers(certificateId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              orgId: Option[Long]): RangeResult[CertificateUserStatus] = {

    val memberIds = certificateMemberRepository.getMemberIds(certificateId, MemberTypes.User)

    val items = skipTake
      .map(r => memberIds.slice(r.skip, r.take))
      .getOrElse(memberIds)
      .map { id =>
        val user = userService.getWithDeleted(id)
        if (user.isDeleted) {
          CertificateUserStatus(user)
        } else {
          CertificateUserStatus(user, certificateStateRepository.getBy(user.id, certificateId))
        }
      }

    RangeResult(memberIds.size, items)
  }

  override def getAvailableUserMembers(certificateId: Long,
                                       nameFilter: Option[String],
                                       ascending: Boolean,
                                       skipTake: Option[SkipTake],
                                       orgId: Option[Long]): RangeResult[LUser] = {
    val companyId = CompanyHelper.getCompanyId
    val memberIds = certificateMemberRepository.getMemberIds(certificateId, MemberTypes.User)

    memberService.getUserMembers(memberIds, false, companyId, nameFilter, ascending, skipTake, orgId)
  }

  override def delete(certificateId: Long): Unit = certificateMemberRepository.delete(certificateId)
}