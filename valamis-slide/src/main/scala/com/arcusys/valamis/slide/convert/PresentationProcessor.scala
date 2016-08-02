package com.arcusys.valamis.slide.convert

import java.io.{ByteArrayOutputStream, File, InputStream}

trait PresentationProcessor {
  def convert(stream: InputStream, fileName: String): Seq[ByteArrayOutputStream]

  def parsePPTX(content: Array[Byte], fileName: String):  List[String]

  def processPresentation(name: String, stream: InputStream, packageTitle: String, packageDescription: String, originalFileName: String): File
}

