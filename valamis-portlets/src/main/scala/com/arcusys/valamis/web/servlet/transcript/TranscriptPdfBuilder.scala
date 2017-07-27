package com.arcusys.valamis.web.servlet.transcript

import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.servlet.ServletContext

trait TranscriptPdfBuilder {
  def build(companyId: Long,
            userId: Long,
            servletContext: ServletContext,
            locale: Locale): ByteArrayOutputStream

  def buildCertificate(userId: Long,
                       servletContext: ServletContext,
                       certificateId: Long,
                       companyId: Long,
                       locale: Locale
                      ): ByteArrayOutputStream
}