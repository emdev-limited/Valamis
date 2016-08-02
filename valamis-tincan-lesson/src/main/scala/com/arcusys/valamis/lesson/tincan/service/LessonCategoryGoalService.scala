package com.arcusys.valamis.lesson.tincan.service

import com.arcusys.valamis.lesson.tincan.model.LessonCategoryGoal

trait LessonCategoryGoalService {
  def get(packageId: Long): Seq[LessonCategoryGoal]

  def add(goals: Seq[LessonCategoryGoal]): Unit

  def delete(packageId: Long)
}
