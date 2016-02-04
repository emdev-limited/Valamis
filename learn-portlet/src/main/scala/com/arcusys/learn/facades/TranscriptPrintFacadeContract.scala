package com.arcusys.learn.facades

import java.io.ByteArrayOutputStream

trait TranscriptPrintFacadeContract {
  def printTranscript(companyID: Int, userID: Int, templatesPath: String): ByteArrayOutputStream
}