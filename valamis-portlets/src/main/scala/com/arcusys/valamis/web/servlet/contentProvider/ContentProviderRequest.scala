package com.arcusys.valamis.web.servlet.contentProvider

import com.arcusys.valamis.web.servlet.request.{Parameter, BaseCollectionFilteredRequestModel, BaseCollectionFilteredRequest}
import org.scalatra.{ScalatraBase, ScalatraServlet}

/**
  * Created By:
  * User: zsoltberki
  * Date: 28.9.2016
  */
object ContentProviderRequest extends BaseCollectionFilteredRequest {
  val Id = "id"
  val Name = "name"
  val Description = "description"
  val Url = "url"
  val ImageUrl = "image"
  val Width = "width"
  val Height = "height"
  val CustomerKey = "customerKey"
  val CustomerSecret = "customerSecret"
  val IsPrivate = "isPrivate"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    def id = Parameter(Id).longRequired

    def name = Parameter(Name).required

    def description = Parameter(Description).option

    def url = Parameter(Url).required

    def width = Parameter(Width).intOption

    def height = Parameter(Height).intOption

    def customerKey = Parameter(CustomerKey).option

    def customerSecret = Parameter(CustomerSecret).option

    def isPrivate = Parameter(IsPrivate).booleanRequired

    def imageUrl = Parameter(ImageUrl).option
  }

}
