package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model.{Certificate, CertificateState, CertificateStatuses}
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import org.joda.time.DateTime

trait CertificateUserService {

  // todo refactor and move to open badges servlet
  def getUserOpenBadges(companyId: Long,
                        userId: Long): Seq[Certificate]
}