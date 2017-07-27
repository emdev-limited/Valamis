package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage.{CertificateRepository}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.learn.liferay.services._
import com.liferay.portal.kernel.util.PrefsPropsUtil
import org.joda.time.DateTime

abstract class CertificateUserServiceImpl extends CertificateUserService {

  def certificateBadgeService: CertificateBadgeService

  // todo refactor and move to open badges servlet
  override def getUserOpenBadges(companyId: Long, userId: Long): Seq[Certificate] = {
    certificateBadgeService.getOpenBadges(userId, companyId, None).zipWithIndex
      .map { case (badge, index) => badge.copy(id = -index - 1) }
  }
}
