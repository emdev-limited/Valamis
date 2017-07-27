package com.arcusys.valamis.web.servlet.lti

import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.slide.service.lti.LTIDataService
import com.arcusys.valamis.slide.service.lti.model.LTIData
import com.arcusys.valamis.web.configuration.InjectableSupport
import org.scalatra.{BadRequest, ScalatraServlet}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LTIPublicServlet
  extends ScalatraServlet
    with LogSupport
    with InjectableSupport {
  private lazy val ltiService = inject[LTIDataService]

  get("/:uuid") {

    val uuid = params("uuid")

    val returnType = params.get("return_type")
    val embedType = params.get("embed_type")


      val ltiReturnType = returnType orElse embedType getOrElse {
        Await.result(ltiService.add(new LTIData(uuid, Some("Error - cannot find return type"))), Duration.Inf)
        halt(BadRequest())
      }

      val url = params("url")
      val title = params.get("title")
      val text = params.get("text")
      val height = params.getAs[Int]("height")
      val width = params.getAs[Int]("width")

    Await.result(ltiService.add(LTIData(uuid, title, text, url, width, height, ltiReturnType)), Duration.Inf)
  }


}
