package com.arcusys.valamis.user.service

trait UserCertificateRepository {
  def getUsersBy(certificateId: Long): Seq[Long]
}