package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.export.service.ExportService
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.liferay.{CacheUtil, CacheUtilMultiVMPoolImpl}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.reports.service.{LearningPatternReportService, LearningPatternReportServiceImpl}
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.slide.convert.PresentationProcessor
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.slide.service._
import com.arcusys.valamis.social
import com.arcusys.valamis.statements.StatementChecker
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.web.listener.LessonListener
import com.arcusys.valamis.web.service._
import com.arcusys.valamis.web.service.export.ExportServiceImpl
import com.arcusys.valamis.web.servlet.course.{CourseFacade, CourseFacadeContract}
import com.arcusys.valamis.web.servlet.file.{FileFacade, FileFacadeContract}
import com.arcusys.valamis.web.servlet.grade.notification.GradebookNotificationHelper
import com.arcusys.valamis.web.servlet.grade.{GradebookFacade, GradebookFacadeContract}
import com.arcusys.valamis.web.servlet.user.{UserFacade, UserFacadeContract}
import com.escalatesoft.subcut.inject.NewBindingModule

class WebConfiguration(dbInfo: => SlickDBInfo) extends NewBindingModule(fn = implicit module => {
  import module._

  module <~ new CommonConfiguration(dbInfo)
  module <~ new PersistenceSlickConfiguration(dbInfo)
  module <~ new LrsSupportConfiguration(dbInfo)
  module <~ new LessonConfiguration(dbInfo)
  module <~ new GradebookConfiguration(dbInfo)
  module <~ new CertificateConfiguration(dbInfo)
  module <~ new QuestionBankConfiguration(dbInfo)
  module <~ new SlideConfiguration(dbInfo)
  module <~ new TranscriptConfiguration
  module <~ new StoryTreeConfiguration(dbInfo)

  bind[GradebookNotificationHelper] toSingle new GradebookNotificationHelper {
    val lessonService = inject[LessonService](None)
  }

  bind[LearningPatternReportService] toSingle new LearningPatternReportServiceImpl {
    val lessonService = inject[LessonService](None)
    val statementReader = inject[LessonStatementReader](None)
    val teacherGradeService = inject[TeacherLessonGradeService](None)
    val tincanLessonService = inject[TincanPackageService](None)
    val userResult = inject[UserLessonResultService](None)
    val lessonPlayerService = inject[LessonPlayerService](None)
  }

  bind[ImageProcessor] toSingle new ImageProcessorImpl
  bind[PresentationProcessor] toSingle new PresentationProcessorImpl

  // -------------FACADES----------------------------------
  bind[FileFacadeContract].toSingle(new FileFacade)
  bind[GradebookFacadeContract].toSingle(new GradebookFacade)
  bind[CourseFacadeContract].toSingle(new CourseFacade)
  bind[UserFacadeContract].toSingle(new UserFacade)

  // END----------FACADES----------------------------------

  // -------------OTHER----------------------------------
  bind[UserLocalServiceHelper] toSingle UserLocalServiceHelper()

  // END----------OTHER----------------------------------

  // -------------BL-SERVICES----------------------------------


  bind[TagService[SlideSet]].toSingle(new TagService[SlideSet])
  bind[ExportService].toSingle(new ExportServiceImpl)


  //tincan
  bind[StatementChecker].toSingle(new StatementCheckerImpl {
    lazy val gradeChecker = Configuration.inject[GradeChecker](None)
  })

  // END----------BL-SERVICES----------------------------------

  // -------------OTHER----------------------------------

  bind[StatementActivityCreator].toSingle(new StatementActivityCreatorImpl)

  bind[social.service.CommentService].toSingle(new social.service.CommentServiceImpl)
  bind[social.service.LikeService].toSingle(new social.service.LikeServiceImpl)
  bind[social.service.ActivityService].toSingle(new social.service.ActivityServiceImpl)
  bind[ActivityInterpreter].toSingle(new ActivityInterpreterImpl)

  bind[CacheUtil].toSingle(new CacheUtilMultiVMPoolImpl)

  bind[SlideSetAssetHelper] toSingle new SlideSetAssetHelperImpl
  bind[MemberService] toSingle new MemberService
  bind[LessonListener] toSingle new LessonListener {
    lazy val lessonService = inject[LessonService](None)
    lazy val lessonGradeService = inject[LessonGradeService](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val userCourseService = inject[UserCourseResultService](None)
    lazy val gradeService = inject[LessonGradeService](None)
  }
})

