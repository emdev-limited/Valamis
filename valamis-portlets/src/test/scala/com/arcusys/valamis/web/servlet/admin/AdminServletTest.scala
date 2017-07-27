package com.arcusys.valamis.web.servlet.admin

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrssupport.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.web.configuration.database.DatabaseInit
import com.arcusys.valamis.web.configuration.ioc.LrsSupportConfiguration
import com.arcusys.valamis.web.portlet.base.PermissionBase
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}
import org.scalatest.{BeforeAndAfter, FunSuiteLike}
import org.scalatra.test.scalatest._
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

class AdminServletTest
  extends ScalatraSuite
    with FunSuiteLike
    with BeforeAndAfter
    with SlickDbTestBase {

  implicit val companyId = 1L

  def slickDbInfo: SlickDBInfo = new SlickDBInfo {
    override def databaseDef: JdbcBackend#DatabaseDef = db
    override def slickProfile: JdbcProfile = driver
    override def slickDriver: JdbcDriver = driver
  }

  val adminServlet = new AdminServlet(){
    override def checkCSRFToken: Unit = Unit
    override def requirePortletPermission(permission: PermissionBase, portlets: PortletName*): Unit = Unit
    override implicit val bindingModule: BindingModule = new NewBindingModule(fn = implicit module => {
      module <~ new LrsSupportConfiguration(slickDbInfo)
    })
    override def getCompanyId = 1L
  }

  addServlet(adminServlet, "/*")

  before {
    createDB()

    new DatabaseInit(slickDbInfo).init()


    //add settings for registered lrs
    adminServlet.endpointService.setEndpoint {
      LrsEndpoint("/valamis-lrs-portlet/xapi", AuthType.INTERNAL, "appId", "appSecret", None)
    }
  }

  after {
    dropDB()
  }

  test("switch to internal lrs") {
    adminServlet.endpointService.setEndpoint {
      LrsEndpoint("/valamis-lrs-portlet/xapi", AuthType.OAUTH, "appId", "appSecret", None)
    }

    post("/administering/settings/lrs?isExternalLrs=false") {
      status shouldEqual 204

      val lrsSetting = adminServlet.endpointService.getEndpoint
      lrsSetting.isDefined shouldBe true

      lrsSetting.get.customHost shouldEqual None
      lrsSetting.get.auth shouldEqual AuthType.INTERNAL
    }
  }

  test("switch to internal lrs with custom host") {
    adminServlet.endpointService.setEndpoint {
      LrsEndpoint("/valamis-lrs-portlet/xapi", AuthType.OAUTH, "appId", "appSecret", None)
    }

    post("/administering/settings/lrs?isExternalLrs=false&internalLrsCustomHost=http://localhost:8080") {
      status shouldEqual 204

      val lrsSetting = adminServlet.endpointService.getEndpoint
      lrsSetting.isDefined shouldBe true

      lrsSetting.get.customHost shouldEqual Some("http://localhost:8080")
      lrsSetting.get.auth shouldEqual AuthType.INTERNAL
    }
  }
}
