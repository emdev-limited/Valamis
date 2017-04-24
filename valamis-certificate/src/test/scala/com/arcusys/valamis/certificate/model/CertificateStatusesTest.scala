package com.arcusys.valamis.certificate.model

import org.scalatest.FunSuite

class CertificateStatusesTest extends FunSuite {

  test("status id, like in stored db and social activities") {
    assert(CertificateStatuses.InProgress.id == 0)
    assert(CertificateStatuses.Failed.id == 1)
    assert(CertificateStatuses.Success.id == 2)
    assert(CertificateStatuses.Overdue.id == 3)
  }

}
