package com.arcusys.valamis.persistence.impl.scorm.model

case class SeqPermissionsModel(id: Option[Long],
                     sequencingId: Long,
                     choiceForChildren: Option[Boolean],
                     choiceForNonDescendants: Option[Boolean],
                     flowForChildren: Option[Boolean],
                     forwardOnlyForChildren: Option[Boolean])


