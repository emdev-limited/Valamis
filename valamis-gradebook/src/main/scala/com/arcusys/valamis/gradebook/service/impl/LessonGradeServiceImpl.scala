package com.arcusys.valamis.gradebook.service.impl

import java.net.URI

import com.arcusys.learn.liferay.LiferayClasses.{LGroup, LUser}
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.gradebook.model.{LessonWithGrades, UserCoursesWithGrade}
import com.arcusys.valamis.gradebook.service.{LessonGradeService, TeacherCourseGradeService}
import com.arcusys.valamis.gradebook.utils.LessonWithGradesSortExtension
import com.arcusys.valamis.lesson.model.{Lesson, LessonSort, LessonSortBy, LessonStates}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lrssupport.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.valamis.user.model.{UserSort, UserSortBy}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.utils.SeqCutExtension
import org.joda.time.DateTime

abstract class LessonGradeServiceImpl extends LessonGradeService {

  def teacherGradeService: TeacherLessonGradeService
  def lessonService: LessonService
  def lessonResultService: UserLessonResultService
  def courseService: CourseService
  def userService: UserService
  def membersService: LessonMembersService
  def teacherCourseGradeService: TeacherCourseGradeService


  lazy val completeVerbs = Seq(
    new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
    new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Passed))
  )

  def getCompletedLessonsCount(courseId: Long, userId: Long): Int = {
    lessonService.getAll(courseId) count { lesson =>
      val teacherGrade = teacherGradeService.get(userId, lesson.id).flatMap(_.grade)
      isLessonFinished(teacherGrade, userId, lesson)
    }
  }

  def isLessonFinished(teacherGrade: Option[Float], userId: Long, lesson: Lesson): Boolean = {
    lazy val user = UserLocalServiceHelper().getUser(userId)
    lazy val lessonResult = lessonResultService.get(lesson, user)
    teacherGrade.map { grade =>
      isGradeMoreSuccessLimit(grade, lesson.scoreLimit)
    }.getOrElse {
      !lesson.requiredReview && lessonResult.isFinished
    }
  }

  private def isGradeMoreSuccessLimit(grade: Float, scoreLimit: Double): Boolean = {
    (grade.toDouble + 0.0001) >= scoreLimit
  }

  def isCourseCompleted(courseId: Long, userId: Long): Boolean = {
    val lessonsCount = lessonService.getCount(courseId)
    val completedLessonsCount = getCompletedLessonsCount(courseId, userId)
    lessonsCount == completedLessonsCount
  }

  def getCoursesCompletedWithGrade(userId: Long): Seq[UserCoursesWithGrade] = {

    val courses = courseService.getSitesByUserId(userId)
    val grades = teacherCourseGradeService.get(courses.map(_.getGroupId), userId)

    val coursesWithGrade: Seq[UserCoursesWithGrade] = grades.map { g =>
      val course = courses.filter(g.courseId == _.getGroupId).head
      UserCoursesWithGrade(course, g.grade)
    }

    val coursesWithCompletedLessons: Seq[UserCoursesWithGrade] = courses.filterNot(c => grades.map(_.courseId).contains(c.getGroupId))
      .filter(c => getCompletedLessonsCount(c.getGroupId, userId) > 0)
      .map(UserCoursesWithGrade(_, None))

    coursesWithGrade ++ coursesWithCompletedLessons
  }

  def getFinishedLessonsGradesByUser(user: LUser,
                                     coursesIds: Seq[Long],
                                     isFinished: Boolean,
                                     skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val lessons = lessonService.getByCourses(coursesIds)
    getFinishedLessonsGrades(user, lessons, isFinished, skipTake)
  }

  def getLastActivityLessonWithGrades(user: LUser,
                                      courseId: Long,
                                      skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val allLessons = lessonService.getAll(courseId)
    val members = membersService.getLessonsUsers(allLessons, Seq(user))
    val lessons = members.filter(_.user == user).map(_.lesson)
    val allItems = getUsersGradesByLessons(Seq(user), lessons)
      .filter(_.lastAttemptedDate.isDefined)
    val items = allItems
      .sort(UserSort(UserSortBy.LastAttempted, Order.Desc))
      .skip(skipTake)

    RangeResult(allItems.size, items)
  }

  def getUsersGradesByLessons(users: Seq[LUser],
                              lessons: Seq[Lesson]): Seq[LessonWithGrades] = {

    val usersIds = users.map(_.getUserId)
    val lessonIds = lessons.map(_.id)

    val teacherGrades = teacherGradeService.get(usersIds, lessonIds)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))
    val lessonResults = lessonResultService.get(users, lessons)
      .groupBy(_.userId)
      .mapValues(_.groupBy(_.lessonId).mapValues(_.head))

    for {
      user <- users
      lesson <- lessons
      teacherGrade = teacherGrades.get(user.getUserId).flatMap(_.get(lesson.id))
      lessonResult = lessonResults(user.getUserId)(lesson.id)
      state = lesson.getLessonStatus(lessonResult, teacherGrade.flatMap(_.grade))
    } yield
      LessonWithGrades(
        lesson,
        user,
        lessonResult.lastAttemptDate,
        lessonResult.score,
        teacherGrade,
        state
      )
  }

  def getLessonAverageGrades(lesson: Lesson, users: Seq[LUser]): Float = {
    getUsersGradesByLessons(users, Seq(lesson)).flatMap { grade =>
      grade.teacherGrade.flatMap(_.grade) orElse grade.autoGrade
    }.sum
  }

// TODO decide what make with gradebook average grades - maybe make one
  def getLessonAverageGradesForReport(lesson: Lesson,
                                      users: Seq[LUser]): Option[Float] = {
    val averadeGrade = getUsersGradesByLessons(users, Seq(lesson))
      .flatMap { grade =>
        grade.teacherGrade.flatMap(_.grade) orElse grade.autoGrade
      }
    if (averadeGrade.length > 0) {
      Some(averadeGrade.sum / averadeGrade.length)
    } else {
      None
    }
  }

  def getLessonGradesByCourse(courseId: Long,
                              lessonId: Long,
                              companyId: Long,
                              organizationId: Option[Long],
                              userNameFilter: Option[String],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val users = userService.getByCourses(Seq(courseId), companyId, organizationId, userNameFilter)
    val lesson = lessonService.getLessonRequired(lessonId)
    getLessonsWithGradesByLesson(lesson, users, sortBy, skipTake)
  }

  def getLessonGradesByCourses(courses: Seq[LGroup],
                               lessonId: Long,
                               companyId: Long,
                               organizationId: Option[Long],
                               userNameFilter: Option[String],
                               sortBy: Option[UserSort],
                               skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val users = userService.getByCourses(coursesIds, companyId, organizationId, userNameFilter)
    val lesson = lessonService.getLessonRequired(lessonId)
    getLessonsWithGradesByLesson(lesson, users, sortBy, skipTake)
  }

  def getUserGradesByCourse(courseId: Long,
                            user: LUser,
                            lessonTitleFilter: Option[String],
                            sortBy: Option[LessonSort],
                            skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val allLessons = lessonService.getAllSorted(courseId, lessonTitleFilter)
    getLessonsWithGradesByUser(user, allLessons, sortBy, skipTake)
  }

  def getUserGradesByCourses(courses: Seq[LGroup],
                             user: LUser,
                             lessonTitleFilter: Option[String],
                             sortBy: Option[LessonSort],
                             skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val allLessons = lessonService.getSortedByCourses(coursesIds, lessonTitleFilter)
    getLessonsWithGradesByUser(user, allLessons, sortBy, skipTake)
  }

  def getInReviewByCourse(courseId: Long,
                          users: Seq[LUser],
                          nameFilter: Option[String],
                          sortBy: Option[UserSort],
                          skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val lessons = lessonService.getInReview(courseId)
    val allItems = getUsersGradesByLessons(users, lessons)
      .filter(item => item.lastAttemptedDate.isDefined && item.teacherGrade.flatMap(_.grade).isEmpty)
      .filter(containsName(nameFilter))

    val items = allItems
      .sort(sortBy)
      .skip(skipTake)

    RangeResult(allItems.size, items)
  }

  def getInReviewByCourses(courses: Seq[LGroup],
                           companyId: Long,
                           organizationId: Option[Long],
                           nameFilter: Option[String],
                           sortBy: Option[UserSort],
                           skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val users = userService.getByCourses(coursesIds, companyId, organizationId)

    val lessons = lessonService.getInReviewByCourses(coursesIds)
    val allItems = getUsersGradesByLessons(users, lessons)
      .filter(item => item.lastAttemptedDate.isDefined && item.teacherGrade.isEmpty)
      .filter(containsName(nameFilter))

    val items = allItems
      .sort(sortBy)
      .skip(skipTake)

    RangeResult(allItems.size, items)
  }

  private def containsName(nameFilter: Option[String]): (LessonWithGrades => Boolean) = {
    nameFilter.map(_.toLowerCase) match {
      case Some(filter) => g =>
        g.lesson.title.toLowerCase.contains(filter) ||
          g.user.getFullName.toLowerCase.contains(filter)

      case None => g => true
    }
  }

  private def getFinishedLessonsGrades(user: LUser,
                                       allLessons: Seq[Lesson],
                                       isFinished: Boolean,
                                       skipTake: Option[SkipTake],
                                       ascending: Option[Boolean] = None): RangeResult[LessonWithGrades] = {
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

    val members = membersService.getLessonsUsers(allLessons, Seq(user))
    val lessons = members.filter(_.user == user).map(_.lesson)
    val allItems = getUsersGradesByLessons(Seq(user), lessons)
      .filter(x => if (isFinished) {
        x.state.contains(LessonStates.Finished)
      }
      else {
        !x.state.contains(LessonStates.Finished)
      })

    val sortedItems = ascending match {
      case Some(true) => allItems.sortBy(_.lastAttemptedDate).reverse
      case Some(false) => allItems.sortBy(_.lastAttemptedDate)
      case None => allItems
    }

    val items = sortedItems.skip(skipTake)

    RangeResult(allItems.size, items)
  }

  private def getLessonsWithGradesByLesson(lesson: Lesson,
                                           allUsers: Seq[LUser],
                                           sortBy: Option[UserSort],
                                           skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val allMembers = membersService
      .getLessonsUsers(Seq(lesson), allUsers)
      .filter(_.lesson == lesson)
      .map(_.user)

    val members = getSortedUsers(allMembers, sortBy, skipTake)

    val items = getUsersGradesByLessons(members, Seq(lesson))
    RangeResult(allMembers.size, items)
  }

  private def getLessonsWithGradesByUser(user: LUser,
                                         allLessons: Seq[Lesson],
                                         sortBy: Option[LessonSort],
                                         skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val members = membersService.getLessonsUsers(allLessons, Seq(user))
    val lessons = members.filter(_.user == user).map(_.lesson)
    val sortedLessons = getSortedLessons(lessons, sortBy, skipTake)
    val items = getUsersGradesByLessons(Seq(user), sortedLessons)
    RangeResult(lessons.size, items)
  }

  private def getSortedLessons(allItems: Seq[Lesson],
                               sortBy: Option[LessonSort],
                               skipTake: Option[SkipTake]): Seq[Lesson] = {

    val sortedItems = sortBy match {
      case Some(LessonSort(LessonSortBy.Name, Order.Asc)) => allItems.sortBy(_.title)
      case Some(LessonSort(LessonSortBy.Name, Order.Desc)) => allItems.sortBy(_.title).reverse
      case _ => allItems
    }

    sortedItems.skip(skipTake)
  }

  private def getSortedUsers(allItems: Seq[LUser],
                             sortBy: Option[UserSort],
                             skipTake: Option[SkipTake]): Seq[LUser] = {

    val sortedItems = sortBy match {
      case Some(UserSort(UserSortBy.Name, Order.Asc)) => allItems.sortBy(_.getFullName)
      case Some(UserSort(UserSortBy.Name, Order.Desc)) => allItems.sortBy(_.getFullName).reverse
      case _ => allItems
    }

    sortedItems.skip(skipTake)
  }
}
