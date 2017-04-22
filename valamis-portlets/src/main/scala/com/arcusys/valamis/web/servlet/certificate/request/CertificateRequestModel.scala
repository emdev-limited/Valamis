package com.arcusys.valamis.web.servlet.certificate.request

case class CertificateRequestModel(
  id: Option[Long],
  year: Option[String],
  title: String,
  description: String,
  userId: Long
)
