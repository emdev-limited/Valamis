package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.certificate.model.CertificateState
import com.arcusys.valamis.certificate.storage.{CertificateMemberRepository, CertificateStateRepository}
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}


class CertificateMemberServiceImpl(implicit val bindingModule: BindingModule)
  extends CertificateMemberService
    with Injectable {

  private lazy val certificateMemberRepository = inject[CertificateMemberRepository]
  private lazy val memberService = inject[MemberService]
  private lazy val certificateStateRepository = inject[CertificateStateRepository]

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

    if (memberIds.isEmpty){
      RangeResult(0, Nil)
    }
    else{
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
                              orgId: Option[Long]): RangeResult[(LUser, CertificateState)] = {

    lazy val companyId = CompanyHelper.getCompanyId
    val memberIds = certificateMemberRepository.getMemberIds(certificateId, MemberTypes.User)

    if (memberIds.isEmpty){
      RangeResult(0, Nil)
    }
    else {
      memberService.getUserMembers(memberIds, true, companyId, nameFilter, ascending, skipTake, orgId)
        .map(user =>
          (user, certificateStateRepository.getBy(user.getUserId, certificateId)
            .getOrElse(throw new EntityNotFoundException(s"no certificate state with id: $certificateId and user ${user.getUserId}"))))
    }
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