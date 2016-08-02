package com.arcusys.valamis.lesson.exception

import com.arcusys.valamis.exception.EntityNotFoundException

class NoLessonException(val id: Long) extends EntityNotFoundException(s"no lesson with id: $id")
