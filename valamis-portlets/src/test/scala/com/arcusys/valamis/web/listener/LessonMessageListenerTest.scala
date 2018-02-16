package com.arcusys.valamis.web.listener

import org.specs2.mutable._


class LessonMessageListenerTest extends Specification {

  "LessonActions" should {
    import LessonActions._

    "parse legacy values" in {
      """ parse "Check" """ in {
        withName("Check") mustEqual IsDeployed
      }

      """ parse "LessonStatus" """ in {
        withName("LessonStatus") mustEqual GetLessonStatus
      }

      """ parse "LessonNames" """ in {
        withName("LessonNames") mustEqual GetLessonNames
      }

      """ parse "UploadPackage" """ in {
        withName("UploadPackage") mustEqual UploadPackage
      }
    }
  }

}
