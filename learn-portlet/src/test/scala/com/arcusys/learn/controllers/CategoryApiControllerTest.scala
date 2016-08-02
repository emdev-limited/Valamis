//package com.arcusys.learn.controllers
//
////import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
//import org.scalatra.test.scalatest.ScalatraSuite
//import org.scalamock.scalatest.MockFactory
//import org.scalamock.proxy.ProxyMockFactory
//import com.arcusys.learn.test.mocks.MockInjectableFactory
//import com.arcusys.valamis.utils.JsonSupport
//import com.arcusys.learn.service.util.SessionHandlerContract
//import com.arcusys.learn.mocks.Mocks
//import com.arcusys.learn.facades.{ CategoryFacadeContract }
//import com.arcusys.learn.controllers.api.{ CategoryApiController }
//import com.arcusys.learn.models.request.{ CategoryActionType, CategoryRequest }
//import org.mockito.Mockito
//import javax.servlet.http.HttpServletResponse
//
///**
// * Created by Iliya Tryapitsin on 11.04.2014.
// */
//
//@RunWith(classOf[org.scalatest.junit.JUnitRunner])
//class CategoryApiControllerTest extends FlatSpec with ScalatraSuite with BeforeAndAfterEach with MockFactory with ProxyMockFactory with MockInjectableFactory with JsonSupport {
//
//  override def beforeEach() {
//    Mockito.reset(Mocks.categoryFacade, Mocks.sessionHandlerContract)
//  }
//
//  bindingModule.modifyBindings(module => {
//    module.bind[SessionHandlerContract] toSingle Mocks.sessionHandlerContract
//    module.bind[CategoryFacadeContract] toSingle Mocks.categoryFacade
//
//    addServlet(new CategoryApiController(module), "")
//  })
//
//  behavior of "CategoryApiController"
//  ignore should "return 401 status" in {
//    get("/") {
//      status should equal(401)
//      Mocks.SessionHandler.checkTeacherPermissionsVerify()
//    }
//  }
//
//  /* "CategoryApiController" should "return categories" in {
//
//    Mocks.CategoryFacade.getForCourseIdStub()
//
//    get("/") {
//      body should not equal ""
//      status should equal(HttpServletResponse.SC_OK)
//
//      Mocks.CategoryFacade.getForCourseIdVerify(None)
//    }
//  }
//
//  "CategoryApiController" should "return categories for course" in {
//    Mocks.CategoryFacade.getForCourseIdStub()
//
//    get("/", params = Seq(CategoryRequest.CourseId -> Mocks.CategoryFacade.courseId.toString)) {
//      body should not equal ""
//      status should equal(200)
//
//      Mocks.CategoryFacade
//        .getForCourseIdVerify(
//          Option(Mocks.CategoryFacade.courseId))
//    }
//  }*/
//
//  //TODO: unkomment
//  behavior of "CategoryApiController"
//  ignore should "return child categories" in {
//    Mocks.CategoryFacade.getChildStub()
//
//    val params = Seq(
//      CategoryRequest.CourseId -> Mocks.CategoryFacade.courseId.toString,
//      CategoryRequest.ParentId -> Mocks.CategoryFacade.parentId.toString)
//
//    get("/", params = params) {
//      body should not equal ""
//      status should equal(HttpServletResponse.SC_OK)
//
//      Mocks.CategoryFacade.getChildVerify()
//    }
//  }
//
//  ignore should "return child categories with questions" in {
//    Mocks.CategoryFacade.getChildWithQuestionsStub()
//
//    val params = Seq(
//      CategoryRequest.CourseId -> Mocks.CategoryFacade.courseId.toString,
//      CategoryRequest.Action -> CategoryActionType.WITH_QUESTIONS.toString,
//      CategoryRequest.Questions -> "1",
//      CategoryRequest.Questions -> "2",
//      CategoryRequest.Categories -> "1",
//      CategoryRequest.Categories -> "2")
//
//    get("/child/" + Mocks.CategoryFacade.parentId, params = params) {
//      status should equal(200)
//      body should not equal ""
//
//      Mocks.CategoryFacade.getChildWithQuestionsVerify()
//    }
//  }
//
//  ignore should "create new category" in {
//    Mocks.CategoryFacade.createStub()
//
//    val params = Map[String, String](
//      CategoryRequest.Title -> Mocks.CategoryFacade.title,
//      CategoryRequest.Description -> Mocks.CategoryFacade.description,
//      CategoryRequest.CourseId -> Mocks.CategoryFacade.courseId.toString,
//      CategoryRequest.Action -> CategoryActionType.ADD.toString,
//      CategoryRequest.ParentId -> Mocks.CategoryFacade.parentId.toString)
//
//    val headers = Map[String, String]()
//
//    post("/", params, headers) {
//      status should equal(200)
//      body should not equal ""
//
//      Mocks.CategoryFacade.createVerify()
//    }
//  }
//
//  ignore should "update exist category" in {
//    Mocks.CategoryFacade.updateStub()
//
//    val params = Map[String, String](
//      CategoryRequest.Title -> Mocks.CategoryFacade.title,
//      CategoryRequest.Description -> Mocks.CategoryFacade.description,
//      CategoryRequest.Action -> CategoryActionType.UPDATE.toString,
//      CategoryRequest.Id -> Mocks.CategoryFacade.parentId.toString)
//
//    val headers = Map[String, String]()
//
//    post("/", params, headers) {
//      status should equal(200)
//      body should not equal ""
//
//      Mocks.CategoryFacade.updateVerify()
//    }
//  }
//
//  ignore should "delete exist category" in {
//
//    Mocks.SessionHandler.setTeacherPermissions()
//
//    val params = Map[String, String](
//      CategoryRequest.Action -> CategoryActionType.DELETE.toString,
//      CategoryRequest.Id -> Mocks.CategoryFacade.parentId.toString)
//
//    val headers = Map[String, String]()
//
//    post("/", params, headers) {
//      status should equal(200)
//      body should not equal ""
//
//      Mocks.CategoryFacade.deleteVerify()
//    }
//  }
//}
