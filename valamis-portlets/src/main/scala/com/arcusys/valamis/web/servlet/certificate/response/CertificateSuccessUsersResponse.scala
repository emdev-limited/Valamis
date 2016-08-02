package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.user.model.User

case class CertificateSuccessUsersResponse(
  id: Long,
  title: String,
  shortDescription: String,
  description: String,
  logo: String,
  succeedUsers: Seq[User])

