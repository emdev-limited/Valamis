package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.lesson.model.LessonStates.LessonState
import com.arcusys.valamis.ratings.model.Rating
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.model.User

case class LessonWithPlayerState(lesson: Lesson,
                                 limit: Option[LessonLimit],
                                 tags: Seq[ValamisTag],
                                 owner: Option[User],
                                 rating: Rating,
                                 state: Option[LessonState],
                                 attemptsCount: Int,
                                 suspendedId: Option[String] = None)
