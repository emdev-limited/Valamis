package com.arcusys.valamis.slide.service.export

import com.arcusys.valamis.slide.model.{Slide, SlideSetStatus}
import com.arcusys.valamis.tag.model.ValamisTag
import org.joda.time.DateTime

// Add new fields as Option for supporting old exports
case class SlideSetExportModel(id: Long = 0L,
                    title: String = "",
                    description: String = "",
                    courseId: Long,
                    logo: Option[String] = None,
                    slides: Seq[Slide] = Seq(),
                    isTemplate: Boolean = false,
                    isSelectedContinuity: Boolean = false,
                    themeId: Option[Long] = None,
                    duration: Option[Long] = None,
                    scoreLimit: Option[Double] = None,
                    playerTitle: String = "page",
                    slidesCount: Option[Long] = None,
                    topDownNavigation: Boolean = false,
                    activityId: String = "",
                    status: String = SlideSetStatus.Draft,
                    version: Double = 1.0,
                    modifiedDate: DateTime = new DateTime(),
                    oneAnswerAttempt: Boolean = false,
                    tags: Seq[ValamisTag] = Seq(),
                    requiredReview: Option[Boolean] = None)
