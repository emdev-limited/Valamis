package com.arcusys.learn.liferay.util

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import com.liferay.portal.kernel.util.Base64

object Base64Helper {
  def decode(data: String): Array[Byte] = Base64.decode(data)
  def encode(content: Array[Byte]):String = Base64.encode(content)
  def stringToObject(data: String): Any = Base64.stringToObject(data)
  def objectToString(data: Any): String = Base64.objectToString(data)

  def encodeImagesToBase64(images: List[BufferedImage]):  List[String] = {
    for (image <- images) yield {
      encodeImageToBase64(image)
    }
  }

  def encodeImageToBase64(image: BufferedImage): String = {
    val ImageFormat = "png"

    val outputStream = new ByteArrayOutputStream
    try {
      ImageIO.write( image, ImageFormat, outputStream )
      encode(outputStream.toByteArray)
    }
    finally {
      if (outputStream != null) outputStream.close()
    }
  }
}
