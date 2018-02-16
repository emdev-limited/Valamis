package com.arcusys.valamis.web.servlet.public

import java.util.Locale
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model.{Lesson, LessonLimit, LessonType}
import com.arcusys.valamis.lesson.scorm.service.ScormPackageService
import com.arcusys.valamis.lesson.service.{CustomLessonService, LessonAssetHelper, LessonNotificationService, LessonService}
import com.arcusys.valamis.lesson.service.impl.{LessonLimitServiceImpl, LessonServiceImpl}
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.web.configuration.ioc.LessonConfiguration
import com.arcusys.valamis.web.portlet.base.PermissionBase
import com.arcusys.valamis.web.servlet.base.ServletTestBase
import com.escalatesoft.subcut.inject.{BindingModule, MutableBindingModule, NewBindingModule}
import org.joda.time.DateTime
import org.json4s.JValue
import org.scalamock.scalatest.MockFactory

case class LessonTestData(
   lesson: Lesson,
   limit: Option[LessonLimit],
   members: Seq[(Long, MemberTypes.Value)]
)

/**
  * Created by pkornilov on 2/3/17.
  */
class LessonServletBaseTest extends ServletTestBase with MockFactory {

  protected val companyId = 20116L

  protected val courseId = 20147L
  protected val otherCourseId = 20148L

  protected val userId = 20333L

  val tagServiceMock = mock[TagService[Lesson]]
  var assetHelperStub: LessonAssetHelper = stub[LessonAssetHelper]

  protected val _bindingModule: MutableBindingModule = new MutableBindingModule {
    this <~ new NewBindingModule (fn = implicit module => {
      module <~ new LessonConfiguration(slickDbInfo)
    })
  }

  addServlet(new LessonServlet() {

    override def getLessonLinkBasePath: String = "/c"

    override def checkCSRFToken: Unit = {}

    override def requirePortletPermission(permission: PermissionBase, portlets: PortletName*): Unit = {}

    override implicit val bindingModule: BindingModule = _bindingModule

    override def getCompanyId = companyId

    override def getUserId = userId

    override protected lazy val tagService: TagService[Lesson] = tagServiceMock

    override protected lazy val lessonService: LessonService = _lessonService

    override implicit def locale: Locale = new Locale("fi")
  }, "/*")

  before {
    initDatabase()
    withExpectations {
      (assetHelperStub.updatePackageAssetEntry _).when(*).returns(-1)
    }
  }

  after {
    dropDB()
  }

  protected def lessonLink(id: Long) =
    s"/c/portal/learn-portlet/open_package?oid=$id"

  protected def createLesson(cId: Long)(lessonData: LessonTestData): DateTime = {
    val lesson = lessonData.lesson
    val created = _lessonService.create(lesson.lessonType, cId, lesson.title, lesson.description,
      lesson.ownerId, Some(lesson.scoreLimit), lesson.requiredReview)

    val id = created.id
    val creationDate = created.creationDate

    _lessonService.update(lesson.copy(id = id, courseId = cId))

    if (lesson.isVisible.isEmpty) {
      //TODO VALAMIS_API add members
    }

    lessonData.limit map (_.copy(lessonId = id)) foreach lessonLimitService.setLimit
    creationDate
  }

  def createSampleLesson(): DateTime = {
    createLesson(courseId)(LessonTestData(
      lesson = Lesson(id = -1,
        lessonType = LessonType.Tincan,
        title = "Lesson 1",
        description = "Lesson 1 desc",
        logo = Some("logo.png"),
        courseId = -1,
        isVisible = None, //custom visibility
        beginDate = Some(new DateTime("2017-01-17T00:00:00Z")),
        endDate = Some(new DateTime("2017-02-16T00:00:00Z")),
        ownerId = userId,
        creationDate = DateTime.now,
        requiredReview = false,
        scoreLimit = 0.7
      ),
      limit = None,
      members = Seq()
    ))
  }

  protected def noLessonResponse(id: Long): JValue = parse(
    s"""
      |{
      | "code":-1,
      | "message":"Lesson with id $id doesn't exist"
      |}
    """.stripMargin)

  private val _lessonService: LessonService = new LessonServiceImpl(db, driver) {

    //mock only some services
    lazy val tagService: TagService[Lesson] = tagServiceMock
    lazy val assetHelper: LessonAssetHelper = assetHelperStub

    lazy val socialActivityHelper: SocialActivityHelper[Lesson] = stub[SocialActivityHelper[Lesson]]

    lazy val userService: UserLocalServiceHelper = new UserLocalServiceHelper {
      override def getUsers(userIds: Seq[Long]) = Nil
    }

    //other services are real
    lazy val ratingService: RatingService[Lesson] = _bindingModule.inject[RatingService[Lesson]](None)
    lazy val fileService: FileService = _bindingModule.inject[FileService](None)
    lazy val fileStorage: FileStorage = _bindingModule.inject[FileStorage](None)
    lazy val lessonNotificationService = _bindingModule.inject[LessonNotificationService](None)
    lazy val customLessonServices: Map[LessonType, CustomLessonService] =
      Map[LessonType.LessonType, CustomLessonService](
        LessonType.Tincan -> _bindingModule.inject[TincanPackageService](None),
        LessonType.Scorm -> _bindingModule.inject[ScormPackageService](None)
      )
  }

  protected val lessonLimitService = new LessonLimitServiceImpl(db, driver)

  implicit class JValueExt(value: JValue) {

    def withoutCreationDate: JValue = {
      value removeField {
        case ("creationDate", _) => true
        case _ => false
      }
    }

  }
}
