package com.arcusys.valamis.slide.convert

import java.io.ByteArrayInputStream

import com.arcusys.valamis.slide.service.SlideServiceContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.{ImageType, PDFRenderer}

trait PDFProcessor {
  def parsePDF(content: Array[Byte], slideId: Long, slideSetId: Long): List[(Long, String)]
}

class PDFProcessorImpl(implicit val bindingModule: BindingModule)
  extends PDFProcessor
  with Injectable {

  private lazy val slideService = inject[SlideServiceContract]

  private val Scale = 2
  private val ImageTpe = ImageType.RGB

  def parsePDF(content: Array[Byte], slideId: Long, slideSetId: Long): List[(Long, String)] = {
    val input = new ByteArrayInputStream(content)
    val pdf = PDDocument.load(input)
    val renderer = new PDFRenderer(pdf)
    try {
      val pages = (0 until pdf.getNumberOfPages)
        .map(renderer.renderImage(_, Scale, ImageTpe))
        .toList

      slideService.addSlidesToSlideSet(slideId, slideSetId, pages)
    }
    finally {
      pdf.close()
    }
  }
}