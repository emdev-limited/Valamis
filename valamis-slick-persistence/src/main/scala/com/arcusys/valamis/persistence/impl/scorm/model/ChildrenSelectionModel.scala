package com.arcusys.valamis.persistence.impl.scorm.model

import com.arcusys.valamis.lesson.scorm.model.manifest.RandomizationTimingType.RandomizationTimingType

case class ChildrenSelectionModel(id: Option[Long],
                                  sequencingId : Long,
                                  takeCount: Option[Int],
                                  takeTimingOnEachAttempt: Option[RandomizationTimingType],
                                  reorderOnEachAttempt: Option[RandomizationTimingType])
