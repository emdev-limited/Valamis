package com.arcusys.valamis.reports.table

import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.persistence.common.SlickProfile

trait LessonTables
  extends LessonTableComponent
    with LessonGradeTableComponent
    with LessonAttemptsTableComponent
    with SlickProfile
