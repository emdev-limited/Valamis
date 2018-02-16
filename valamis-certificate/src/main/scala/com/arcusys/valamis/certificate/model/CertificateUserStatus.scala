package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.user.model.User

case class CertificateUserStatus(user: User,
                                 status: Option[CertificateState] = None)