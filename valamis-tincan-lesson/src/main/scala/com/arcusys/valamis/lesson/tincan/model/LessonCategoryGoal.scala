package com.arcusys.valamis.lesson.tincan.model

case class LessonCategoryGoal(lessonId: Long,
                              name: String,
                              category: String,
                              count: Int,
                              id: Option[Long] = None)
