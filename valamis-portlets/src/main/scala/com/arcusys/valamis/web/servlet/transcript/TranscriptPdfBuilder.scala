package com.arcusys.valamis.web.servlet.transcript

import java.io.ByteArrayOutputStream
import javax.servlet.ServletContext

import org.joda.time.DateTime

trait TranscriptPdfBuilder {
  def build(companyId: Long, userId: Long, servletContext: ServletContext): ByteArrayOutputStream

  def buildCertificate(userId: Long,
                       servletContext: ServletContext,
                       certificateId: Long,
                       companyId: Long): ByteArrayOutputStream
}