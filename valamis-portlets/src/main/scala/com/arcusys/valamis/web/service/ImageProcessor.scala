package com.arcusys.valamis.web.service

import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, RenderingHints}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import javax.imageio.ImageIO

import org.slf4j.LoggerFactory

trait ImageProcessor {
  def resizeImage(data: Array[Byte], maxWidth: Int, maxHeight: Int): Array[Byte]
}

class ImageProcessorImpl extends ImageProcessor {

  private lazy val log = LoggerFactory.getLogger(getClass)

  private def read(is: InputStream): (BufferedImage, String) = {
    val imageStream = ImageIO.createImageInputStream(is)

    val reader = {
      val readers = ImageIO.getImageReaders(imageStream)

      if (!readers.hasNext) throw new Exception("unsupported image")

      readers.next
    }

    val img = try {
      reader.setInput(imageStream, true, true)
      reader.read(0, reader.getDefaultReadParam)
    }
    finally {
      reader.dispose()
      imageStream.close()
    }

    (img, reader.getFormatName)
  }

  def resizeImage(data: Array[Byte], maxWidth: Int, maxHeight: Int): Array[Byte] = {
    val in = new ByteArrayInputStream(data)

    try {
      val out = new ByteArrayOutputStream()

      val (originalImage, imageFormat) = read(in)
      if (originalImage.getWidth < maxWidth || originalImage.getHeight < maxHeight) {
        data
      }
      else {
        //Resize image to make one of dimension match maximum
        val ratio = Math.min(
          maxWidth / originalImage.getWidth.toDouble,
          maxHeight / originalImage.getHeight.toDouble)

        val newWidth = Math.floor(originalImage.getWidth * ratio).toInt
        val newHeight = Math.floor(originalImage.getHeight * ratio).toInt
        val resizedImage = resizeImageToDimensions(originalImage, newWidth, newHeight)

        ImageIO.write(resizedImage, imageFormat, out)
        out.toByteArray
      }
    }
    catch {
      case e: Exception =>
        log.debug(e.getMessage)
        data
    }
    finally {
      in.close()
    }
  }

  private def resizeImageToDimensions(original: BufferedImage, width: Int, height: Int) = {

    val imageType = if(original.getColorModel.hasAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
    val resized = new BufferedImage(width, height, imageType)

    val g = resized.createGraphics()
    g.setComposite(AlphaComposite.Src)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON)
    g.drawImage(original, 0, 0, width, height, null)
    g.dispose()

    resized
  }
}
