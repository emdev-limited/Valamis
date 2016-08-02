package com.arcusys.valamis.lesson.scorm.model

case class ScormUser(id: Long,
                     name: String = "",
                     preferredAudioLevel: Float = 1,
                     preferredLanguage: String = "",
                     preferredDeliverySpeed: Float = 1,
                     preferredAudioCaptioning: Int = 0)
