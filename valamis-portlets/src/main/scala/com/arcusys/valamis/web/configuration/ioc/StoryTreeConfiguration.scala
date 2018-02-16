package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.service.{LessonStatementReader, TeacherLessonGradeService, UserLessonResultService, LessonService}
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.storyTree.service.{StoryTreeNodeService, StoryTreeService, StoryTreeStatusService}
import com.arcusys.valamis.storyTree.service.impl.{StoryTreeNodeServiceImpl, StoryTreeServiceImpl, StoryTreeStatusServiceImpl}
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class StoryTreeConfiguration(db: => SlickDBInfo)
                            (implicit val configuration: BindingModule)
  extends NewBindingModule(module => {

    import configuration.inject
    import module.bind

    bind[StoryTreeService].toSingle(new StoryTreeServiceImpl(db.databaseDef, db.slickProfile) {
      lazy val fileService = inject[FileService](None)
      lazy val lessonService = inject[LessonService](None)
      lazy val lessonCategoryGoalService = inject[LessonCategoryGoalService](None)
    })
    bind[StoryTreeNodeService].toSingle(new StoryTreeNodeServiceImpl(db.databaseDef, db.slickProfile) {
      lazy val lessonService = inject[LessonService](None)
      lazy val lessonCategoryGoalService = inject[LessonCategoryGoalService](None)
    })
    bind[StoryTreeStatusService].toSingle(new StoryTreeStatusServiceImpl(db.databaseDef, db.slickProfile) {
      lazy val lessonService = inject[LessonService](None)
      lazy val lessonCategoryGoalService = inject[LessonCategoryGoalService](None)
      lazy val lrsClient = inject[LrsClientManager](None)
      lazy val lessonResultService = inject[UserLessonResultService](None)
      lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
      lazy val lessonStatementReader = inject[LessonStatementReader](None)
    })
})