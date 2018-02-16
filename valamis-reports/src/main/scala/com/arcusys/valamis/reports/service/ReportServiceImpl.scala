package com.arcusys.valamis.reports.service

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.reports.DateReport
import com.arcusys.valamis.certificate.service.{LearningPathService}
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import com.arcusys.valamis.reports.model._
import com.arcusys.valamis.user.util.UserExtension
import org.joda.time.{DateTime, Days}
import com.arcusys.valamis.util.Joda.dateTimeOrdering
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.model.LessonStates
import com.arcusys.valamis.lesson.service.{UserLessonResultService}
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.user.service.UserService

abstract class ReportServiceImpl(val driver: JdbcProfile, val db: JdbcBackend#Database)
  extends ReportService
    with ReportExportSupport
    with TopLessonReporting {

  def dateReport: DateReport

  def learningPathService: LearningPathService

  def userService: UserService

  def lessonGradeService: LessonGradeService

  def courseService: CourseService

  def userResult: UserLessonResultService

  override def getTopLessons(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[LessonReport] = {
    getLessonsByAttempt(cfg) zip getLessonsByGrade(cfg) map {
      case (attempts, grades) =>
        (grades ++ attempts) sortBy (-_._4) take cfg.limit map TopLesson.tupled
    } map LessonReport
  }

  override def getUsersToLessonCount(since: DateTime, until: DateTime, courseIds: Seq[Long])
                                    (implicit ec: ExecutionContext): Future[Map[Long, Int]] = {
    val attemptsF = getUserAttempts(since, until, courseIds)
    val gradesF = getUserGrades(courseIds, since, until)

    attemptsF zip gradesF map {
      case (attempts, grades) => attempts ++ grades
    }
  }

  override def getMostActiveUsers(startDate: DateTime,
                                  endDate: DateTime,
                                  top: Integer,
                                  companyId: Long,
                                  allCoursesIds: Seq[Long])
                                 (implicit ec: ExecutionContext): Future[Seq[MostActiveUsers]] = {
    val usersToLessonF = getUsersToLessonCount(startDate, endDate, allCoursesIds)

    val usersToCertificateF =
      if (learningPathService.isLearningPathDeployed) {
        learningPathService.getUsersToCertificateCount(startDate, endDate, companyId)
      } else {
        Future(Map[Long, Int]())
      }

    for {
      usersToCertificate <- usersToCertificateF
      usersToLesson <- usersToLessonF
    } yield {
      val userIds = usersToCertificate.keySet ++ usersToLesson.keySet
      val filtered = UserLocalServiceHelper().getActiveUsers(userIds.toSeq) map (_.getUserId)

      filtered map { userId =>
        val countCertificates = usersToCertificate.getOrElse(userId, 0)
        val countLessons = usersToLesson.getOrElse(userId, 0)
        val user = UserLocalServiceHelper().getUser(userId)

        MostActiveUsers(
          userId,
          user.getFullName,
          user.getPortraitUrl,
          activityValue = countCertificates + countLessons,
          countCertificates = countCertificates,
          countLessons = countLessons
        )
      }
    }.toSeq sortBy (-_.activityValue) take top
  }

  override def getAverageGradesReport(companyId: Long,
                                      courseId: Long): Seq[AveragePassingGrades] = {
    val users = userService.getBy(UserFilter(
      companyId = Some(companyId),
      groupId = Some(courseId)
    ))

    val lessons = lessonService.getAllSorted(courseId,
      ascending = true)

    val data = lessons.map { lesson =>
      val grade = lessonGradeService.getLessonAverageGradesForReport(lesson,
        users)
      (lesson.id, lesson.title, grade)
    }
      .withFilter(_._3.isDefined)
      .map(a => AveragePassingGrades(lessonId = a._1,
        lessonTitle = a._2,
        grade = a._3.get))

    data.sortBy(_.grade).reverse
  }

  override def getAverageGradesForAllCoursers(companyId: Long,
                                              userId: Long): Seq[AveragePassingGrades] = {
    val coursesIds = courseService.getSitesByUserId(userId)
      .map(_.getGroupId)

    coursesIds.flatMap { c =>
      getAverageGradesReport(companyId, c)
    }.sortBy(_.grade).reverse
  }

  override def getCompletedLessonsReport(courseIds: Seq[Long],
                                         take: Int,
                                         skip: Int): Seq[AttemptedLessonsRow] = {
    val lessons = lessonService.getByCourses(courseIds)
    val lessonsIds = lessons.map(_.id)
    val finishedTotal = userResult.getAttemptedTotal(lessonsIds, isFinished = true, take, skip)

    val userIds = finishedTotal.map(_._1)
    val users = finishedTotal flatMap { case (userId, _) =>
      UserLocalServiceHelper().fetchUser(userId)
    }

    val attemptedTotal = userResult.getAttemptedTotal(lessonsIds, userIds, isFinished = false).toMap

    val lessonsRequiredReview = lessons.filter(_.requiredReview)
    val notFinishedLessonsRequiredReview =
      lessonGradeService.getUsersGradesByLessons(users, lessonsRequiredReview) filter {
        grade => grade.state.isDefined && grade.state != Some(LessonStates.Finished)
      } groupBy {
        _.user.getUserId
      } map {
        case (userId, seq) => (userId, seq.length)
      }

    finishedTotal flatMap { case (userId, finishedCount) =>
      UserLocalServiceHelper().fetchUser(userId) map { user =>
        AttemptedLessonsRow(
          user.getUserId,
          user.getFullName,
          attemptedTotal.getOrElse(userId, 0),
          finishedCount - notFinishedLessonsRequiredReview.getOrElse(userId, 0)
        )
      }
    } sortBy(_.countFinished) reverse
  }

  override def getAttemptedLessonsReport(courseIds: Seq[Long],
                                         take: Int,
                                         skip: Int): Seq[AttemptedLessonsRow] = {
    val lessons = lessonService.getByCourses(courseIds)
    val attemptedTotal = userResult.getAttemptedTotal(lessons.map(_.id), isFinished = false, take, skip)
    val userIds = attemptedTotal.map(_._1)
    val users = attemptedTotal flatMap { case (userId, _) =>
      UserLocalServiceHelper().fetchUser(userId)
    }

    val lessonsNotRequiredReview = lessons.filter(!_.requiredReview)
    val finishedTotalNotRequiredReview =
      userResult.getAttemptedTotal(lessonsNotRequiredReview.map(_.id), userIds, isFinished = true).toMap

    val lessonsRequiredReview = lessons.filter(_.requiredReview)

    val finishedTotalRequiredReview =
      lessonGradeService.getUsersGradesByLessons(users, lessonsRequiredReview) filter {
        _.state == Some(LessonStates.Finished)
      } groupBy {
        _.user.getUserId
      } map {
        case (userId, seq) => (userId, seq.length)
      }

    attemptedTotal flatMap { case (userId, attemptedCount) =>
      UserLocalServiceHelper().fetchUser(userId) map { user =>
        AttemptedLessonsRow(
          user.getUserId,
          user.getFullName,
          attemptedCount,
          finishedTotalNotRequiredReview.getOrElse(userId, 0) + finishedTotalRequiredReview.getOrElse(userId, 0)
        )
      }
    }
  }

  override def getCertificateReport(startDateValue: DateTime,
                                    endDateValue: DateTime,
                                    companyId: Long,
                                    userIds: Seq[Long])(implicit ec: ExecutionContext): Future[Seq[CertificateReportRow]] = {
    val stepsCount = Days.daysBetween(startDateValue, endDateValue).getDays

    dateReport.getDayChangesReport(
      companyId,
      userIds,
      startDateValue,
      stepsCount
    ) map { points =>
      points
        .groupBy(_.date)
        .map {
          case (date, dateStates) =>
            CertificateReportRow(
              date,
              dateStates.find(_.status == CertificateStatuses.Success).map(_.count) getOrElse 0,
              dateStates.find(_.status == CertificateStatuses.InProgress).map(_.count) getOrElse 0
            )
        }.toSeq.sortBy(_.date)
    }
  }
}
