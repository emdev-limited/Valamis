package com.arcusys.valamis.persistence.impl.scorm.model

case class ScormUserModel(userId: Long,
                          name: String,
                          preferredAudioLevel: Option[Double],
                          preferredLanguage: Option[String],
                          preferredDeliverySpeed: Option[Double],
                          preferredAudioCaptioning: Option[Int])
