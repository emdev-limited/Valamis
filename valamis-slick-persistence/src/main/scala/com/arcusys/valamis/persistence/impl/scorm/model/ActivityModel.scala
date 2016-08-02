package com.arcusys.valamis.persistence.impl.scorm.model

case class ActivityModel(indexNumber: Long,
                                id: Option[String],
                                packageId: Option[Long],
                                organizationId: String,
                                parentId: Option[String],
                                title: String,
                                identifierRef: Option[String],
                                resourceParameters: Option[String],
                                hideLMSUI: Option[String],
                                visible: Boolean,
                                objectivesGlobalToSystem: Option[Boolean],
                                sharedDataGlobalToSystem: Option[Boolean],
                                masteryScore: Option[String],
                                maxTimeAllowed: Option[String])




