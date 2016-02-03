package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model.{LessonLimit, LessonType}
import com.arcusys.valamis.lesson.scorm.storage.tracking.AttemptStorage
import com.arcusys.valamis.lesson.storage.LessonLimitStorage
import com.arcusys.valamis.model.PeriodTypes
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

class LessonLimitChecker(implicit val bindingModule: BindingModule) extends Injectable {

  private val statementReader = inject[LessonStatementReader]
  private val lessonLimitStorage = inject[LessonLimitStorage]
  private val attemptStorage = inject[AttemptStorage]
  private lazy val packageService = inject[ValamisPackageService]

  private def getLessonLimit(packageId: Long, lessonType: LessonType) = {
    lessonLimitStorage.getByID(packageId, lessonType)
  }

  /*
  Return true if checker failed
 */
  def checkScormPackage(user: LUser, packageId: Long): Boolean = {
    getLessonLimit(packageId, LessonType.Scorm) match {
      case Some(limit) =>
        val attemptsCount = attemptStorage.getAllComplete(user.getUserId.toInt, packageId).size
        val activityId = packageService.getScormRootActivityId(packageId)
        val lastTimestamps = statementReader.getLastAttempted(user, activityId).map(_.timestamp)
        checkPackage(user, activityId, limit, attemptsCount, lastTimestamps)
      case _ => true
    }
  }

  def checkTincanPackage(user: LUser, packageId: Long): Boolean = {
    getLessonLimit(packageId, LessonType.Tincan) match {
      case Some(limit) =>
        val activityId = packageService.getTincanRootActivityId(packageId)
        val statements = statementReader.getCompleted(user, activityId)
        val attemptsCount = statements.size
        val lastAttemptDate = statements.map(_.timestamp).headOption
        checkPackage(user, activityId, limit, attemptsCount, lastAttemptDate)
      case _ => true
    }
  }

  private def checkPackage(user: LUser, activityId: String, limit: LessonLimit, attemptsCount: Int, lastAttemptDate: Option[DateTime]): Boolean = {

    val passingLimitCheck = if (limit.passingLimit <= 0) true
    else attemptsCount < limit.passingLimit

    val intervalCheck = if (limit.rerunIntervalType == PeriodTypes.UNLIMITED) true
    else {
      lastAttemptDate match {
        case Some(date) =>
          val nextAttemptDate = limit.rerunIntervalType match {
            case PeriodTypes.DAYS  => date.plusDays(limit.rerunInterval)
            case PeriodTypes.MONTH => date.plusMonths(limit.rerunInterval)
            case PeriodTypes.WEEKS => date.plusWeeks(limit.rerunInterval)
            case PeriodTypes.YEAR  => date.plusYears(limit.rerunInterval)
          }
          DateTime.now.isAfter(nextAttemptDate)
        case None =>
          true
      }
    }
    passingLimitCheck && intervalCheck
  }

  def isTincanAttempted(user: LUser, packageId: Long): Boolean = {
    statementReader.getLastAttemptedTincan(user, packageId) isDefined
  }

  def isScormAttempted(userId: Long, packageId: Long): Boolean =
    attemptStorage.getAllComplete(userId.toInt, packageId.toInt).nonEmpty
}
