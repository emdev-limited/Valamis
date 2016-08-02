package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.liferay.AssetHelper

class LessonAssetHelper extends AssetHelper[Lesson] {

  def updatePackageAssetEntry(lesson: Lesson): Long = {
    updateAssetEntry(
      lesson.id,
      Some(lesson.ownerId),
      Some(lesson.courseId),
      Some(lesson.title),
      Some(lesson.description),
      lesson,
      isVisible = lesson.isVisible.contains(true))
  }
}
