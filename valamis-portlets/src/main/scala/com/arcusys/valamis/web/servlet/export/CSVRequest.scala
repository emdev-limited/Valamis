package com.arcusys.valamis.web.servlet.export

import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseCollectionRequest, Parameter}
import org.scalatra.{ScalatraBase, ScalatraServlet}

object CSVRequest extends BaseCollectionRequest {
  val Guid = "guid"
  val Name = "name"
  val Actor = "actor"
  val Activity = "activity"
  val Verb = "verb"
  val Format = "format"
  val Since = "since"
  val Until = "until"
  val Registration = "registration"
  val RelatedAgents = "related_agents"
  val RelatedActivities = "related_activities"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    def guid: String = Parameter(Guid).required
    def name: String = Parameter(Name).required
    def countOption: Option[Long] = Parameter(BaseCollectionRequest.Count).longOption
    def actor: Option[String] = Parameter(Actor).option
    def activity = Parameter(Activity).option
    def verb = Parameter(Verb).option
    def format = Parameter(Format).option
    def since = Parameter(Since).option
    def until = Parameter(Until).option
    def registration = Parameter(Registration).option
    def relatedAgents = Parameter(RelatedAgents).option
    def relatedActivities = Parameter(RelatedActivities).option
  }
}
