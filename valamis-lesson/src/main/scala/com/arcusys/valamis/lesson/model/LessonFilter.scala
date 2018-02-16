package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.lesson.model.LessonType.LessonType


/**
  * Created by mminin on 17.02.16.
  */
case class LessonFilter(courseIds: Seq[Long],
                        lessonType: Option[LessonType],
                        onlyVisible: Boolean = false,
                        title: Option[String] = None,
                        tagId: Option[Long] = None)
