package com.arcusys.valamis.certificate.model

import org.scalatest.FunSuite

class CertificateTest extends FunSuite {

  test("class name, like in stored social activities") {
    val className = classOf[Certificate].getName

    assert(className == "com.arcusys.valamis.certificate.model.Certificate")
  }

}
