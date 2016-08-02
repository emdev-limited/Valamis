package com.arcusys.valamis.web.listener

import com.arcusys.learn.liferay.LiferayClasses.{LBaseModelListener, LUser}
import com.arcusys.valamis.certificate.model.{CertificateItemsCount, CertificateState, CertificateStatuses}
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.certificate.storage.{CertificateMemberRepository, CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.DateTime

class UserListener extends LBaseModelListener[LUser] with Injectable {
  implicit lazy val bindingModule = Configuration

  private lazy val certificateStateRepository = inject[CertificateStateRepository]
  private lazy val certificateMemberRepository = inject[CertificateMemberRepository]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateService = inject[CertificateService]

  private val classNameToMemberType = Map(
    "com.liferay.portal.model.Organization" -> MemberTypes.Organization,
    "com.liferay.portal.model.UserGroup" -> MemberTypes.UserGroup,
    "com.liferay.portal.model.Role" -> MemberTypes.Role
  )

  override def onAfterAddAssociation(classPK: AnyRef,
                                     associationClassName: String,
                                     associationClassPK: AnyRef): Unit = {

    for {
      memberType <- classNameToMemberType.get(associationClassName)
    } {
      certificateMemberRepository.getMembers(associationClassPK.toString.toLong, memberType)
        .foreach(m => {
          val (certificate, counts) = certificateRepository.getByIdWithItemsCount(m.certificateId)
            .getOrElse(throw new EntityNotFoundException(s"no certificate with id: ${m.certificateId}"))

          val exists = certificateStateRepository.getBy(classPK.toString.toLong, certificate.id).nonEmpty

          if (!exists) {
            val status = counts match {
              case CertificateItemsCount(_, 0, 0, 0, 0, 0) if certificate.isPublished =>
                CertificateStatuses.Success
              case _ =>
                CertificateStatuses.InProgress
            }
            certificateStateRepository.create(
              CertificateState(
                classPK.toString.toLong,
                status,
                new DateTime(),
                new DateTime(),
                certificate.id
              ))

            if (certificate.isPublished) {
              certificateService.addPackageGoalState(certificate.id, classPK.toString.toLong)
              certificateService.addAssignmentGoalState(certificate.id, classPK.toString.toLong)
            }
          }
        })
    }
  }


  override def onAfterRemoveAssociation(classPK: AnyRef,
                                        associationClassName: String,
                                        associationClassPK: AnyRef): Unit = {

    for {
      memberType <- classNameToMemberType.get(associationClassName)
    } {
      certificateMemberRepository.getMembers(associationClassPK.toString.toLong, memberType)
        .foreach(m => {
          certificateService.deleteMembers(m.certificateId, Seq(classPK.toString.toLong), MemberTypes.User)
        })
    }
  }
}

