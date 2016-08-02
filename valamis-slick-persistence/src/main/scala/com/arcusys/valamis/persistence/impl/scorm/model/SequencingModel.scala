package com.arcusys.valamis.persistence.impl.scorm.model

case class SequencingModel(id: Option[Long],
                           packageId: Option[Long],
                           activityId : Option[String],
                           sharedId:  Option[String],
                           sharedSequencingIdReference: Option[String],
                           cAttemptObjectiveProgressChild: Boolean,
                           cAttemptAttemptProgressChild: Boolean,
                           attemptLimit: Option[Int],
                           durationLimitInMilliseconds: Option[Long],
                           preventChildrenActivation: Boolean,
                           constrainChoice: Boolean)