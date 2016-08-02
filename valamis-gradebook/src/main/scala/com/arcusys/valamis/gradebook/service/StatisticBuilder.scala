package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.gradebook.model.Statistic
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service.{LessonMembersService, UserLessonResultService, TeacherLessonGradeService, LessonService}

abstract class StatisticBuilder {

  def lessonService: LessonService
  def teacherGradeService: TeacherLessonGradeService
  def courseResults: UserCourseResultService
  def lessonResultService: UserLessonResultService
  def membersService: LessonMembersService
  def courseService: CourseService

  def getLessonsStatistic(users: Seq[LUser],
                          courseId: Long): Map[LUser, Statistic] = {
    val lessons = lessonService.getAll(courseId)
    getStatisticByUser(users, lessons)
  }

  def getCoursesLessonStatistic(users: Seq[LUser],
                                courseIds: Seq[Long]): Map[LUser, Statistic] = {
    val lessons = lessonService.getByCourses(courseIds)
    getStatisticByUser(users, lessons)
  }

  def getStatisticByUser(users: Seq[LUser],
                         lessons: Seq[Lesson]): Map[LUser, Statistic] = {

    val lessonsIds = lessons.map(_.id)
    val userIds = users.map(_.getUserId)

    val teacherGrades = teacherGradeService.get(userIds, lessonsIds)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val lessonResults = lessonResultService.get(users, lessons)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val members = membersService.getLessonsUsers(lessons, users)

    users.map { user =>
      user -> getLessonStatesByUsers(
        Seq(user),
        members.filter(_.user == user).map(_.lesson),
        (u, l) => teacherGrades.get(u.getUserId).flatMap(_.get(l.id)),
        (u, l) => lessonResults(u.getUserId)(l.id)
      )
    }.toMap
  }

  def getStatisticByLesson(users: Seq[LUser],
                          lessons: Seq[Lesson]): Map[Lesson, Statistic] = {

    val lessonsIds = lessons.map(_.id)
    val userIds = users.map(_.getUserId)

    val teacherGrades = teacherGradeService.get(userIds, lessonsIds)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val lessonResults = lessonResultService.get(users, lessons)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val members = membersService.getLessonsUsers(lessons, users)

    lessons.map { lesson =>
      lesson -> getLessonStatesByUsers(
        members.filter(_.lesson == lesson).map(_.user),
        Seq(lesson),
        (u, l) => teacherGrades.get(u.getUserId).flatMap(_.get(l.id)),
        (u, l) => lessonResults(u.getUserId)(l.id),
        true
      )
    }.toMap
  }

  def getStatisticByCourse(user: LUser,
                           courses: Seq[LGroup]): Map[LGroup, Statistic] = {

    val courseIds = courses.map(_.getGroupId)
    val lessons = lessonService.getByCourses(courseIds)
    val lessonsIds = lessons.map(_.id)

    val teacherGrades = teacherGradeService.get(user.getGroupId, lessonsIds)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val lessonResults = lessonResultService.get(Seq(user), lessons)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val members = membersService.getLessonsUsers(lessons, Seq(user))

    courses.map { course =>
      val lessonsByCourse = lessons.filter(_.courseId == course.getGroupId)
      val lessonsByMembers = members.filter(_.user == user).map(_.lesson)
      course -> getLessonStatesByUsers(
        Seq(user),
        lessonsByCourse.intersect(lessonsByMembers),
        (u, l) => teacherGrades.get(u.getUserId).flatMap(_.get(l.id)),
        (u, l) => lessonResults(u.getUserId)(l.id)
      )
    }.toMap
  }

  def getLessonStatesByUsers(users: Seq[LUser],
                             lessons: Seq[Lesson],
                             getTeacherGrade: ((LUser, Lesson) => Option[LessonGrade]),
                             getLessonResult: ((LUser, Lesson) => UserLessonResult),
                             byUser: Boolean = false
                            ): Statistic = {

    val total = if(byUser) users.size else lessons.size

    val states =
      for {
        user <- users
        lesson <- lessons
        lessonResult = getLessonResult(user, lesson)
        teacherGrade = getTeacherGrade(user, lesson).flatMap(_.grade)
      }
        yield lesson.getLessonStatus(lessonResult, teacherGrade)

    getStatistic(total, states.flatten)
  }

  def getCoursesStatistic(users: Seq[LUser]): Map[LUser, Statistic] = {
    users.map {user =>
      val courses = courseService.getSitesByUserId(user.getUserId)
      val states = getStatisticByCourse(user, courses).values
        .flatMap(getCourseStatus)
        .toSeq

      user -> getStatistic(courses.size, states)
    }.toMap
  }

  def getCourseLessonsStatistic(users: Seq[LUser], courseId: Long): Statistic = {
    val lessons = lessonService.getAll(courseId)
    val states = getStatisticByUser(users, lessons).values
      .flatMap(getCourseStatus)
      .toSeq

    getStatistic(users.size, states)
  }

  private def getCourseStatus(statistic: Statistic): Option[LessonStates.Value] = {
    if (statistic.total == statistic.success){
      Some(LessonStates.Finished)
    }
    else if (statistic.total == statistic.notStarted){
      None
    }
    else {
      Some(LessonStates.Attempted)
    }
  }

  private def getStatistic(total: Int, states: Seq[LessonStates.Value]): Statistic = {
    val success = states.count(_ == LessonStates.Finished)
    val inProgress = states.count(_ != LessonStates.Finished)

    Statistic(
      success = success,
      inProgress = inProgress,
      notStarted = total - success - inProgress,
      total = total
    )
  }

}
