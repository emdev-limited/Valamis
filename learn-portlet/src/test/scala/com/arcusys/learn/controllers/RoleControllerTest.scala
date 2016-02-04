//package com.arcusys.learn.controllers
//
////import org.scalatra.test.scalatest.{ ScalatraSuite }
//import org.scalamock.scalatest.MockFactory
//import com.arcusys.learn.test.mocks.MockInjectableFactory
//import com.arcusys.learn.service.util.SessionHandlerContract
//import org.mockito.{ Mockito }
//import com.arcusys.learn.models.request.{ CrudActionType, RoleRequest }
//import com.arcusys.learn.scorm.tracking.model.{ PermissionType }
//import javax.servlet.http.{ HttpServletResponse }
//import org.scalamock.proxy.ProxyMockFactory
//import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
//import com.arcusys.learn.facades.RoleFacadeContract
//import com.arcusys.learn.mocks.Mocks
//import com.arcusys.valamis.util.JsonSupport
//import com.arcusys.learn.controllers.api.RoleApiController
//
///**
// * Created by Iliya Tryapitsin on 05.03.14.
// */
////class RoleControllerTest extends FlatSpec with ScalatraSuite with MockFactory with BeforeAndAfterEach with ProxyMockFactory with MockInjectableFactory with JsonSupport {
//
//  override def beforeEach() {
//    Mockito.reset(Mocks.roleFacade, Mocks.sessionHandlerContract)
//  }
//
//  bindingModule.modifyBindings(module => {
//    module.bind[SessionHandlerContract] toSingle Mocks.sessionHandlerContract
//    module.bind[RoleFacadeContract] toSingle Mocks.roleFacade
//
//    addServlet(new RoleApiController(module), "")
//  })
//
//  behavior of "RoleController"
//  ignore should "return roles for permission" in {
//    Mocks.RoleFacade.getForPermissionStub()
//
//    get("/%s".format(PermissionType.TEACHER.toString)) {
//      status should equal(HttpServletResponse.SC_OK)
//      Mocks.RoleFacade.getForPermissionVerify()
//    }
//  }
//
//  ignore should "create new role" in {
//    val params = Map[String, String](
//      RoleRequest.PERMISSION -> Mocks.RoleFacade.permissionType.toString,
//      RoleRequest.IS_Default -> Mocks.RoleFacade.isDefault.toString,
//      RoleRequest.LIFERAY_ROLE_ID -> Mocks.RoleFacade.liferayRoleId.toString,
//      RoleRequest.ACTION -> CrudActionType.Add.toString)
//
//    Mocks.RoleFacade.createStub()
//
//    post("/", params) {
//      status should equal(HttpServletResponse.SC_OK)
//
//      Mocks.RoleFacade.createVerify()
//    }
//  }
//
//  ignore should "update role" in {
//    val params = Map[String, String](
//      RoleRequest.PERMISSION -> Mocks.RoleFacade.permissionType.toString,
//      RoleRequest.IS_Default -> Mocks.RoleFacade.isDefault.toString,
//      RoleRequest.ACTION -> CrudActionType.Update.toString)
//
//    Mocks.RoleFacade.updateStub()
//
//    post("/%s".format(Mocks.RoleFacade.roleId.toString), params) {
//      status should equal(HttpServletResponse.SC_OK)
//
//      Mocks.RoleFacade.updateVerify()
//    }
//  }
//
//  ignore should "delete role" in {
//    val params = Map[String, String](
//      RoleRequest.PERMISSION -> Mocks.RoleFacade.permissionType.toString,
//      RoleRequest.IS_Default -> Mocks.RoleFacade.isDefault.toString,
//      RoleRequest.ACTION -> CrudActionType.Delete.toString)
//
//    Mocks.SessionHandler.setTeacherPermissions()
//
//    post("/%s".format(Mocks.RoleFacade.roleId.toString), params) {
//      status should equal(HttpServletResponse.SC_NO_CONTENT)
//
//      Mocks.RoleFacade.deleteVerify()
//
//      Mockito.reset(Mocks.sessionHandlerContract)
//    }
//  }
//}