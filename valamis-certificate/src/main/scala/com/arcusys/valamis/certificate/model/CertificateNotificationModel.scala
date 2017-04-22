package com.arcusys.valamis.certificate.model

case class CertificateNotificationModel(
                                         messageType: String,
                                         certificateTitle: String,
                                         certificateLink: String,
                                         userId: Long)
