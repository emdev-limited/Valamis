package com.arcusys.valamis.slide.service

import javax.servlet.ServletContext

import com.arcusys.valamis.lesson.model.Lesson

trait SlideSetPublishService {
  def findPublishedLesson(slideSetId: Long, userId: Long): Lesson

  def publish(servletContext: ServletContext, id: Long, userId: Long, courseId: Long): Unit
}
