package com.arcusys.valamis.web.service

import java.awt.geom.{AffineTransform, Rectangle2D}
import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, InputStream}

import com.arcusys.learn.liferay.util.Base64Helper
import com.arcusys.valamis.lesson.generator.tincan.file.TinCanRevealJSPackageGenerator
import com.arcusys.valamis.slide.convert.PresentationProcessor
import com.arcusys.valamis.util.mustache.Mustache
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.sl.usermodel.Slide
import org.apache.poi.xslf.usermodel.XMLSlideShow

import scala.collection.JavaConverters._

class PresentationProcessorImpl extends PresentationProcessor {

  private lazy val pptxForegroundTemplate =
    new Mustache(scala.io.Source.fromInputStream(getResourceInputStream("tincan/pptx-foreground-iframe.html")).mkString)
  private lazy val pptxTemplate =
    new Mustache(scala.io.Source.fromInputStream(getResourceInputStream("tincan/pptx.html")).mkString)
  private lazy val indexTemplate =
    new Mustache(scala.io.Source.fromInputStream(getResourceInputStream("tincan/revealjs.html")).mkString)

  private def getResourceInputStream(name: String) = Thread.currentThread.getContextClassLoader.getResourceAsStream(name)

  override def convert(stream: InputStream, fileName: String): Seq[ByteArrayOutputStream] = {
    getPages(stream, fileName, img => {
      val out = new ByteArrayOutputStream
      javax.imageio.ImageIO.write(img, "png", out) //jpg takes more memory(space).
      out
    }
    )
  }


  private def getPages[A](stream: InputStream, fileName: String, mapper: BufferedImage => A): List[A] = {
    val slideShow =
      try {
        if (fileName.toUpperCase.endsWith("PPTX")) {
          new XMLSlideShow(stream)
        } else {
          new HSLFSlideShow(stream)
        }
      } finally {
        stream.close()
      }
    val pageSize = slideShow.getPageSize

    val zoom = 4 //Just a magic number to enlarge resolution number.
    val affine = new AffineTransform()
    affine.setToScale(zoom, zoom)

    slideShow.getSlides.asScala
      .map { slide =>
        val img =
          new BufferedImage(
            Math.ceil(pageSize.width * zoom).toInt,
            Math.ceil(pageSize.height * zoom).toInt,
            BufferedImage.TYPE_INT_RGB
          )
        val graphics = img.createGraphics()
        graphics.setTransform(affine)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        //clear the drawing area
        graphics.setPaint(Color.white)
        graphics.fill(new Rectangle2D.Float(0, 0, pageSize.width, pageSize.height))
        //render
        slide.asInstanceOf[Slide[_, _]].draw(graphics)
        mapper(img)
      }
      .toList
  }

  override def parsePPTX(content: Array[Byte], fileName: String): List[String]= {
    val pages = getPages(new ByteArrayInputStream(content), fileName, img => img)
    Base64Helper.encodeImagesToBase64(pages)
  }

  override def processPresentation(name: String, stream: InputStream, packageTitle: String, packageDescription: String, originalFileName: String): File = {

    val indexedImages = convert(stream, originalFileName).zipWithIndex

    val imageSupplementaries = indexedImages.foldLeft(List[(String, InputStream)]()) {
      case (acc, (slide, i)) =>
        val slideName = s"slide-${i + 1}.png"
        val pptxForeground = pptxForegroundTemplate.render(Map("file" -> slideName))
        val pptxForegroundFileName = s"pptx-foreground-iframe-${i}.html"

        ("files/quizData/" + slideName -> new ByteArrayInputStream(slide.toByteArray)) ::
          (pptxForegroundFileName -> new ByteArrayInputStream(pptxForeground.getBytes)) ::
          acc
    }

    val imageSections = indexedImages.map {
      case (slide, i) =>
        pptxTemplate.render(Map("id" -> i, "title" -> s"slide-${i + 1}"))
    }.mkString("\n")

    val index = new ByteArrayInputStream(indexTemplate.render(Map("sections" -> imageSections, "title" -> name)).getBytes)

    TinCanRevealJSPackageGenerator.composePackage(("index.html" -> index) :: imageSupplementaries, s"http://valamislearning.com/presentation/${name}", name, packageDescription)
  }
}
