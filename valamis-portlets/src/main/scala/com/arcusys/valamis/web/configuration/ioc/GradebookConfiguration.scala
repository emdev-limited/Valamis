package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.gradebook.service.impl._
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.user.service.UserService
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class GradebookConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(fn = module => {
  import configuration.inject
  import module.bind

  bind[TeacherCourseGradeService] toSingle {
    new TeacherCourseGradeServiceImpl(db.databaseDef, db.slickProfile)
  }

  bind[LessonGradeService] toSingle new LessonGradeServiceImpl {
    lazy val lrsClient = inject[LrsClientManager](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val userServiceHelper = inject[UserLocalServiceHelper](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val courseService = inject[CourseService](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val memberService = inject[LessonMembersService](None)
    lazy val userService = inject[UserService](None)
    lazy val membersService = inject[LessonMembersService](None)
    lazy val teacherCourseGradeService = inject[TeacherCourseGradeService](None)
  }

  bind[UserCourseResultService] toSingle new UserCourseResultServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val userCourseResultService = inject[UserCourseResultServiceImpl](None)
    lazy val packageChecker = inject[LessonGradeService](None)
    lazy val lessonService = inject[LessonService](None)
  }

  bind[CourseLessonsResultService] toSingle new CourseLessonsResultServiceImpl(db.databaseDef, db.slickProfile) {
    lazy val lessonGradeService = inject[LessonGradeService](None)
    lazy val memberService = inject[LessonMembersService](None)
    lazy val statisticBuilder = inject[StatisticBuilder](None)
  }

  bind[StatisticBuilder] toSingle new StatisticBuilder {
    lazy val lessonService = inject[LessonService](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val courseResults = inject[UserCourseResultService](None)
    lazy val membersService = inject[LessonMembersService](None)
    lazy val courseService = inject[CourseService](None)
  }

  bind[GradeBookService] toSingle new GradeBookServiceImpl {
    lazy val statementReader = inject[LessonStatementReader](None)
    lazy val lessonService = inject[LessonService](None)
  }
})
