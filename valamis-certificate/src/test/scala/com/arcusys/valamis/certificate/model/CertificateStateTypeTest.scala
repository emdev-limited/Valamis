package com.arcusys.valamis.certificate.model

import org.scalatest.FunSuite

class CertificateStateTypeTest extends FunSuite {

  test("class name, like in stored social activities") {
    val className = CertificateStateType.getClass.getName

    assert(className == "com.arcusys.valamis.certificate.model.CertificateStateType$")
  }

  test("publish id, like in stored social activities") {
    val publishId = CertificateStateType.Publish.id

    assert(publishId == 2)
  }

}
