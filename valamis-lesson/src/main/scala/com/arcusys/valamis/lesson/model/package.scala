package com.arcusys.valamis.lesson

package object model {
  implicit class LessonExt(val lesson: Lesson) extends AnyVal {
    def getLessonStatus(lessonResult: UserLessonResult,
                        teacherGrade: Option[Float]
                       ) : Option[LessonStates.Value] = {

      lazy val state = if (lessonResult.isFinished) Some(LessonStates.Finished)
      else if (lessonResult.isSuspended) Some(LessonStates.Suspended)
      else if (lessonResult.attemptsCount > 0) Some(LessonStates.Attempted)
      else None

      teacherGrade match {
        case Some(grade) if grade > lesson.scoreLimit || (grade - lesson.scoreLimit).abs < 0.001 => Some(LessonStates.Finished)
        case Some(grade) if grade < lesson.scoreLimit => Some(LessonStates.Attempted)
        case None if lesson.requiredReview && state.isDefined => Some(LessonStates.InReview)
        case None if lesson.requiredReview && state.isEmpty => None
        case None if !lesson.requiredReview => state
      }
    }
  }
}
