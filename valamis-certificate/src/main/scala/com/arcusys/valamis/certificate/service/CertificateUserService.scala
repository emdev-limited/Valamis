package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter, CertificateState, CertificateStatuses}
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import org.joda.time.DateTime

trait CertificateUserService {
  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]): RangeResult[Certificate]

  def getCertificatesByUserWithOpenBadgesAndDates(companyId: Long,
                                                  userId: Long
                                                   ): Seq[(Certificate, Option[CertificateState])]

  def getByUser(userId: Long,
                filter: CertificateFilter,
                isAchieved: Option[Boolean] = None,
                skipTake: Option[SkipTake]): RangeResult[Certificate]

  def getSuccessByUser(userId: Long, companyId: Long, titlePattern: Option[String] = None): Seq[Certificate]

  def getAvailableCertificates(userId:Long,
                               filter: CertificateFilter,
                               skipTake: Option[SkipTake]): RangeResult[Certificate]

  def getUsers(c: Certificate): Seq[(DateTime, LUser)]

  def getWithStates(userId: Long,
                    companyId: Long,
                    scopeId: Option[Long],
                    statuses: Set[CertificateStatuses.Value]): Seq[(Certificate, CertificateState)]

  def hasUser(certificateId: Long, userId: Long): Boolean

  def addMembers(certificateId: Long,
                 memberIds: Seq[Long],
                 memberType: MemberTypes.Value,
                 isCurrentUser: Boolean = false)

  def addUser(userId: Long,
              userStatus: CertificateStatuses.Value,
              certificate: Certificate,
              isCurrentUser: Boolean = false): Unit

  def addUserMember(certificateId: Long,
                    userId: Long,
                    courseId: Long)

  def isUserJoined(certificateId: Long, userId: Long): Boolean

  def deleteMembers(certificateId: Long,
                    memberIds: Seq[Long],
                    memberType: MemberTypes.Value): Unit

  def deleteUser(userId: Long, certificateId: Long): Unit
}