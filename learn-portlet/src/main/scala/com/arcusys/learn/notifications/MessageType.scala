package com.arcusys.learn.notifications

object MessageType extends Enumeration {
  type MessageType = Value

  val CourseCertificateExpiration = Value("course_expiration")
  val CourseCertificateDeadline = Value("course_deadline")
  val EnrolledStudent = Value("enrolled_student")
  val FinishedLearningModule = Value("finished_learning_module")
}
