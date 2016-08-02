package com.arcusys.valamis.web.servlet.file.request

/**
 * Created by Iliya Tryapitsin on 17.03.14.
 */
object UploadContentType extends Enumeration {
  type UploadContentType = Value

  val Icon = Value("icon")
  val WebGLModel = Value("webgl")
  val RevealJs = Value("reveal")
  val Pdf = Value("pdf")
  val Pptx = Value("pptx")
  val Base64Icon = Value("base64-icon")
  val Package = Value("scorm-package")
  val DocLibrary = Value("document-library")
  val ImportLesson = Value("import-lesson")
  val ImportQuestion = Value("import-question")
  val ImportMoodleQuestion = Value("import-question-moodle")
  val ImportCertificate = Value("import-certificate")
  val ImportPackage = Value("import-package")
  val ImportSlideSet = Value("import-slide-set")
  val ImportFromPdf = Value("import-from-pdf")
  val ImportFromPptx = Value("import-from-pptx")
}