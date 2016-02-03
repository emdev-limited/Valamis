package com.arcusys.valamis.user.model

case class User(id: Long, name: String)

case class ScormUser(id: Long,
  name: String = "",
  preferredAudioLevel: Float = 1,
  preferredLanguage: String = "",
  preferredDeliverySpeed: Float = 1,
  preferredAudioCaptioning: Int = 0)
