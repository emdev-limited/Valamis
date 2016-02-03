package com.arcusys.valamis.certificate

import com.arcusys.valamis.model.{Order, SortBy}
import com.arcusys.valamis.certificate.model.CertificateSortBy

case class CertificateSort(sortBy: CertificateSortBy.CertificateSortBy, order: Order.Value) extends SortBy(sortBy, order)