package com.arcusys.valamis.gradebook.service.impl

import java.net.URI

import com.arcusys.learn.liferay.LiferayClasses.{LGroup, LUser}
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.model.{LessonSortBy, LessonSort, LessonStates, Lesson}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.valamis.user.model.{UserSortBy, UserSort, UserFilter}
import com.arcusys.valamis.user.service.UserService
import org.joda.time.DateTime

abstract class LessonGradeServiceImpl extends LessonGradeService {

  def teacherGradeService: TeacherLessonGradeService
  def userServiceHelper: UserLocalServiceHelper
  def lessonService: LessonService
  def lessonResultService: UserLessonResultService
  def courseService: CourseService
  def lrsClient: LrsClientManager
  def memberService: LessonMembersService
  def userService: UserService
  def membersService: LessonMembersService

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

  def isLessonFinished(lesson: Lesson, user: LUser): Boolean = {
    teacherGradeService.get(user.getUserId, lesson.id).flatMap(_.grade) match {
      case Some(teacherGrade) => isGradeMoreSuccessLimit(teacherGrade, lesson.scoreLimit)
      case None => lessonResultService.get(lesson, user).isFinished
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
    grade >= scoreLimit
  }

  def isCourseCompleted(courseId: Long, userId: Long): Boolean = {
    val lessonsCount = lessonService.getCount(courseId)
    val completedLessonsCount = getCompletedLessonsCount(courseId, userId)
    lessonsCount == completedLessonsCount
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
    val items = getSortedLessonGrades(allItems, Some(UserSort(UserSortBy.LastAttempted, Order.Desc)), skipTake)

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

  def getLessonGradesByCourse(courseId: Long,
                              lessonId: Long,
                              companyId: Long,
                              organizationId: Option[Long],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val users = userService.getUsersByGroupOrOrganization(companyId, courseId, organizationId)
    val lesson = lessonService.getLessonRequired(lessonId)
    getLessonsWithGradesByLesson(lesson, users, sortBy, skipTake)
  }

  def getLessonGradesByCourses(courses: Seq[LGroup],
                               lessonId: Long,
                               companyId: Long,
                               organizationId: Option[Long],
                               sortBy: Option[UserSort],
                               skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val users = userService.getByCourses(coursesIds, companyId, organizationId)
    val lesson = lessonService.getLessonRequired(lessonId)
    getLessonsWithGradesByLesson(lesson, users, sortBy, skipTake)
  }

  def getUserGradesByCourse(courseId: Long,
                            user: LUser,
                            sortBy: Option[LessonSort],
                            skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val allLessons = lessonService.getAll(courseId)
    getLessonsWithGradesByUser(user, allLessons, sortBy, skipTake)
  }

  def getUserGradesByCourses(courses: Seq[LGroup],
                             user: LUser,
                             sortBy: Option[LessonSort],
                             skipTake: Option[SkipTake]): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val allLessons = lessonService.getByCourses(coursesIds)
    getLessonsWithGradesByUser(user, allLessons, sortBy, skipTake)
  }

  def getUsersGradesByCourse(courseId: Long,
                             users: Seq[LUser],
                             sortBy: Option[UserSort],
                             skipTake: Option[SkipTake],
                             inReview: Boolean = false): RangeResult[LessonWithGrades] = {

    val (total, allItems) = if (inReview) {
      val lessons = lessonService.getInReview(courseId)
      val items = getUsersGradesByLessons(users, lessons)
        .filter(item => item.lastAttemptedDate.isDefined && item.teacherGrade.isEmpty)
      (items.size, items)

    }
    else {
      val total = lessonService.getCount(courseId)
      val lessons = lessonService.getAll(courseId)
      (total, getUsersGradesByLessons(users, lessons))
    }

    val items = getSortedLessonGrades(allItems, sortBy, skipTake)

    RangeResult(total, items)
  }

  def getUsersGradesByCourses(courses: Seq[LGroup],
                              companyId: Long,
                              organizationId: Option[Long],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake],
                              inReview: Boolean = false): RangeResult[LessonWithGrades] = {

    val coursesIds = courses.map(_.getGroupId)
    val users = userService.getByCourses(coursesIds, companyId, organizationId)

    val (total, allItems) = if (inReview) {
      val lessons = lessonService.getInReviewByCourses(coursesIds)
      val items = getUsersGradesByLessons(users, lessons)
        .filter(item => item.lastAttemptedDate.isDefined && item.teacherGrade.isEmpty)
      (items.size, items)

    }
    else {
      val total = lessonService.getCountByCourses(coursesIds)
      val lessons = lessonService.getByCourses(coursesIds)
      (total, getUsersGradesByLessons(users, lessons))
    }

    val items = getSortedLessonGrades(allItems, sortBy, skipTake)

    RangeResult(total, items)
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

    val items = getSkipped(sortedItems, skipTake)

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

  private def getSortedLessonGrades(allItems: Seq[LessonWithGrades],
                                    sortBy: Option[UserSort],
                                    skipTake: Option[SkipTake]): Seq[LessonWithGrades] = {

    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

    val sortedItems = sortBy match {
      case Some(UserSort(UserSortBy.LastAttempted, Order.Asc)) => allItems.sortBy(_.lastAttemptedDate)
      case Some(UserSort(UserSortBy.LastAttempted, Order.Desc)) => allItems.sortBy(_.lastAttemptedDate).reverse
      case Some(UserSort(_, Order.Asc)) => allItems.sortBy(_.user.getFullName)
      case Some(UserSort(_, Order.Desc)) => allItems.sortBy(_.user.getFullName).reverse
      case None => allItems
    }

    getSkipped(sortedItems, skipTake)
  }

  private def getSortedLessons(allItems: Seq[Lesson],
                               sortBy: Option[LessonSort],
                               skipTake: Option[SkipTake]): Seq[Lesson] = {

    val sortedItems = sortBy match {
      case Some(LessonSort(LessonSortBy.Name, Order.Asc)) => allItems.sortBy(_.title)
      case Some(LessonSort(LessonSortBy.Name, Order.Desc)) => allItems.sortBy(_.title).reverse
      case None => allItems
    }

    getSkipped(sortedItems, skipTake)
  }

  private def getSortedUsers(allItems: Seq[LUser],
                             sortBy: Option[UserSort],
                             skipTake: Option[SkipTake]): Seq[LUser] = {

    val sortedItems = sortBy match {
      case Some(UserSort(UserSortBy.Name, Order.Asc)) => allItems.sortBy(_.getFullName)
      case Some(UserSort(UserSortBy.Name, Order.Desc)) => allItems.sortBy(_.getFullName).reverse
      case None => allItems
    }

    getSkipped(sortedItems, skipTake)

  }

  private def getSkipped[T](allItems: Seq[T], skipTake: Option[SkipTake]): Seq[T] = {
    skipTake match {
      case Some(SkipTake(skip, take)) => allItems.slice(skip, skip + take)
      case None => allItems
    }
  }
}
