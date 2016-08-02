package com.arcusys.valamis.persistence.impl.scorm.model

case class AttemptModel(id: Option[Long],
                                userId: Long,
                                packageId: Long,
                                organizationId: String,
                                isComplete: Boolean)
