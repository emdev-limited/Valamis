package com.arcusys.valamis.lesson.service


class GradeVerifier {

  def verify(grade: Option[Float]) = {
    grade.collect {
      case g if g > 1 => throw new IllegalArgumentException("Grade should be in the range from 0 to 1")
      case g => g
    }
  }
}
