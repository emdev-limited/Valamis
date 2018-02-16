package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.content.service.{CategoryService, PlainTextService, QuestionService}
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.generator.tincan.file.html.TinCanQuestionViewGenerator
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.{LessonAssetHelper, LessonNotificationService, LessonService}
import com.arcusys.valamis.lesson.tincan.service.{LessonCategoryGoalService, TincanPackageService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.slide.service._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.slide.storage._
import com.arcusys.valamis.statements.StatementChecker
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.utils.ResourceReader
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}


class SlideConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(fn = module => {
  import configuration.inject
  import module.bind

  bind[SlideSetService] toSingle new SlideSetServiceImpl {
    lazy val slideSetRepository = inject[SlideSetRepository](None)
    lazy val slideSetImporter = inject[SlideSetImporter](None)
    lazy val slideSetExporter = inject[SlideSetExporter](None)
    lazy val slideService = inject[SlideService](None)
    lazy val slideElementService = inject[SlideElementService](None)
    lazy val uriService = inject[TincanURIService](None)
    lazy val fileService = inject[FileService](None)
    lazy val slideThemeRepository = inject[SlideThemeRepository](None)
    lazy val courseService = inject[CourseService](None)
    lazy val slideTagService = inject[TagService[SlideSet]](None)
    lazy val slideAssetHelper = inject[SlideSetAssetHelper](None)
  }

  bind[SlideSetPublishService] toSingle new SlideSetPublishServiceImpl {
    lazy val lessonBuilder = inject[TincanLessonBuilder](None)
    lazy val packageCategoryGoalStorage = inject[LessonCategoryGoalService](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val tincanPackageService = inject[TincanPackageService](None)
    lazy val lessonTagService = inject[TagService[Lesson]](None)
    lazy val lessonAssetHelper = inject[LessonAssetHelper](None)
    lazy val lessonSocialActivityHelper = new SocialActivityHelper[Lesson]
    lazy val slideService = inject[SlideService](None)
    lazy val slideSetService = inject[SlideSetService](None)
    lazy val slideSetRepository = inject[SlideSetRepository](None)
    lazy val uriService = inject[TincanURIService](None)
    lazy val slideTagService = inject[TagService[SlideSet]](None)
    lazy val lessonNotificationService = inject[LessonNotificationService](None)
    lazy val lrsReader = inject[LrsClientManager](None)
    lazy val lrsRegistration = inject[LrsRegistration](None)
    lazy val statementChecker = inject[StatementChecker](None)
  }

  bind[SlideService] toSingle new SlideServiceImpl {
    lazy val slideRepository = inject[SlideRepository](None)
    lazy val slideSetRepository = inject[SlideSetRepository](None)
    lazy val fileService = inject[FileService](None)
    lazy val presentationProcessor = Configuration.inject[PresentationProcessor](None)
    lazy val pdfProcessor = Configuration.inject[PDFProcessor](None)
    lazy val slideThemeService = inject[SlideThemeService](None)
  }
  bind[SlideElementService] toSingle new SlideElementServiceImpl {
    lazy val slideElementRepository = inject[SlideElementRepository](None)
    lazy val slideElementPropertyRepository = inject[SlideElementPropertyRepository](None)
    lazy val fileService = inject[FileService](None)
  }
  bind[SlideThemeService] toSingle new SlideThemeServiceImpl {
    lazy val slideSetRepository = inject[SlideSetRepository](None)
    lazy val slideThemeRepository = inject[SlideThemeRepository](None)
    lazy val fileService = inject[FileService](None)
  }
  bind[SlideSetExporter] toSingle new SlideSetExporterImpl {
    lazy val fileService = inject[FileService](None)
    lazy val questionService = inject[QuestionService](None)
    lazy val plainTextService = inject[PlainTextService](None)
    lazy val slideService = inject[SlideService](None)
    lazy val categoryService = inject[CategoryService](None)
  }
  bind[SlideSetImporter] toSingle new SlideSetImporterImpl {
    lazy val slideSetService = inject[SlideSetService](None)
    lazy val slideService = inject[SlideService](None)
    lazy val slideElementService = inject[SlideElementService](None)
    lazy val fileService = inject[FileService](None)
    lazy val questionService = inject[QuestionService](None)
    lazy val plainTextService = inject[PlainTextService](None)
    lazy val slideElementPropertyRepository = inject[SlideElementPropertyRepository](None)
    lazy val categoryService = inject[CategoryService](None)
  }
  bind[TincanLessonBuilder] toSingle new TincanLessonBuilderImpl {
    lazy val resourceReader = Configuration.inject[ResourceReader](None)
    lazy val slideService = inject[SlideService](None)
    lazy val slideSetService = inject[SlideSetService](None)
    lazy val questionService = inject[QuestionService](None)
    lazy val plainTextService = inject[PlainTextService](None)
    lazy val fileService = inject[FileService](None)
    lazy val tincanQuestionViewGenerator = new TinCanQuestionViewGenerator
    lazy val uriService = inject[TincanURIService](None)
    lazy val categoryService = inject[CategoryService](None)

  }
})
