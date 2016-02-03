package com.arcusys.learn.mocks

import java.nio.ByteBuffer
import com.arcusys.learn.facades._
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.models.Gradebook.StudentResponse
import com.arcusys.learn.models.request.CertificateRequest
import com.arcusys.learn.models.response.certificates.CertificateResponse
import com.arcusys.learn.models.response.users.{UserResponse, UserResponseUtils}
import com.arcusys.learn.models.{ ValidPeriod }
import com.arcusys.valamis.certificate.model.CertificateSortBy
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.gradebook.model.GradebookUserSortBy
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.settings.storage.SettingStorage
import com.liferay.portal.model.Organization
import org.joda.time.DateTime
import org.mockito.{ Matchers, Mockito }

import scala.collection.JavaConverters._
import scala.util.Random

object Mocks {
  val fileFacadeContract = Mockito.mock(classOf[FileFacadeContract])
  val settingStorage = Mockito.mock(classOf[SettingStorage])
  val courseStorage = Mockito.mock(classOf[CourseGradeStorage])
  val userLocalServiceHelper = Mockito.mock(classOf[UserLocalServiceHelper])
  val fileStorage = Mockito.mock(classOf[FileStorage])
  val certificateFacadeContract = Mockito.mock(classOf[CertificateFacadeContract])
  val gradebookFacade = Mockito.mock(classOf[GradebookFacadeContract])
  val userFacade = Mockito.mock(classOf[UserFacadeContract])
  val packageFacade = Mockito.mock(classOf[PackageFacadeContract])

  object General {
    val page = 1
    val count = 10
    def skip = (page - 1) * count
    val filter = ""
    val sortDirectionByAsc = true
    val skipTake = Some(new SkipTake(skip, count))
  }

  object GradebookFacade {
    val courseId = 1
    val studentId = 1

    def getStudentsStub(filter: String = "",
      orgFilter: String = "",
      isSortDirectionAsc: Boolean = true,
      isShortResult: Boolean = true) = Mockito
      .stub(gradebookFacade.getStudents(
        Matchers.eq(courseId),
        Matchers.eq(General.page),
        Matchers.eq(General.count),
        Matchers.eq(filter),
        Matchers.eq(orgFilter),
        Matchers.eq(GradebookUserSortBy.name),
        true
      ))
      .toReturn(
        Seq(
          StudentResponse(1, "Janne Hietala", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.1f, "comment", 1, 5, Seq()),
          StudentResponse(2, "Jussi Hurskainen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.2f, "comment", 2, 5, Seq()),
          StudentResponse(3, "Mika Kuikka", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().minusHours(1).toDateTimeISO.toString, 0.3f, "comment", 3, 5, Seq()),
          StudentResponse(4, "Juho Antilla", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().minusDays(1).toDateTimeISO.toString, 0.4f, "comment", 4, 5, Seq()),
          StudentResponse(5, "Timo Harstela", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().minusMinutes(10).toDateTimeISO.toString, 0.5f, "comment", 5, 5, Seq()),
          StudentResponse(6, "Timo Peltonen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().minusMinutes(15).toDateTimeISO.toString, 0.5f, "comment", 0, 5, Seq())))

    //      } else {
    //        val activity = Activity(
    //          "Activity", "activityId-666",
    //          None, None, None, None, None,
    //          Set(), Seq(), Seq(), Seq(), Seq(), Seq(), Seq()
    //        )
    //        val verb = Verb("verbId_test2", Map("en" -> "experienced"))
    //        val result = Result(Option(Score(1, None, None, None)), None, None, None, None, Seq())
    //        val actor = Agent("Agent", Option("Test test"), Some("mail@mail.com"), None, None, None)
    //        val statement = Statement(UUID.randomUUID(), actor, verb, activity, Some(result), None, None, None, None, None, Seq())
    //
    //        val packages = Seq(
    //          PackageGradeResponse(1, "Package 1", "description", false, 10, Seq(
    //            statement,
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID())
    //
    //          )),
    //          PackageGradeResponse(2, "Package 2", "description", false, 10, Seq(
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()),
    //            statement.copy(id = UUID.randomUUID()))
    //          )
    //        )
    //
    //        Seq(
    //          StudentResponse(1, "Janne Hietala", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.1f, "comment", 1, 5, packages),
    //          StudentResponse(2, "Jussi Hurskainen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.2f, "comment", 2, 5, packages),
    //          StudentResponse(3, "Mika Kuikka", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.3f, "comment", 3, 5, packages),
    //          StudentResponse(4, "Juho Antilla", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.4f, "comment", 4, 5, packages),
    //          StudentResponse(5, "Timo Harstela", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.5f, "comment", 5, 5, packages),
    //          StudentResponse(6, "Timo Peltonen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.5f, "comment", 0, 5, packages))
    //
    //      })

    //    def getExtStudentsStub(filter: String = "",
    //      orgFilter: String = "",
    //      packageIds: Seq[Int] = Seq(),
    //      isSortDirectionAsc: Boolean = true) = {
    //      val activity = Activity(
    //        "Activity", "activityId-666",
    //        None, None, None, None, None,
    //        Set(), Seq(), Seq(), Seq(), Seq(), Seq(), None
    //      )
    //      val verb = Verb("verbId_test2", Map("en" -> "experienced"))
    //      val result = Result(Option(Score(1, None, None, None)), None, None, None, None, None)
    //      val actor = Agent("Agent", Option("Test test"), Some("mail@mail.com"), None, None, None)
    //      val statement = Statement(UUID.randomUUID(), actor, verb, activity, Some(result), None, None, None, None, None, Seq())
    //
    //      val packages = Seq(
    //        PackageGradeResponse(1, "", "Package 1", "description", false, "10", JsonDeserializer.serializeStatementResult(StatementResult(Seq(
    //          statement,
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID())), ""))
    //        ),
    //        PackageGradeResponse(2, "", "Package 2", "description", false, "10", JsonDeserializer.serializeStatementResult(StatementResult(Seq(
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID()),
    //          statement.copy(id = UUID.randomUUID())), ""))
    //        )
    //      )
    //
    //      Mockito
    //        .stub(gradebookFacade.getExtStudents(
    //          Matchers.eq(courseId),
    //          Matchers.eq(General.page),
    //          Matchers.eq(General.count),
    //          Matchers.eq(filter),
    //          Matchers.eq(orgFilter),
    //          Matchers.any[Seq[Int]],
    //          Matchers.eq("name_asc")))
    //        .toReturn(Seq(
    //          StudentResponse(1, "Janne Hietala", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.1f, "comment", 1, 5, packages),
    //          StudentResponse(2, "Jussi Hurskainen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.2f, "comment", 2, 5, packages),
    //          StudentResponse(3, "Mika Kuikka", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.3f, "comment", 3, 5, packages),
    //          StudentResponse(4, "Juho Antilla", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.4f, "comment", 4, 5, packages),
    //          StudentResponse(5, "Timo Harstela", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.5f, "comment", 5, 5, packages),
    //          StudentResponse(6, "Timo Peltonen", "", Seq("Joensuu, Finland"), Seq("Arcusys Ltd."), DateTime.now().toDateTimeISO.toString, 0.5f, "comment", 0, 5, packages)))
    //    }

    def getGradesForStudentStub(filter: String = "",
      packageIds: Seq[Int] = Seq(),
      isSortDirectionAsc: Boolean = true) = Mockito
      .stub(gradebookFacade.getGradesForStudent(
        Matchers.eq(courseId),
        Matchers.eq(studentId),
        Matchers.eq(General.skip),
        Matchers.eq(General.count)))
      .toReturn(StudentResponse(1, "Test 1", "avatar url", Seq("address"), Seq(), "last modified", 0.5f, "comment", 1, 10, Seq()))

    def getStudentsVerify(filter: String = "",
      orgFilter: String = "",
      isSortDirectionAsc: Boolean = true,
      isShortResult: Boolean = true) = {
      Mockito
        .verify(gradebookFacade)
        .getStudents(
          Matchers.eq(courseId),
          Matchers.eq(General.skip),
          Matchers.eq(General.count),
          Matchers.eq(filter),
          Matchers.eq(orgFilter),
          Matchers.eq(GradebookUserSortBy.name),
          true
        )

      GradebookFacade
    }

    def getExtStudentsVerify(filter: String = "",
      orgFilter: String = "",
      packageIds: Seq[Int] = Seq(),
      isSortDirectionAsc: Boolean = true) = {
      Mockito
        .verify(gradebookFacade)
        .getStudents(
          Matchers.eq(courseId),
          Matchers.eq(General.page),
          Matchers.eq(General.count),
          Matchers.eq(filter),
          Matchers.eq(orgFilter),
          Matchers.eq(GradebookUserSortBy.name),
          true,
          true,
          Matchers.any[Seq[Long]]
        )

      GradebookFacade
    }

    def getGradesForStudentVerify(filter: String = "",
      packageIds: Seq[Int] = Seq(),
      isSortDirectionAsc: Boolean = true) = {
      Mockito
        .verify(gradebookFacade)
        .getGradesForStudent(
          Matchers.eq(courseId),
          Matchers.eq(studentId),
          Matchers.eq(General.page),
          Matchers.eq(General.count))

      GradebookFacade
    }
  }

  object UserLocalServiceHelper {

    def getUserStub(liferayFakeUser: LUser) = Mockito
      .stub(Mocks.userLocalServiceHelper.getUser(Matchers.any[Long]))
      .toReturn(liferayFakeUser)
  }

  object FileStorage {
    val contentLength = 100
  }

  object CourseStorage {
    val courseId = 1
    val userId = 1

    def getStub(courseId: Int = courseId, userId: Int = userId) = {
      Mockito
        .stub(courseStorage.get(Matchers.eq(courseId), Matchers.eq(userId)))
        .toReturn(Option(CourseGrade(courseId, userId, Some(.5F), "Test comment", Option(DateTime.now))))
    }

    def getVerify(courseId: Int = courseId, userId: Int = userId) = {
      Mockito
        .verify(courseStorage)
        .get(Matchers.eq(courseId), Matchers.eq(userId))
    }
  }

  object SessionHandler {
    def setTeacherPermissions() = { }

    def checkTeacherPermissionsVerify() = { }
  }

  object CertificateFacade {
    val allCertificates = List[CertificateResponse](
      CertificateResponse(1, "Test certificate 1", "Test short description", "Test description", "test logo", true, null, new DateTime(), true, List(), List(), List(), List(), Map("1" -> UserResponse(1000, "Test user", "", "")), None),
      CertificateResponse(2, "Test certificate 2", "Test short description", "Test description", "test logo", true, null, new DateTime(), true, List(), List(), List(), List(), Map(), None))
    val page = 1
    val count = 10
    val companyId = 100
    val scope = Some(1)
    val sortBy = CertificateSortBy.Name
    val isSortDirectionAsc = true
    val filter = ""
    val userId = 1000
    val certificateId = 1
    val rootUrl = "test root url"
    val id = 1
    val isShortResult = false
    val isOnlyPublished = false

    val title = "test title"
    val description = "test description"
    val logo = "test logo"
    val isPermanent = true

    val publishBadge = false
    val shortDescription = "test short description"

     def getByIdStub() = {
      Mockito
        .stub(
          certificateFacadeContract
            .getById(Matchers.eq(certificateId)))
        .toReturn(
          allCertificates
            .filter(x => x.id == certificateId)
            .head)

      CertificateFacade
    }

    def getByIdVerify() = {
      Mockito
        .verify(certificateFacadeContract)
        .getById(Matchers.eq(certificateId))
      CertificateFacade
    }

    def getForUserStub() = {
      Mockito
        .stub(certificateFacadeContract.getForUser(
          Matchers.eq(userId),
          Matchers.eq(companyId),
          Matchers.eq(isShortResult),
          Matchers.eq(isSortDirectionAsc),
          Matchers.eq(Some(SkipTake((page - 1) * count, count))),
          Matchers.eq(None),
          Matchers.eq(None)))
        .toReturn(RangeResult(1,allCertificates.filter(x => x.users.filter(u => u._2.id == userId).count(p => true) > 0)))

      CertificateFacade
    }

    def getForUserVerify() = {
      Mockito
        .verify(certificateFacadeContract)
        .getForUser(
          Matchers.eq(userId),
          Matchers.eq(companyId),
          Matchers.eq(isShortResult),
          Matchers.eq(isSortDirectionAsc),
          Matchers.eq(Some(SkipTake((page - 1) * count, count))),
          Matchers.eq(None),
          Matchers.eq(None))

      CertificateFacade
    }

    def createStub() = {
      Mockito
        .stub(
          certificateFacadeContract
            .create(
              Matchers.eq(companyId),
              Matchers.eq(userId),
              Matchers.eq(CertificateRequest.DefaultTitle),
              Matchers.eq(CertificateRequest.DefaultDescription)))
        .toReturn(allCertificates.last)

      CertificateFacade
    }

    def createVerify() = {
      Mockito
        .verify(certificateFacadeContract)
        .create(
          Matchers.eq(companyId),
          Matchers.eq(userId),
          Matchers.eq(CertificateRequest.DefaultTitle),
          Matchers.eq(CertificateRequest.DefaultDescription))

      CertificateFacade
    }

    def changeStub() = {
      Mockito
        .stub(
          certificateFacadeContract
            .change(
              Matchers.eq(id),
              Matchers.eq(title),
              Matchers.eq(description),
              Matchers.any(classOf[ValidPeriod]).toString,
              Option(Matchers.any(classOf[ValidPeriod]).asInstanceOf[Int]),
              Matchers.eq(publishBadge),
              Matchers.eq(shortDescription),
              Matchers.eq(companyId),
              Matchers.eq(userId),
              Matchers.any(classOf[Option[Long]])))
        .toReturn(allCertificates.last)

      CertificateFacade
    }

    def changeVerify() = {
      Mockito
        .verify(certificateFacadeContract)
        .change(
          Matchers.eq(id),
          Matchers.eq(title),
          Matchers.eq(description),
          Matchers.any(classOf[ValidPeriod]).toString,
          Option(Matchers.any(classOf[ValidPeriod]).asInstanceOf[Int]),
          Matchers.eq(publishBadge),
          Matchers.eq(shortDescription),
          Matchers.eq(companyId),
          Matchers.eq(userId),
          Matchers.any(classOf[Option[Long]]))

      CertificateFacade
    }
  }

  object FileFacade {
    val fileId = 1
    val unExistFileId = 2
    val courseID = 1
    val userID = 1
    val groupID = 1

    def doFileContentStub() = {
      val content = ByteBuffer
        .allocate(FileStorage.contentLength)
        .putInt(Random.nextInt())
        .array()

      /*   Mockito
        .stub(fileFacadeContract.getFileContent(Matchers.eq(fileId)))
        .toReturn(content)

      Mockito
        .when(fileFacadeContract.getFileContent(Matchers.eq(unExistFileId)))
        .thenThrow(new EntityNotFoundException())*/

      FileFacade
    }

    /* def doFileContentVerify(id: Int) = {
      Mockito
        .verify(fileFacadeContract)
        .getFileContent(Matchers.eq(id))

      FileFacade
    }*/
  }

  object UserFacade {
    val courseId = 1
    val isShortResponse = true

    val roleIds = Array(1l, 2l, 3l)
    val firstUser = Mockito.mock(classOf[LUser])
    val secondUser = Mockito.mock(classOf[LUser])
    val users = Seq(firstUser, secondUser)

    Mockito
      .stub(UserResponseUtils.getPortraitUrl(Matchers.any[LUser]))
      .toReturn("test portrait")

    Mockito
      .stub(firstUser.getRoleIds)
      .toReturn(roleIds)

    Mockito
      .stub(firstUser.getUserId)
      .toReturn(1)

    Mockito
      .stub(firstUser.getOrganizations())
      .toReturn(List(Mockito.mock(classOf[Organization])).asJava)

    Mockito
      .stub(firstUser.getFullName)
      .toReturn("First user")

    Mockito
      .stub(firstUser.getUserId)
      .toReturn(1)

    Mockito
      .stub(secondUser.getRoleIds)
      .toReturn(Array(2l, 3l))

  }
}
