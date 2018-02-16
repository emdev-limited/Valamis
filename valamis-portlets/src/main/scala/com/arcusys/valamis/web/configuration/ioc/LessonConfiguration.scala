package com.arcusys.valamis.web.configuration.ioc

import java.io.File

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.reports.DateReport
import com.arcusys.valamis.certificate.service.{LearningPathService, LearningPathServiceImpl}
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.lesson.model.{Lesson, LessonType, LessonPlayer, UserLessonResult}
import com.arcusys.valamis.lesson.scorm.service._
import com.arcusys.valamis.lesson.scorm.service.sequencing._
import com.arcusys.valamis.lesson.scorm.storage.{ActivityStorage, ResourcesStorage}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lesson.service.export.{PackageExportProcessor, PackageImportProcessor, PackageMobileExportProcessor}
import com.arcusys.valamis.lesson.service.impl._
import com.arcusys.valamis.lesson.tincan.service._
import com.arcusys.valamis.liferay.{AssetHelper, SocialActivityHelper}
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.reports.model.{TopLesson, TopLessonConfig}
import com.arcusys.valamis.reports.service.{ReportService, ReportServiceImpl}
import com.arcusys.valamis.slide.convert.PresentationProcessor
import com.arcusys.valamis.slide.service.PresentationPackageUploader
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.listener.LessonListener
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 26.02.16.
  */
class LessonConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[LessonNotificationService] toSingle new LessonNotificationServiceImpl {
    lazy val lessonService = inject[LessonService](None)
  }

  bind[TincanPackageService] toSingle new TincanPackageServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val fileStorage = inject[FileStorage](None)
  }

  bind[ScormPackageService] toSingle new ScormPackageServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val uriService = inject[TincanURIService](None)
    lazy val fileStorage = inject[FileStorage](None)
  }

  bind[LessonCategoryGoalService].toSingle {
    new LessonCategoryGoalServiceImpl(db.databaseDef, db.slickProfile)
  }

  bind[RatingService[Lesson]] toSingle new RatingService[Lesson]
  bind[LessonAssetHelper] toSingle new LessonAssetHelper
  bind[LessonMembersService] toSingle new LessonMembersServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val memberService = inject[MemberService](None)
    lazy val userService = inject[UserService](None)
  }
  bind[LessonLimitService] toSingle new LessonLimitServiceImpl(db.databaseDef, db.slickProfile)
  bind[TagService[Lesson]] toSingle new TagService[Lesson]

  bind[LessonStatementReader].toSingle(new LessonStatementReader {
    lazy val lrsClient = inject[LrsClientManager](None)
    lazy val lessonService = inject[LessonService](None)
  })

  bind[ReportService].toSingle(new ReportServiceImpl(db.slickProfile, db.databaseDef) {
    lazy val reader = inject[LessonStatementReader](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val dateReport = inject[DateReport](None)
    lazy val userService = inject[UserService](None)
    lazy val lessonGradeService = inject[LessonGradeService](None)
    lazy val courseService = inject[CourseService](None)
    lazy val learningPathService = inject[LearningPathService](None)
    lazy val userResult = inject[UserLessonResultService](None)
  })

  bind[LessonService].toSingle(new LessonServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val fileService = inject[FileService](None)
    lazy val ratingService = inject[RatingService[Lesson]](None)
    lazy val customLessonServices = Map[LessonType.LessonType, CustomLessonService](
      LessonType.Tincan -> inject[TincanPackageService](None),
      LessonType.Scorm -> inject[ScormPackageService](None)
    )
    lazy val tagService = inject[TagService[Lesson]](None)
    lazy val assetHelper = inject[LessonAssetHelper](None)
    lazy val socialActivityHelper = new SocialActivityHelper[Lesson]
    lazy val userService = UserLocalServiceHelper()
    lazy val fileStorage = inject[FileStorage](None)
    lazy val lessonNotificationService = inject[LessonNotificationService](None)

    override def delete(lessonId: Long): Unit = {
      val lesson = getLessonRequired(lessonId)

      inject[TeacherLessonGradeService](None).deleteByLesson(lessonId)

      super.delete(lessonId)

      inject[UserCourseResultService](None).resetCourseResults(lesson.courseId)
    }
  })

  bind[LessonPlayerService] toSingle new LessonPlayerServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val lessonService = inject[LessonService](None)
    lazy val statementReader = inject[LessonStatementReader](None)
    lazy val ratingService = inject[RatingService[Lesson]](None)
    lazy val tagService = inject[TagService[Lesson]](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val teacherGradeService= inject[TeacherLessonGradeService](None)
    lazy val playerAssetHelper = new AssetHelper[LessonPlayer]
  }

  bind[UserLessonResultService] toSingle new UserLessonResultServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val lessonService = inject[LessonService](None)
    lazy val statementReader = inject[LessonStatementReader](None)
    lazy val courseService = inject[CourseService](None)
    lazy val lessonResultCalculate = inject[LessonResultCalculate](None)

    override def onLessonFinished(result: UserLessonResult): Unit = {
      inject[LessonListener](None).lessonFinished(result.userId, result.lessonId, result.lastAttemptDate.get)
    }
  }

  bind[LessonResultCalculate] toSingle new LessonResultCalculate {
    lazy val lessonService = inject[LessonService](None)
    lazy val statementReader = inject[LessonStatementReader](None)
  }

  bind[TincanPackageUploader] toSingle new TincanPackageUploader {
    lazy val lessonService = inject[LessonService](None)
    lazy val tincanPackageService = inject[TincanPackageService](None)
    lazy val lessonLimitService = inject[LessonLimitService](None)
  }
  bind[ScormPackageUploader] toSingle new ScormPackageUploader {
    lazy val lessonService = inject[LessonService](None)
    lazy val scormPackageService = inject[ScormPackageService](None)
    lazy val resourceStorage = inject[ResourcesStorage](None)
    lazy val activityStorage = inject[ActivityStorage](None)
    lazy val fileStorage = inject[FileStorage](None)
  }
  bind[PresentationPackageUploader] toSingle new PresentationPackageUploader {
    lazy val presentationProcessor = inject[PresentationProcessor](None)
    lazy val tincanPackageUploader = inject[TincanPackageUploader](None)
  }

  bind[TeacherLessonGradeService] toSingle new TeacherLessonGradeServiceImpl(db.databaseDef, db.slickDriver) {
    lazy val lrsClient = inject[LrsClientManager](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val courseService = inject[CourseService](None)

    override def onLessonGraded(userId: Long, lessonId: Long, grade: Option[Float]): Unit = {
      inject[LessonListener](None).lessonGraded(userId, lessonId, grade)
    }
  }

  bind[PackageUploadManager] toSingle new PackageUploadManager {
    lazy val customUploaders = Seq[CustomPackageUploader](
      inject[TincanPackageUploader](None),
      inject[ScormPackageUploader](None),
      inject[PresentationPackageUploader](None)
    )

    lazy val lessonAssetHelper = inject[LessonAssetHelper](None)

    override def uploadPackage(title: String,
                               description: String,
                               courseId: Long,
                               userId: Long,
                               fileName: String,
                               packageFile: File): Lesson = {
      val lesson = super.uploadPackage(title, description, courseId, userId, fileName, packageFile)

      inject[UserCourseResultService](None).setCourseNotCompleted(courseId)

      lesson
    }
  }


  // ---- SCORM RTE
  bind[ActivityServiceContract].toSingle(new ActivityService)
  bind[NavigationRequestServiceContract] toSingle new NavigationRequestService
  bind[TerminationRequestServiceContract] toSingle new TerminationRequestService
  bind[SequencingRequestServiceContract] toSingle new SequencingRequestService
  bind[DeliveryRequestServiceContract] toSingle new DeliveryRequestService
  bind[RollupServiceContract] toSingle new RollupService
  bind[EndAttemptServiceContract] toSingle new EndAttemptService

  bind[PackageMobileExportProcessor] toSingle new PackageMobileExportProcessor {
    lazy val fileService = inject[FileService](None)
    lazy val fileStorage = inject[FileStorage](None)
  }

  bind[PackageExportProcessor] toSingle new PackageExportProcessor {
    lazy val fileService = inject[FileService](None)
    lazy val fileStorage = inject[FileStorage](None)
  }

  bind[PackageImportProcessor] toSingle new PackageImportProcessor {
    lazy val packageUploader = inject[PackageUploadManager](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val lessonNotificationService = inject[LessonNotificationService](None)
  }

  bind[LearningPathService] toSingle new LearningPathServiceImpl
})
