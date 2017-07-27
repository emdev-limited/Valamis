package com.arcusys.valamis.slide.service.lti.model

case class LTIData(uuid: String,
                   title: Option[String],
                   text: Option[String],
                   url: String,
                   width: Option[Int],
                   height: Option[Int],
                   returnType: String,
                   ltiStatus: Option[String] = None
                  ) {
  def this(uuid: String, ltiStatus: Option[String]) =
    this(uuid, None, None, "", None, None, "", ltiStatus)
}
