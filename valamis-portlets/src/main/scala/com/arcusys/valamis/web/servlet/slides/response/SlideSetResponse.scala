package com.arcusys.valamis.web.servlet.slides.response

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.tag.model.ValamisTag
import org.joda.time.DateTime

case class SlideSetResponse(id: Long,
                            title: String,
                            description: String,
                            courseId: Long,
                            logo: Option[String],
                            isTemplate: Boolean,
                            isSelectedContinuity: Boolean,
                            themeId: Option[Long],
                            duration: Option[Long],
                            scoreLimit: Option[Double],
                            playerTitle: String,
                            slidesCount: Option[Long],
                            topDownNavigation: Boolean,
                            activityId: String,
                            status: String,
                            version: Double,
                            modifiedDate: DateTime,
                            oneAnswerAttempt: Boolean,
                            tags: Seq[ValamisTag],
                            lockUserId: Option[Long],
                            lockDate: Option[DateTime],
                            requiredReview: Boolean,
                            lockUserName: Option[String]) {

  def this(slideSet: SlideSet,
           user: Option[LUser],
           slidesCount: Option[Long] = None,
           tags: Seq[ValamisTag] = Nil) = {
    this(
      slideSet.id,
      slideSet.title,
      slideSet.description,
      slideSet.courseId,
      slideSet.logo,
      slideSet.isTemplate,
      slideSet.isSelectedContinuity,
      slideSet.themeId,
      slideSet.duration,
      slideSet.scoreLimit,
      slideSet.playerTitle,
      slidesCount,
      slideSet.topDownNavigation,
      slideSet.activityId,
      slideSet.status,
      slideSet.version,
      slideSet.modifiedDate,
      slideSet.oneAnswerAttempt,
      tags,
      slideSet.lockUserId,
      slideSet.lockDate,
      slideSet.requiredReview,
      user.map(_.getFullName)
    )
  }
}

