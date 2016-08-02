package com.arcusys.learn.models.request

import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest}
import org.scalatra.{ScalatraBase, ScalatraServlet}

/**
 * Created by Iliya Tryapitsin on 29.05.2014.
 */
object DashboardRequest extends BaseRequest {
  val CompanyId = "companyID"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(val scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {

  }
}


