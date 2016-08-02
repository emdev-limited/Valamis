package com.arcusys.learn

import com.arcusys.learn.facade.{TranscriptPrintFacade, TranscriptPrintFacadeContract}
import com.arcusys.learn.service.{AntiSamyHelper, ResourceReaderImpl}
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.service._
import com.escalatesoft.subcut.inject.NewBindingModule

class AdditionalConfiguration extends NewBindingModule(fn = implicit module => {

  module.bind[TranscriptPrintFacadeContract] toSingle new TranscriptPrintFacade

  module.bind[GradeChecker] toSingle new GradeCheckerImpl

  module.bind[Sanitizer] toSingle AntiSamyHelper

  module.bind[PresentationProcessor] toSingle new PresentationProcessorImpl
  module.bind[PDFProcessor] toSingle new PDFProcessorImpl

  module.bind[ResourceReader].toSingle(new ResourceReaderImpl)

})
