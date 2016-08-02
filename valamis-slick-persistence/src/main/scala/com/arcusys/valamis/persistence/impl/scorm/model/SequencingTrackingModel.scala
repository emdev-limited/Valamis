package com.arcusys.valamis.persistence.impl.scorm.model

case class SequencingTrackingModel(
                          id: Option[Long],
                          sequencingId: Long,
                          completionSetByContent: Option[Boolean],
                          objectiveSetByContent: Option[Boolean])