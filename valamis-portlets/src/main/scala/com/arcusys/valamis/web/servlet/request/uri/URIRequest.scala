package com.arcusys.valamis.web.servlet.request.uri

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseCollectionFilteredRequestModel, Parameter}
import org.apache.http.ParseException
import org.scalatra.{ScalatraBase, ScalatraServlet}

object URIRequest extends BaseCollectionFilteredRequest {
  val Type = "type"
  val Id = "id"
  val Content = "content"
  val PrefixURI = "prefix"
  val Action = "action"
  val Start = "start"
  val End = "end"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    def objectType = Parameter(Type).required
    def id = Parameter(Id).required
    def content = Parameter(Content).option
    def prefix = Parameter(PrefixURI).required
    def action = Parameter(Action).option
    def start = Parameter(Start).intOption
    def end = Parameter(End).intOption
    override def skipTake = {
      (start, end) match {
        case (Some(s), Some(e)) if e > s => Some(SkipTake(s, e - s))
        case (None, None) => None
        case _ => throw new ParseException("start or end parameter couldn't be parsed")
      }
    }
  }
}
