package com.arcusys.valamis.web.service

import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, RenderingHints}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import org.slf4j.LoggerFactory

trait ImageProcessor {
  def resizeImage(data: Array[Byte], maxWidth: Int, maxHeight: Int): Array[Byte]
}

class ImageProcessorImpl extends ImageProcessor {

  private lazy val log = LoggerFactory.getLogger(getClass)

  def resizeImage(data: Array[Byte], maxWidth: Int, maxHeight: Int): Array[Byte] = {
    val in = new ByteArrayInputStream(data)

    try {
      val out = new ByteArrayOutputStream()

      val original = ImageIO.read(in)
      if (original.getWidth < maxWidth || original.getHeight < maxHeight) {
        data
      }
      else {
        //Resize image to make one of dimension match maximum
        val ratio = Math.min(
          maxWidth / original.getWidth.toDouble,
          maxHeight / original.getHeight.toDouble)

        val newWidth = Math.floor(original.getWidth * ratio).toInt
        val newHeight = Math.floor(original.getHeight * ratio).toInt
        val resized = resizeImageToDimensions(original, newWidth, newHeight)

        val imageFormat = if(resized.getColorModel.hasAlpha) "PNG" else "JPG"
        ImageIO.write(resized, imageFormat, out)
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
