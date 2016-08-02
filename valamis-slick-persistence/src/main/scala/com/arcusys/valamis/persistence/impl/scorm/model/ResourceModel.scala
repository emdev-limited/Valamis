package com.arcusys.valamis.persistence.impl.scorm.model

case class ResourceModel(id: Option[Long],
                                packageId: Option[Long],
                                scormType: String,
                                resourceId: Option[String],
                                href: Option[String],
                                base: Option[String])


