package com.arcusys.valamis.updaters.version310.model.certificate

object CertificateStatuses extends Enumeration {
  val InProgress = Value("InProgress")
  val Failed = Value("Failed")
  val Success = Value("Success")
  val Overdue = Value("Overdue")

  def inProgressAndSuccess = Set(InProgress, Success)
  def all = Set(InProgress, Failed, Success, Overdue)
}
