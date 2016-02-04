package com.arcusys.valamis.certificate.model

/**
 * Created by Iliya Tryapitsin on 14.03.14.
 */


object CertificateStateType extends Enumeration {
  type CertificateStateType = Value
  val Publish = Value(2)
  val Unpublish = Value(1)
}
