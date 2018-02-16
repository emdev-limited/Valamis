package com.arcusys.valamis.slide.service

import java.io.InputStream

import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.slide.model.{SlideSetSort, SlideSet}

trait SlideSetService {
  def getById(id: Long): SlideSet

  def getLogo(id: Long): Option[Array[Byte]]

  def setLogo(id: Long, name: String, content: Array[Byte])

  def deleteLogo(id: Long)

  def getSlideSets(courseId: Long,
                   titleFilter: Option[String],
                   sortBy: SlideSetSort,
                   skipTake: Option[SkipTake],
                   isTemplate: Boolean): RangeResult[SlideSet]

  def delete(id: Long)

  def clone(id: Long,
            isTemplate: Boolean,
            fromTemplate: Boolean,
            title: String,
            description: String,
            logo: Option[String],
            newVersion: Option[Boolean] = None): SlideSet

  def exportSlideSet(id: Long): InputStream

  def importSlideSet(stream: InputStream, scopeId: Int)

  def updateInfo(id: Long, title: String, description: String, tags: Seq[String]): Unit

  def updateSettings(id: Long,
                     isSelectedContinuity: Boolean,
                     themeId: Option[Long],
                     slideSetDuration: Option[Long],
                     scoreLimit: Option[Double],
                     playerTitle: String,
                     topDownNavigation: Boolean,
                     oneAnswerAttempt: Boolean,
                     requiredReview: Boolean): Unit

  def create(slideSetModel: SlideSet, tags: Seq[String]): SlideSet

  def createWithDefaultSlide(slideSetModel: SlideSet, tags: Seq[String]): SlideSet

  def getVersions(id: Long): Seq[SlideSet]

  def deleteAllVersions(id: Long)

  def createNewActivityId(courseId: Long): String

  def changeLockStatus(slideSetId: Long, userId: Option[Long])
}