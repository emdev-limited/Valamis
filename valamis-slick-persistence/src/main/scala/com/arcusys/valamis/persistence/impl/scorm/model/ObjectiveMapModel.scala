package com.arcusys.valamis.persistence.impl.scorm.model

case class ObjectiveMapModel(id: Option[Long],
                                objectiveId: Long,
                                readSatisfiedStatusFrom: Option[String],
                                readNormalizedMeasureFrom: Option[String],
                                writeSatisfiedStatusTo: Option[String],
                                writeNormalizedMeasureTo: Option[String],
                                readRawScoreFrom: Option[String],
                                readMinScoreFrom: Option[String],
                                readMaxScoreFrom: Option[String],
                                readCompletionStatusFrom: Option[String],
                                readProgressMeasureFrom: Option[String],
                                writeRawScoreTo: Option[String],
                                writeMinScoreTo: Option[String],
                                writeMaxScoreTo: Option[String],
                                writeCompletionStatusTo: Option[String],
                                writeProgressMeasureTo: Option[String])
