package com.arcusys.valamis.slide.service

import java.io.InputStream
import javax.servlet.ServletContext

import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.slide.model.SlideSetModel

trait SlideSetServiceContract {
  def getById(id: Long): Option[SlideSetModel]

  def getLogo(slideSetId: Long): Option[Array[Byte]]

  def setLogo(slideSetId: Long, name: String, content: Array[Byte])

  def getSlideSets(courseId: Long,
                   titleFilter: Option[String],
                   sortTitleAsc: Boolean,
                   skipTake: SkipTake,
                   isTemplate: Option[Boolean]): List[SlideSetModel]

  def getSlideSetsCount(titleFilter: Option[String], courseId: Long, isTemplate: Option[Boolean]): Int

  def delete(id: Long)

  def clone(id: Long,
            isTemplate: Boolean,
            fromTemplate: Boolean,
            title: String,
            description: String,
            logo: Option[String],
            newVersion: Option[Boolean] = None)

  def findSlideLesson(slideSetId: Long, userId: Long): Lesson

  def publishSlideSet(servletContext: ServletContext, id: Long, userId: Long, courseId: Long): Unit

  def exportSlideSet(id: Long): InputStream

  def importSlideSet(stream: InputStream, scopeId: Int)

  def update(slideSetModel: SlideSetModel, tags: Seq[String]): SlideSetModel

  def updateWithVersion(slideSetModel: SlideSetModel, tags: Seq[String]): Option[SlideSetModel]

  def create(slideSetModel: SlideSetModel, tags: Seq[String]): SlideSetModel

  def createWithDefaultSlide(slideSetModel: SlideSetModel, tags: Seq[String]): SlideSetModel

  def getVersions(id: Long): List[SlideSetModel]

  def deleteAllVersions(id: Long)

  def createNewActivityId(courseId: Long): String
}