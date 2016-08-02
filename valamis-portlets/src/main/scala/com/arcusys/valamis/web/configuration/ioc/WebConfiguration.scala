package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.service.{AssignmentService, AssignmentServiceImpl, CertificateStatusChecker}
import com.arcusys.valamis.content.service._
import com.arcusys.valamis.course._
import com.arcusys.valamis.file.service.{FileEntryService, FileEntryServiceImpl, FileService, FileServiceImpl}
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.lesson.generator.tincan.file.{TinCanRevealJSPackageGenerator, TinCanRevealJSPackageGeneratorContract}
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.liferay.{CacheUtil, CacheUtilMultiVMPoolImpl}
import com.arcusys.valamis.lrs.service._
import com.arcusys.valamis.lrs.service.util.StatementChecker
import com.arcusys.valamis.lrsEndpoint.service.{LrsEndpointService, LrsEndpointServiceImpl}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.settings.service._
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.slide.model.SlideSetModel
import com.arcusys.valamis.slide.service._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.uri.service.{TincanURIService, TincanURIServiceImpl}
import com.arcusys.valamis.user.service.{UserService, UserServiceImpl}
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.configuration.SlickDBInfoLiferayImpl
import com.arcusys.valamis.web.listener.LessonListener
import com.arcusys.valamis.web.service._
import com.arcusys.valamis.web.servlet.certificate.facade.{CertificateFacade, CertificateFacadeContract}
import com.arcusys.valamis.web.servlet.course.{CourseFacade, CourseFacadeContract}
import com.arcusys.valamis.web.servlet.file.{FileFacade, FileFacadeContract}
import com.arcusys.valamis.web.servlet.grade.{GradebookFacade, GradebookFacadeContract}
import com.arcusys.valamis.web.servlet.report.{ReportFacade, ReportFacadeContract}
import com.arcusys.valamis.web.servlet.user.{UserFacade, UserFacadeContract}
import com.arcusys.valamis.social
import com.escalatesoft.subcut.inject.NewBindingModule

class WebConfiguration extends NewBindingModule(fn = implicit module => {
  import module._

  val dbInfo = new SlickDBInfoLiferayImpl
  module <~ new PersistenceSlickConfiguration(dbInfo)
  module <~ new LessonConfiguration(dbInfo)
  module <~ new GradebookConfiguration(dbInfo)
  module <~ new CertificateConfiguration(dbInfo)

  bind[ImageProcessor] toSingle new ImageProcessorImpl

  // -------------FACADES----------------------------------
  bind[CertificateFacadeContract].toSingle(new CertificateFacade)
  bind[FileFacadeContract].toSingle(new FileFacade)
  bind[GradebookFacadeContract].toSingle(new GradebookFacade)
  bind[ReportFacadeContract].toSingle(new ReportFacade)
  bind[CourseFacadeContract].toSingle(new CourseFacade)
  bind[UserFacadeContract].toSingle(new UserFacade)

  // END----------FACADES----------------------------------

  // -------------OTHER----------------------------------
  bind[UserLocalServiceHelper] toSingle UserLocalServiceHelper()

  // END----------OTHER----------------------------------

  // -------------BL-SERVICES----------------------------------

  bind[FileService] toSingle new FileServiceImpl
  bind[LRSToActivitySettingService] toSingle new LRSToActivitySettingServiceImpl
  bind[TincanURIService].toSingle(new TincanURIServiceImpl)
  bind[UserService].toSingle(new UserServiceImpl)
  bind[FileEntryService].toSingle(new FileEntryServiceImpl)
  bind[SlideSetServiceContract].toSingle(new SlideSetService)
  bind[SlideServiceContract].toSingle(new SlideService {
    lazy val presentationProcessor = Configuration.inject[PresentationProcessor](None)
    lazy val pdfProcessor = Configuration.inject[PDFProcessor](None)
  })
  bind[SlideElementServiceContract].toSingle(new SlideElementService)
  bind[SlideThemeServiceContract].toSingle(new SlideThemeService)
  bind[TagService[SlideSetModel]].toSingle(new TagService[SlideSetModel])
  bind[TagService[LGroup]].toSingle(new TagService[LGroup])

  //tincan
  bind[LrsClientManager].toSingle(new LrsClientManagerImpl)
  bind[StatementChecker].toSingle(new StatementCheckerImpl {
    lazy val gradeChecker = Configuration.inject[GradeChecker](None)
  })
  bind[LrsRegistration].toSingle(new LrsRegistrationImpl)
  bind[LrsOAuthService].toSingle(new LrsOAuthServiceImpl)
  bind[UserCredentialsStorage].toSingle(new UserCredentialsStorageImpl)

  //lesson
  bind[LrsEndpointService].toSingle(new LrsEndpointServiceImpl)

  bind[SettingService] toSingle new SettingServiceImpl

  bind[CourseService] toSingle new CourseServiceImpl
  bind[CourseMemberService] toSingle new CourseMemberServiceImpl

  // END----------BL-SERVICES----------------------------------

  // -------------OTHER----------------------------------

  bind[TinCanRevealJSPackageGeneratorContract].toSingle(TinCanRevealJSPackageGenerator)

  bind[SlideSetExporterContract].toSingle(new SlideSetExporter)
  bind[SlideSetImporterContract].toSingle(new SlideSetImporter)
  bind[SlideSetPublisherContract].toSingle(new SlideSetPublisher{
    lazy val resourceReader = Configuration.inject[ResourceReader](None)
  })

  bind[StatementActivityCreator].toSingle(new StatementActivityCreatorImpl)

  bind[social.service.CommentService].toSingle(new social.service.CommentServiceImpl)
  bind[social.service.LikeService].toSingle(new social.service.LikeServiceImpl)
  bind[social.service.ActivityService].toSingle(new social.service.ActivityServiceImpl)
  bind[ActivityInterpreter].toSingle(new ActivityInterpreterImpl)

  bind[CacheUtil].toSingle(new CacheUtilMultiVMPoolImpl)

  bind[CategoryService] toSingle new CategoryServiceImpl
  bind[ContentService] toSingle new ContentServiceImpl
  bind[PlainTextService] toSingle new PlainTextServiceImpl
  bind[QuestionService] toSingle new QuestionServiceImpl

  bind[AssignmentService] toSingle new AssignmentServiceImpl

  bind[SlideSetAssetHelper] toSingle new SlideSetAssetHelperImpl
  bind[MemberService] toSingle new MemberService
  bind[LessonListener] toSingle new LessonListener {
    lazy val lessonService = inject[LessonService](None)
    lazy val lessonGradeService = inject[LessonGradeService](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val certificateChecker = inject[CertificateStatusChecker](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val userCourseService = inject[UserCourseResultService](None)
    lazy val gradeService = inject[LessonGradeService](None)
  }

})

