package com.arcusys.learn


import com.arcusys.learn.service.{AntiSamyHelper, ResourceReaderImpl}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slide.convert.PDFProcessor
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service._
import com.escalatesoft.subcut.inject.NewBindingModule

class AdditionalConfiguration extends NewBindingModule(fn = implicit module => {

  module.bind[GradeChecker] toSingle new GradeCheckerImpl

  module.bind[Sanitizer] toSingle AntiSamyHelper

  module.bind[PDFProcessor] toSingle new PDFProcessorImpl

  module.bind[ResourceReader].toSingle(new ResourceReaderImpl)

})
