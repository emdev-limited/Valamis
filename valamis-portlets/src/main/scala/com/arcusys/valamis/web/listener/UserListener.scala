package com.arcusys.valamis.web.listener

import com.arcusys.learn.liferay.LiferayClasses.{LBaseModelListener, LUser}
import com.arcusys.learn.liferay.model.LGroup
import com.arcusys.valamis.certificate.model.{CertificateItemsCount, CertificateStatuses}
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateUserService}
import com.arcusys.valamis.certificate.storage.{CertificateMemberRepository, CertificateRepository}
import com.arcusys.valamis.course.CourseNotificationService
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.lesson.service.LessonMembersService
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class UserListener extends LBaseModelListener[LUser] with Injectable {
  implicit lazy val bindingModule = Configuration

  private lazy val certificateMemberRepository = inject[CertificateMemberRepository]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val courseNotificationService = inject[CourseNotificationService]

  private val classNameToMemberType = Map(
    // TODO make it cool for LR7
    "com.liferay.portal.model.Organization" -> MemberTypes.Organization,
    "com.liferay.portal.model.UserGroup" -> MemberTypes.UserGroup,
    "com.liferay.portal.model.Role" -> MemberTypes.Role
  )

  override def onAfterAddAssociation(classPK: AnyRef,
                                     associationClassName: String,
                                     associationClassPK: AnyRef): Unit = {
    lazy val userId = classPK.toString.toLong

    for {
      memberType <- classNameToMemberType.get(associationClassName)
      memberId = associationClassPK.toString.toLong
    } {
      certificateMemberRepository.getMembers(memberId, memberType)
        .foreach(m => {
          val (certificate, counts) = certificateRepository.getByIdWithItemsCount(m.certificateId)
            .getOrElse(throw new EntityNotFoundException(s"no certificate with id: ${m.certificateId}"))

          val isJoined = certificateUserService.hasUser(userId, m.certificateId)

          if (!isJoined) {
            val status = counts match {
              case CertificateItemsCount(_, 0, 0, 0, 0, 0, _) if certificate.isActive =>
                CertificateStatuses.Success
              case _ =>
                CertificateStatuses.InProgress
            }

            certificateUserService.addUser(userId, status, certificate)
          }
        })
    }
    if (LGroup.getGroupClass.getName.equals(associationClassName)) {
      // user was added to a group, send notification
      val courseId = associationClassPK.toString.toLong
      courseNotificationService.sendUsersAdded(courseId, Seq(userId), MemberTypes.User)
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
          certificateUserService.deleteMembers(m.certificateId, Seq(classPK.toString.toLong), MemberTypes.User)
        })
    }
  }
}