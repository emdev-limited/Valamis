package com.arcusys.valamis.slide.convert

trait PDFProcessor {
  def parsePDF(content: Array[Byte]): List[String]
}

