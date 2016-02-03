package com.arcusys.learn.notifications

import java.util.UUID

import com.arcusys.learn.facades.{CertificateFacadeContract, CourseFacadeContract, UserFacadeContract}
import com.arcusys.learn.models._
import com.arcusys.learn.models.response.certificates._
import com.arcusys.learn.notifications.MessageTemplateLoader.MessageTemplate
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.grade.service.CourseGradeService
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.service.{LessonStatementReader, ValamisPackageService}
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.model.RangeResult
import com.arcusys.valamis.settings.model.SettingType
import com.arcusys.valamis.settings.storage.SettingStorage
import com.escalatesoft.subcut.inject.NewBindingModule
import com.liferay.portal.kernel.mail.MailMessage
import com.liferay.portal.model.impl.GroupImpl
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class CourseMessageServiceTest extends FlatSpec with BeforeAndAfter with Matchers with MockitoSugar {

  private val usersMock = mock[UserFacadeContract]
  private val packagesMock = mock[ValamisPackageService]
  private val coursesMock = mock[CourseFacadeContract]
  private val certificatesMock = mock[CertificateFacadeContract]
  private val settingsMock = mock[SettingStorage]
  private val lrsReaderMock = mock[LrsClientManager]
  private val courseGradeServiceMock = mock[CourseGradeService]
  private val courseServiceMock = mock[CourseService]
  private val statementReaderMock = mock[LessonStatementReader]


  def service(loader: MessageTemplateLoader = validTemplates) = new CourseMessageService with TestingMessageSender {
    override implicit def bindingModule = new NewBindingModule({ implicit module =>
      import module._
      bind[MessageTemplateLoader].toSingle(loader)
      bind[UserFacadeContract].toSingle(usersMock)
      bind[ValamisPackageService].toSingle(packagesMock)
      bind[CourseFacadeContract].toSingle(coursesMock)
      bind[CertificateFacadeContract].toSingle(certificatesMock)
      bind[SettingStorage].toSingle(settingsMock)
      bind[LrsClientManager].toSingle(lrsReaderMock)
      bind[CourseGradeService].toSingle(courseGradeServiceMock)
      bind[LessonStatementReader].toSingle(statementReaderMock)
    })
  }

  behavior of "CourseMessageService"

  /*it should "send messages to teachers whose courses are started by newly enrolled students" in {
    // mocks setup
    Mocks(noGrades = true)

    // send notification
    val srv = service()
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "send messages to teachers if enrolled students have completed some packages within courses owned by teachers" in {
    // mocks setup
    Mocks(noEnrollments = true)

    // send notification
    val srv = service()
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "send messages to students if certificate is about to expire in 14 days" in {
    // mocks setup
    Mocks(expirationInDays = 14)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(2)
  }
  it should "send messages to students if certificate is about to expire in 7 days" in {
    // mocks setup
    Mocks(expirationInDays = 7)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(2)
  }
  it should "send messages to students if certificate is about to expire in today" in {
    // mocks setup
    Mocks(expirationInDays = 0)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(2)
  }
  it should "send messages to students if certificate' goals are about to expire in 14 days" in {
    // mocks setup
    Mocks(deadlineInDays = 14)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "send messages to students if certificate' goals are about to expire in 7 days" in {
    // mocks setup
    Mocks(deadlineInDays = 7)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "send messages to students if certificate' goals are about to expire in today" in {
    // mocks setup
    Mocks(deadlineInDays = 0)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }

  it should "not send any messages if teacher's email address is invalid" in {
    // mocks setup
    Mocks(invalidTeacherEmail = true)

    // send notification
    val srv = service()
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any messages if student's email address is invalid" in {
    // mocks setup
    Mocks(invalidStudentEmail = true)

    // send notification
    val srv = service()
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }

  it should "not send any message if its 'CourseCertificateExpiration' template is invalid" in {
    // mocks setup
    Mocks()

    // send notification
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "not send any message if its 'CourseCertificateDeadline' template is invalid" in {
    // mocks setup
    Mocks()

    // send notification
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(2)
  }
  it should "not send any message if its 'EnrolledStudent' template is invalid" in {
    // mocks setup
    Mocks()

    // send notification
    val srv = service(invalidateEnrolledStudents)
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }
  it should "not send any message if its 'FinishedLearningModule' template is invalid" in {
    // mocks setup
    Mocks()

    // send notification
    val srv = service(invalidateCompletedPackages)
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(1)
  }

  it should "not send any message to a teacher if there are no students enrolled on his/her courses" in {
    // mocks setup
    Mocks(noEnrollments = true)

    // send notification
    val srv = service(invalidateCompletedPackages)
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a teacher if there are no packages that are completed by students" in {
    // mocks setup
    Mocks(noGrades = true)

    // send notification
    val srv = service(invalidateEnrolledStudents)
    srv.dropAttemptNumber()
    srv.sendCourseMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate is not going to expire within 14 days" in {
    // mocks setup
    Mocks(expirationInDays = 17)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate is not going to expire within 7 days" in {
    // mocks setup
    Mocks(expirationInDays = 4)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate is not going to expire today" in {
    // mocks setup
    Mocks(expirationInDays = 2)

    // send notifications
    val srv = service(invalidateGoalsDeadline)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate' goals are not going to deadline within 14 days" in {
    // mocks setup
    Mocks(deadlineInDays = 16)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate' goals are not going to deadline within 7 days" in {
    // mocks setup
    Mocks(deadlineInDays = 6)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }
  it should "not send any message to a student if his/her certificate' goals are not going to deadline in today" in {
    // mocks setup
    Mocks(deadlineInDays = 1)

    // send notifications
    val srv = service(invalidateCertificateExpiration)
    srv.dropAttemptNumber()
    srv.sendCertificateMessages()

    // verify results
    srv.getAttemptNumber should be(0)
  }*/

  object Mocks {
    val course = CourseResponse(1111L, "Courage Course", "http://www.example.com/course.1", "test your courage by cleaning up a dungeon")

    val group = new GroupImpl()// CourseResponse(1111L, "Courage Course", , )
    group.setGroupId(1111L)
    group.setName("Courage Course")
    group.setFriendlyURL("http://www.example.com/course.1")
    group.setDescription("test your courage by cleaning up a dungeon")

    val manifest1 = Manifest(1, None, None, "1.2", None, resourcesBase = None, title = "Package 1", isDefault = true, courseId = Some(course.id.toInt), visibility = Some(true), beginDate = None, endDate = None)
    val manifest2 = Manifest(2, None, None, "1.2", None, resourcesBase = None, title = "Package 2", isDefault = true, courseId = Some(course.id.toInt), visibility = Some(true), beginDate = None, endDate = None)
    val manifest3 = Manifest(3, None, None, "1.2", None, resourcesBase = None, title = "Package 3", isDefault = true, courseId = Some(course.id.toInt), visibility = Some(false), beginDate = None, endDate = None)

    def apply(
      invalidTeacherEmail: Boolean = false,
      invalidStudentEmail: Boolean = false,
      expirationInDays: Int = 14,
      deadlineInDays: Int = 14,
      noGrades: Boolean = false,
      noEnrollments: Boolean = false) = {
      //      Mocks.setupStudents(invalidStudentEmail)
      //      Mocks.setupTeachers(invalidTeacherEmail)
      Mocks.setupCourses()
      Mocks.setupCertificates(expirationInDays)
      Mocks.setupPackages()
      Mocks.setupCoursesForTeachers()
      Mocks.setupStatements(noEnrollments)
      Mocks.setupPackageGrades(noGrades)
      Mocks.setupCompanies()
      Mocks.setupCertificateGoals(deadlineInDays)
      Mocks.setupSettings()
    }

    //    def setupStudents(invalidateEmail: Boolean = false) {
    //      if (!invalidateEmail) {
    //        when(usersMock.byPermission(PermissionType.STUDENT))
    //          .thenReturn(Seq(
    //            UserShortResponse(111L, "Test Student 1", email = Some("test.student1@example.com")),
    //            UserShortResponse(222L, "Test Student 2", email = Some("test.student2@example.com")),
    //            UserShortResponse(333L, "Test Student 3", email = Some("test.student3@example.com"))
    //          ))
    //      } else {
    //        when(usersMock.byPermission(PermissionType.STUDENT))
    //          .thenReturn(Seq(
    //            UserShortResponse(111L, "Test Student 1", email = None),
    //            UserShortResponse(222L, "Test Student 2", email = None),
    //            UserShortResponse(333L, "Test Student 3", email = None)
    //          ))
    //      }
    //    }
    //
    //    def setupTeachers(invalidateEmail: Boolean = false) {
    //      if (invalidateEmail) {
    //        when(usersMock.byPermission(PermissionType.TEACHER))
    //          .thenReturn(Seq(UserShortResponse(666L, "Test Teacher", email = None)))
    //      } else {
    //        when(usersMock.byPermission(PermissionType.TEACHER))
    //          .thenReturn(Seq(UserShortResponse(666L, "Test Teacher", email = Some("test.teacher@example.com"))))
    //      }
    //    }

    def setupCourses() {
      when(courseServiceMock.getAll).thenReturn(Seq(group))
      when(coursesMock.getCourse(1111)).thenReturn(course)
    }

    def setupPackages() {
      when(packagesMock.getByCourse(course.id))
        .thenReturn(Seq( manifest1, manifest2, manifest3))
    }

    def setupStatements(noEnrollments: Boolean = false) {
      if (!noEnrollments) {
        when(statementReaderMock.getLast(1, manifest1)).thenReturn(None)
          .thenReturn(Some(
            Statement(Option(UUID.randomUUID()), null, null, null, None, None, DateTime.now, DateTime.now.minusHours(3), None, None, Nil)
          ))
        when(statementReaderMock.getLast(2, manifest1)).thenReturn(None)

        when(statementReaderMock.getLast(1, manifest2))
          .thenReturn(Some(
            Statement(Option(UUID.randomUUID()), null, null, null, None, None, DateTime.now, DateTime.now.minusHours(6), None, None, Nil)
          ))
        when(statementReaderMock.getLast(2, manifest2)).thenReturn(None)

        when(statementReaderMock.getLast(1, manifest3))
          .thenReturn(Some(
            Statement(Option(UUID.randomUUID()), null, null, null, None, None, DateTime.now, DateTime.now.minusDays(2), None, None, Nil)
          ))
        when(statementReaderMock.getLast(2, manifest3)).thenReturn(None)
      } else {
        when(statementReaderMock.getLast(1, manifest1)).thenReturn(None)
        when(statementReaderMock.getLast(2, manifest1)).thenReturn(None)

        when(statementReaderMock.getLast(1, manifest2)).thenReturn(None)
        when(statementReaderMock.getLast(2, manifest2)).thenReturn(None)

        when(statementReaderMock.getLast(1, manifest3)).thenReturn(None)
        when(statementReaderMock.getLast(2, manifest3)).thenReturn(None)
      }

    }

    def setupCoursesForTeachers() {
      when(coursesMock.getByUserId(666L)).thenReturn(Seq(course))
    }

    def setupCompanies() {
      when(courseServiceMock.getCompanyIds).thenReturn(Seq(1234L, 5678L))
    }

    def setupCertificates(expirationInDays: Int) {
      when(certificatesMock.getForUser(111, 1234, false))
        .thenReturn(RangeResult[CertificateResponseContract](1,
          Seq(CertificateResponse(
            11, "Test Certificate 1", "", "", "", false, ValidPeriod(Some(expirationInDays + 2), "DAYS"), DateTime.now.minusDays(1), false, Nil, Nil, Nil, Nil, Map.empty, None
          ))
        ))
      when(certificatesMock.getForUser(111, 5678,false))
        .thenReturn(RangeResult[CertificateResponseContract](1,
        Seq(CertificateResponse(
            22, "Test Certificate 2", "", "", "", false, ValidPeriod(Some(expirationInDays + 2), "DAYS"), DateTime.now.minusDays(1), false, Nil, Nil, Nil, Nil, Map.empty, None
        ))
        ))
      when(certificatesMock.getForUser(222, 1234, false)).thenReturn(RangeResult[CertificateResponseContract](0,Nil))
      when(certificatesMock.getForUser(222, 5678, false)).thenReturn(RangeResult[CertificateResponseContract](0,Nil))

      when(certificatesMock.getForUser(333, 1234, false))
        .thenReturn(RangeResult[CertificateResponseContract](1,
        Seq(CertificateResponse(
            11, "Test Certificate 1", "", "", "", false, ValidPeriod(Some(expirationInDays + 2), "DAYS"), DateTime.now.minusDays(1), false, Nil, Nil, Nil, Nil, Map.empty, None
        ))
        ))
      when(certificatesMock.getForUser(333, 5678, false)).thenReturn(RangeResult[CertificateResponseContract](0,Nil))

    }

    def setupPackageGrades(noGrades: Boolean = false) {
      //TODO uncomment and fix
      //      if (!noGrades) {
      //        when(packagesMock.getPackageGrade(111, 1))
      //          .thenReturn(Option(PackageGrade(111L, 1L, "12", "", Some(DateTime.now.minusHours(3)))))
      //        when(packagesMock.getPackageGrade(111, 2))
      //          .thenReturn(Option(PackageGrade(111L, 2L, "24", "", Some(DateTime.now.minusHours(4)))))
      //        when(packagesMock.getPackageGrade(111, 3))
      //          .thenReturn(Option(PackageGrade(111L, 3L, "36", "", Some(DateTime.now.minusHours(2)))))
      //        when(packagesMock.getPackageGrade(222, 1)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(222, 2)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(222, 3)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(333, 1))
      //          .thenReturn(Option(PackageGrade(333L, 1L, "36", "", Some(DateTime.now.minusHours(3)))))
      //        when(packagesMock.getPackageGrade(333, 2)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(333, 3)).thenReturn(None)
      //      } else {
      //        when(packagesMock.getPackageGrade(111, 1)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(111, 2)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(111, 3)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(222, 1)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(222, 2)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(222, 3)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(333, 1)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(333, 2)).thenReturn(None)
      //        when(packagesMock.getPackageGrade(333, 3)).thenReturn(None)
      //      }
    }

    def setupCertificateGoals(deadlineInDays: Int) {
      when(certificatesMock.getGoalsDeadlines(11, 111))
        .thenReturn(
          GoalsDeadlineResponse(
            activities = Seq(ActivityGoalDeadlineResponse("activity1", Option(DateTime.now().plusDays(deadlineInDays + 1)))),
            courses = Seq(CourseGoalDeadlineResponse(1111, Option(DateTime.now().plusDays(deadlineInDays + 1)))),
            statements = Seq(StatementGoalDeadlineResponse("The test", "is completed", Option(DateTime.now().plusDays(deadlineInDays + 1)))),
            packages = Seq(PackageGoalDeadlineResponse(1234, Option(DateTime.now().plusDays(deadlineInDays + 1))))
          )
        )
      when(certificatesMock.getGoalsDeadlines(22, 222))
        .thenReturn(
          GoalsDeadlineResponse(
            activities = Nil,
            courses = Nil,
            statements = Seq(StatementGoalDeadlineResponse("The test", "is completed", Option(DateTime.now().plusDays(deadlineInDays + 1)))),
            packages = Nil
          )
        )
      when(certificatesMock.getGoalsDeadlines(11, 333))
        .thenReturn(
          GoalsDeadlineResponse(activities = Nil, courses = Nil, statements = Nil, packages = Nil)
        )
      when(certificatesMock.getGoalsDeadlines(22, 333))
        .thenReturn(
          GoalsDeadlineResponse(activities = Nil, courses = Nil, statements = Nil, packages = Nil)
        )
      when(certificatesMock.getGoalsDeadlines(22, 111))
        .thenReturn(
          GoalsDeadlineResponse(activities = Nil, courses = Nil, statements = Nil, packages = Nil)
        )
      when(certificatesMock.getGoalsDeadlines(11, 222))
        .thenReturn(
          GoalsDeadlineResponse(activities = Nil, courses = Nil, statements = Nil, packages = Nil)
        )
    }

    def setupSettings() {
      when(settingsMock.getByKey(SettingType.SendMessages)).thenReturn(None)
    }

  }

  private def invalidateCertificateExpiration =
    new MessageTemplateLoader {
      def getFor(mtype: MessageType.Value): Option[MessageTemplate] = mtype match {
        case MessageType.CourseCertificateExpiration =>
          None
        case _ =>
          Option(MessageTemplate("testing...", mtype, "testing..."))
      }
      def render(tpl: MessageTemplate, data: Map[String, _]): String = ""
    }

  private def invalidateGoalsDeadline =
    new MessageTemplateLoader {
      def getFor(mtype: MessageType.Value): Option[MessageTemplate] = mtype match {
        case MessageType.CourseCertificateDeadline =>
          None
        case _ =>
          Option(MessageTemplate("testing...", mtype, "testing..."))
      }
      def render(tpl: MessageTemplate, data: Map[String, _]): String = ""
    }

  private def invalidateEnrolledStudents =
    new MessageTemplateLoader {
      def getFor(mtype: MessageType.Value): Option[MessageTemplate] = mtype match {
        case MessageType.EnrolledStudent =>
          None
        case _ =>
          Option(MessageTemplate("testing...", mtype, "testing..."))
      }
      def render(tpl: MessageTemplate, data: Map[String, _]): String = ""
    }

  private def invalidateCompletedPackages =
    new MessageTemplateLoader {
      def getFor(mtype: MessageType.Value): Option[MessageTemplate] = mtype match {
        case MessageType.FinishedLearningModule =>
          None
        case _ =>
          Option(MessageTemplate("testing...", mtype, "testing..."))
      }
      def render(tpl: MessageTemplate, data: Map[String, _]): String = ""
    }

  private def validTemplates =
    new MessageTemplateLoader {
      def getFor(mtype: MessageType.Value): Option[MessageTemplate] = mtype match {
        case _ => Option(MessageTemplate("testing...", mtype, "testing..."))
      }
      def render(tpl: MessageTemplate, data: Map[String, _]): String = ""
    }
}

trait TestingMessageSender extends MessageSender {
  @volatile private var attemptNumber: Int = 0

  override protected def sendMessage(m: MailMessage) {
    attemptNumber += 1
  }

  def areMessagesSent = attemptNumber != 0
  def dropAttemptNumber() { attemptNumber = 0 }
  def getAttemptNumber = attemptNumber
}