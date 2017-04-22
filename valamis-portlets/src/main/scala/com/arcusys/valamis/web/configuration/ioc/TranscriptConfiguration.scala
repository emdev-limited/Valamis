package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.certificate.service.{AssignmentService, CertificateUserService}
import com.arcusys.valamis.certificate.storage.{CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.gradebook.service.{GradeBookService, LessonGradeService}
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.service.TranscriptPdfBuilderImpl
import com.arcusys.valamis.web.servlet.course.CourseFacadeContract
import com.arcusys.valamis.web.servlet.transcript.TranscriptPdfBuilder
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class TranscriptConfiguration(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[TranscriptPdfBuilder] toSingle new TranscriptPdfBuilderImpl {
    lazy val gradeBookService = inject[GradeBookService](None)
    lazy val certificateUserService = inject[CertificateUserService](None)
    lazy val courseFacade = inject[CourseFacadeContract](None)
    lazy val userService = inject[UserService](None)
    lazy val certificateStateRepository = inject[CertificateStateRepository](None)
    lazy val uriService = inject[TincanURIService](None)
    lazy val lessonGradeService = inject[LessonGradeService](None)
    lazy val assignmentService = inject[AssignmentService](None)
    lazy val resourceReader = Configuration.inject[ResourceReader](None)
    lazy val certificateRepository = inject[CertificateRepository](None)
  }

})
