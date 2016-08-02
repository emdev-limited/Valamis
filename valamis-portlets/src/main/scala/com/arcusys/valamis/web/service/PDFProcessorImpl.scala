package com.arcusys.valamis.web.service

import java.io.ByteArrayInputStream

import com.arcusys.learn.liferay.util.Base64Helper
import com.arcusys.valamis.slide.convert.PDFProcessor
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.{ImageType, PDFRenderer}

class PDFProcessorImpl extends PDFProcessor {

  private val Scale = 1.5f // <- soon to be moved to properties file
  private val ImageTpe = ImageType.RGB

  override def parsePDF(content: Array[Byte]): List[String]= {
    val input = new ByteArrayInputStream(content)
    val pdf = PDDocument.load(input)
    val renderer = new PDFRenderer(pdf)
    try {
      val pages = (0 until pdf.getNumberOfPages)
        .map(renderer.renderImage(_, Scale, ImageTpe))
        .toList

      Base64Helper.encodeImagesToBase64(pages)
    }
    finally {
      pdf.close()
    }
  }
}
