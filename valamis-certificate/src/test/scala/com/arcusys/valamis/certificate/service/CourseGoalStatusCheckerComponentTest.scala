package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.CertificateState
import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, CourseGoal}
import com.arcusys.valamis.certificate.storage.{CertificateStateRepository, CourseGoalStorage}
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.FunSpec

class CourseGoalStatusCheckerComponentTest extends FunSpec {
  //FIXME: To run the test comment PermissionHelper.preparePermissionChecker(userId)
  val certificateId = 1
  val userId = 1

  val certificateState = mock(classOf[CertificateState])
  when(certificateState.userJoinedDate).thenReturn(DateTime.now.minusDays(3))

  val certificateStateRepositoryMock = mock(classOf[CertificateStateRepository])
  when(certificateStateRepositoryMock.getBy(userId, certificateId)).thenReturn(Some(certificateState))

  describe("CourseGoalStatusCheckerComponent") {
    ignore("should mark inProgress in getCourseGoalsStatus") {
      val courseIdInProgress = 1
      val courseGoalInProgress = mock(classOf[CourseGoal])
      when(courseGoalInProgress.courseId).thenReturn(courseIdInProgress)
      when(courseGoalInProgress.periodValue).thenReturn(4)
      when(courseGoalInProgress.periodType).thenReturn(PeriodTypes.DAYS)

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[CourseGradeStorage])
        when(courseGradeStorage.get(courseIdInProgress, userId)).thenReturn(None)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalInProgress))
      }

      val courseGoalStatuses = checkerComponent.getCourseGoalsStatus(certificateId, userId)
      assert(courseGoalStatuses.length == 1)
      assert(courseGoalStatuses.head.goal == courseGoalInProgress)
      assert(courseGoalStatuses.head.status == GoalStatuses.InProgress)
    }

    ignore("should mark Failed when not done and time is out in getCourseGoalsStatus") {
      val courseIdNotDone = 2
      val courseGoalNotDone = mock(classOf[CourseGoal])
      when(courseGoalNotDone.courseId).thenReturn(courseIdNotDone)
      when(courseGoalNotDone.periodValue).thenReturn(1)
      when(courseGoalNotDone.periodType).thenReturn(PeriodTypes.DAYS)

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[CourseGradeStorage])
        when(courseGradeStorage.get(courseIdNotDone, userId)).thenReturn(None)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalNotDone))
      }

      val courseGoalStatuses = checkerComponent.getCourseGoalsStatus(certificateId, userId)
      assert(courseGoalStatuses.length == 1)
      assert(courseGoalStatuses.head.goal == courseGoalNotDone)
      assert(courseGoalStatuses.head.status == GoalStatuses.Failed)
    }

    ignore("should mark Failed when done after deadline out in getCourseGoalsStatus") {
      val courseIdDoneAfterDeadline = 3
      val courseGoalDoneAfterDeadline = mock(classOf[CourseGoal])
      when(courseGoalDoneAfterDeadline.courseId).thenReturn(courseIdDoneAfterDeadline)
      when(courseGoalDoneAfterDeadline.periodValue).thenReturn(1)
      when(courseGoalDoneAfterDeadline.periodType).thenReturn(PeriodTypes.DAYS)

      val courseGradeAfterDeadline = Some(mock(classOf[CourseGrade]))
      when(courseGradeAfterDeadline.get.grade).thenReturn(Some(2F))
      when(courseGradeAfterDeadline.get.date).thenReturn(Some(DateTime.now.minusDays(1)))

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[CourseGradeStorage])
        when(courseGradeStorage.get(courseIdDoneAfterDeadline, userId)).thenReturn(courseGradeAfterDeadline)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalDoneAfterDeadline))
      }

      val courseGoalStatuses = checkerComponent.getCourseGoalsStatus(certificateId, userId)
      assert(courseGoalStatuses.length == 1)
      assert(courseGoalStatuses.head.goal == courseGoalDoneAfterDeadline)
      assert(courseGoalStatuses.head.status == GoalStatuses.Failed)
    }

    ignore("should mark Success when done in time in getCourseGoalsStatus") {
      val courseIdSuccess = 4
      val courseGoalSuccess = mock(classOf[CourseGoal])
      when(courseGoalSuccess.courseId).thenReturn(courseIdSuccess)
      when(courseGoalSuccess.periodValue).thenReturn(2)
      when(courseGoalSuccess.periodType).thenReturn(PeriodTypes.DAYS)

      val courseGradeSuccess = Some(mock(classOf[CourseGrade]))
      when(courseGradeSuccess.get.grade).thenReturn(Some(2F))
      when(courseGradeSuccess.get.date).thenReturn(Some(DateTime.now.minusDays(2)))

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[CourseGradeStorage])
        when(courseGradeStorage.get(courseIdSuccess, userId)).thenReturn(courseGradeSuccess)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalSuccess))
      }

      val courseGoalStatuses = checkerComponent.getCourseGoalsStatus(certificateId, userId)
      assert(courseGoalStatuses.length == 1)
      assert(courseGoalStatuses.head.goal == courseGoalSuccess)
      assert(courseGoalStatuses.head.status == GoalStatuses.Success)
    }
  }
}
