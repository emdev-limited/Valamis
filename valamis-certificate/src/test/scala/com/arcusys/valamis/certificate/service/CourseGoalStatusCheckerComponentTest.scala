package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.CertificateState
import com.arcusys.valamis.certificate.model.goal.{CertificateGoal, CourseGoal, GoalStatuses}
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CertificateGoalStateRepository, CertificateStateRepository, CourseGoalStorage}
import com.arcusys.valamis.gradebook.model.CourseGrade
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
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

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[TeacherCourseGradeService])
        when(courseGradeStorage.get(courseIdInProgress, userId)).thenReturn(None)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalInProgress))
        val goalStateRepository =  mock(classOf[CertificateGoalStateRepository])
        val goalRepository = mock(classOf[CertificateGoalRepository])

        def updateUserGoalState(userId: Long, goal: CertificateGoal, status: GoalStatuses.Value,
                                date: DateTime): (GoalStatuses.Value, DateTime) = {
          (GoalStatuses.InProgress, DateTime.now)
        }
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

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[TeacherCourseGradeService])
        when(courseGradeStorage.get(courseIdNotDone, userId)).thenReturn(None)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalNotDone))
        val goalStateRepository =  mock(classOf[CertificateGoalStateRepository])
        val goalRepository = mock(classOf[CertificateGoalRepository])

        def updateUserGoalState(userId: Long, goal: CertificateGoal, status: GoalStatuses.Value,
                                date: DateTime): (GoalStatuses.Value, DateTime) = {
          (GoalStatuses.InProgress, DateTime.now)
        }
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

      val courseGradeAfterDeadline = Some(mock(classOf[CourseGrade]))
      when(courseGradeAfterDeadline.get.grade).thenReturn(Some(2F))
      when(courseGradeAfterDeadline.get.date).thenReturn(DateTime.now.minusDays(1))

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[TeacherCourseGradeService])
        when(courseGradeStorage.get(courseIdDoneAfterDeadline, userId)).thenReturn(courseGradeAfterDeadline)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalDoneAfterDeadline))
        val goalStateRepository =  mock(classOf[CertificateGoalStateRepository])
        val goalRepository = mock(classOf[CertificateGoalRepository])

        def updateUserGoalState(userId: Long, goal: CertificateGoal, status: GoalStatuses.Value,
                                date: DateTime): (GoalStatuses.Value, DateTime) = {
          (GoalStatuses.InProgress, DateTime.now)
        }
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

      val courseGradeSuccess = Some(mock(classOf[CourseGrade]))
      when(courseGradeSuccess.get.grade).thenReturn(Some(2F))
      when(courseGradeSuccess.get.date).thenReturn(DateTime.now.minusDays(2))

      val checkerComponent = new CourseGoalStatusCheckerComponent {
        val certificateStateRepository = certificateStateRepositoryMock

        val courseGradeStorage = mock(classOf[TeacherCourseGradeService])
        when(courseGradeStorage.get(courseIdSuccess, userId)).thenReturn(courseGradeSuccess)

        val courseGoalStorage = mock(classOf[CourseGoalStorage])
        when(courseGoalStorage.getByCertificateId(certificateId)).thenReturn(Seq(courseGoalSuccess))
        val goalStateRepository =  mock(classOf[CertificateGoalStateRepository])
        val goalRepository = mock(classOf[CertificateGoalRepository])

        def updateUserGoalState(userId: Long, goal: CertificateGoal, status: GoalStatuses.Value,
                                date: DateTime): (GoalStatuses.Value, DateTime) = {
          (GoalStatuses.InProgress, DateTime.now)
        }
      }

      val courseGoalStatuses = checkerComponent.getCourseGoalsStatus(certificateId, userId)
      assert(courseGoalStatuses.length == 1)
      assert(courseGoalStatuses.head.goal == courseGoalSuccess)
      assert(courseGoalStatuses.head.status == GoalStatuses.Success)
    }
  }
}
