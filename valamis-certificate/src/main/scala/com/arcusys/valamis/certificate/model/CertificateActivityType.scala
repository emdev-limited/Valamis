package com.arcusys.valamis.certificate.model

// Not forget add same in learningPaths
object CertificateActivityType extends Enumeration{
  val UserJoined = Value(0, "UserJoined")
  val Achieved = Value(1, "Success") // like was with old certificates
  val Published = Value(2, "Published")
  val Expired = Value(3, "Overdue")
  val Failed = Value(4, "Failed")
}
