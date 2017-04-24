package com.arcusys.valamis.lesson.service


object GradeVerifier {

  def verify(grade: Float): Unit = {
    if (grade.isNaN || grade < 0 || grade > 1) {
      throw new IllegalArgumentException("Grade should be in the range from 0 to 1")
    }
  }
}
