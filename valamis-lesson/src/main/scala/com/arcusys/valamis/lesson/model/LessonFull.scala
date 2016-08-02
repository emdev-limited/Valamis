package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.model.User

case class LessonFull(lesson: Lesson,
                      limit: Option[LessonLimit],
                      tags: Seq[ValamisTag],
                      owner: Option[User])
