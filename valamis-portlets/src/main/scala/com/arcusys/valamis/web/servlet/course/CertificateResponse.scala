package com.arcusys.valamis.web.servlet.course

case class CertificateResponse(id: Long,
                               title: String,
                               isActive: Boolean,
                               logo: String,
                               description: String,
                               status: Option[String])