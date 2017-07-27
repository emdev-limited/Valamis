package com.arcusys.valamis.web.servlet.public

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

import com.arcusys.valamis.file.model.FileRecord
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.service.export.PackageExportProcessor
import com.arcusys.valamis.web.servlet.file.request.FileExportRequest
import com.escalatesoft.subcut.inject.NewBindingModule

/**
  * Created by pkornilov on 2/22/17.
  */
class LessonServletGetPackageTest extends LessonServletBaseTest {

  val _fileStorage = mock[FileStorage]
  _bindingModule <~ new NewBindingModule(fn = implicit module => {
    module.bind[PackageExportProcessor] toSingle new PackageExportProcessor {
      lazy val fileService = mock[FileService]
      lazy val fileStorage = _fileStorage
    }
  })

  test("should fail for a lesson that doesn't exist") {
    getWithStatusAssert("/lessons/1/package", 404) {
      parse(body) shouldBe noLessonResponse(1)
    }
  }

  test("should download package for a lesson") {
    val creationDate = createSampleLesson()

    (_fileStorage.getFiles _).expects("data/1/").returns {
      Seq(
        FileRecord("index.html", Some(Array(1,2,3).map(_.toByte))),
        FileRecord("styles.css", Some(Array(4,5,6).map(_.toByte)))
      )
    }

    getWithStatusAssert("/lessons/1/package", 200) {
      header("Content-Type") shouldBe "application/zip; charset=UTF-8"
      header("Content-Disposition") shouldBe
        s"attachment; filename=package_1_${creationDate.toString("YYYY-MM-dd_HH-mm-ss")}" +
          s"${FileExportRequest.ExportExtension}"

      val zipStream = new ZipInputStream(new ByteArrayInputStream(bodyBytes))
      zipStream.getNextEntry.getName shouldBe "index.html"
      zipStream.getNextEntry.getName shouldBe "styles.css"
      zipStream.getNextEntry shouldBe null

    }
  }

}
