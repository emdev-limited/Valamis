package com.arcusys.learn.ioc

import com.arcusys.learn.PersistenceLFConfiguration
import com.arcusys.learn.controllers.api.social.{ActivityInterpreter, ActivityInterpreterImpl}
import com.arcusys.learn.facades._
import com.arcusys.learn.facades.certificate.CertificateFacade
import com.arcusys.learn.liferay.activity.{StatementActivityCreator, StatementActivityCreatorImpl}
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.notifications.MessageTemplateLoader
import com.arcusys.learn.notifications.services.ResourceTemplateLoader
import com.arcusys.learn.service.util.{ImageProcessor, ImageProcessorImpl}
import com.arcusys.learn.service.{GradeChecker, GradeCheckerImpl, SlickDBInfoLiferayImpl}
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.content.service._
import com.arcusys.valamis.content.storage._
import com.arcusys.valamis.course.{UserCourseResultServiceImpl, UserCourseResultService, CourseServiceImpl, CourseService}
import com.arcusys.valamis.file.service.{FileEntryService, FileEntryServiceImpl, FileService, FileServiceImpl}
import com.arcusys.valamis.grade.service.{CourseGradeService, CourseGradeServiceImpl, PackageGradeService, PackageGradeServiceImpl}
import com.arcusys.valamis.gradebook.service.{GradeBookService, GradeBookServiceImpl}
import com.arcusys.valamis.lesson.PackageChecker
import com.arcusys.valamis.lesson.generator.tincan.file.{TinCanRevealJSPackageGenerator, TinCanRevealJSPackageGeneratorContract}
import com.arcusys.valamis.lesson.scorm.service.sequencing._
import com.arcusys.valamis.lesson.service.{ActivityService, _}
import com.arcusys.valamis.lrs.service._
import com.arcusys.valamis.lrsEndpoint.service.{LrsEndpointService, LrsEndpointServiceImpl}
import com.arcusys.valamis.settings.service._
import com.arcusys.valamis.slide.convert._
import com.arcusys.valamis.slide.service._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.uri.service.{URIService, URIServiceContract}
import com.arcusys.valamis.user.service.{UserService, UserServiceImpl}
import com.arcusys.valamis.util.{CacheUtil, CacheUtilMultiVMPoolImpl}
import com.arcusys.valamis.social
import com.escalatesoft.subcut.inject.NewBindingModule

class WebConfiguration extends NewBindingModule(implicit module => {
    import module._

    module <~ new PersistenceSlickConfiguration(new SlickDBInfoLiferayImpl)
    module <~ new PersistenceLFConfiguration

    // -------------FACADES----------------------------------
    bind[CertificateFacadeContract].toSingle(new CertificateFacade)
    bind[FileFacadeContract].toSingle(new FileFacade)
    bind[QuestionFacadeContract].toSingle(new QuestionFacade)
    bind[GradebookFacadeContract].toSingle(new GradebookFacade)
    bind[ReportFacadeContract].toSingle(new ReportFacade)
    bind[CourseFacadeContract].toSingle(new CourseFacade)
    bind[UserFacadeContract].toSingle(new UserFacade)
    bind[PackageFacadeContract].toSingle(new PackageFacade)
    bind[TranscriptPrintFacadeContract].toSingle(new TranscriptPrintFacade)

    // END----------FACADES----------------------------------

    // -------------SERVICES----------------------------------
    bind[NavigationRequestServiceContract] toSingle new NavigationRequestService
    bind[TerminationRequestServiceContract] toSingle new TerminationRequestService
    bind[SequencingRequestServiceContract] toSingle new SequencingRequestService
    bind[DeliveryRequestServiceContract] toSingle new DeliveryRequestService
    bind[RollupServiceContract] toSingle new RollupService
    bind[EndAttemptServiceContract] toSingle new EndAttemptService

    // END----------SERVICES----------------------------------

    // -------------OTHER----------------------------------
    bind[UserLocalServiceHelper] toSingle UserLocalServiceHelper()
    bind[ScopePackageService].toSingle(new ScopePackageServiceImpl)
    bind[MessageTemplateLoader].toSingle(ResourceTemplateLoader)

    // END----------OTHER----------------------------------

    // -------------BL-SERVICES----------------------------------

    bind[CertificateService] toSingle new CertificateServiceImpl
    bind[CourseGradeService] toSingle new CourseGradeServiceImpl
    bind[FileService] toSingle new FileServiceImpl
    bind[GradeBookService] toSingle new GradeBookServiceImpl
    bind[LRSToActivitySettingService] toSingle new LRSToActivitySettingServiceImpl
    bind[TagServiceContract].toSingle(new TagService)
    bind[URIServiceContract].toSingle(new URIService)
    bind[UserService].toSingle(new UserServiceImpl)
    bind[ValamisPackageService].toSingle(new ValamisPackageServiceImpl)
    bind[FileEntryService].toSingle(new FileEntryServiceImpl)
    bind[SlideSetServiceContract].toSingle(new SlideSetService)
    bind[SlideServiceContract].toSingle(new SlideService)
    bind[SlideElementServiceContract].toSingle(new SlideElementService)
    bind[SlideThemeServiceContract].toSingle(new SlideThemeService)

    //tincan
    bind[LrsClientManager].toSingle(new LrsClientManagerImpl)
    bind[LrsRegistration].toSingle(new LrsRegistrationImpl)
    bind[LrsOAuthService].toSingle(new LrsOAuthServiceImpl)
    bind[CurrentUserCredentials].toSingle(new CurrentUserCredentials)

    //lesson
    bind[LrsEndpointService].toSingle(new LrsEndpointServiceImpl)
    bind[ActivityServiceContract].toSingle(new ActivityService)

    bind[SettingService] toSingle new SettingServiceImpl
    bind[SiteDependentSettingServiceImpl] toSingle new SiteDependentSettingServiceImpl

    bind[PresentationProcessor] toSingle new PresentationProcessorImpl
    bind[PDFProcessor] toSingle new PDFProcessorImpl
    bind[ImageProcessor] toSingle new ImageProcessorImpl

    bind[CourseService] toSingle new CourseServiceImpl
    bind[CourseGradeService] toSingle new CourseGradeServiceImpl
    bind[PackageGradeService] toSingle new PackageGradeServiceImpl
    bind[UserCourseResultService] toSingle new UserCourseResultServiceImpl
    bind[GradeChecker] toSingle new GradeCheckerImpl
    bind[PackageChecker] toSingle new PackageCheckerImpl

    // END----------BL-SERVICES----------------------------------

    // -------------OTHER----------------------------------

    bind[PackageUploadManager] toSingle new PackageUploadManager

    bind[CertificateStatusChecker] toSingle new CertificateStatusCheckerImpl

    bind[LessonLimitChecker].toSingle(new LessonLimitChecker)
    bind[LessonStatementReader].toSingle(new LessonStatementReader)
    bind[PlayerScopeRuleManager].toSingle(new PlayerScopeRuleManager)
    bind[TinCanRevealJSPackageGeneratorContract].toSingle(TinCanRevealJSPackageGenerator)

    bind[SlideSetExporterContract].toSingle(new SlideSetExporter)
    bind[SlideSetImporterContract].toSingle(new SlideSetImporter)
    bind[SlideSetPublisherContract].toSingle(new SlideSetPublisher)

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
})

