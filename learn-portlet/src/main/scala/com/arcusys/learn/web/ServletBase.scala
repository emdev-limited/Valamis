package com.arcusys.learn.web

import com.arcusys.learn.service.util.{OldParameterBase, Parameter}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.Injectable
import org.scalatra.ScalatraServlet

trait ServletBase extends ScalatraServlet with Injectable {

  @deprecated
  class JsonModelBuilder[T](transform: T => Map[String, Any]) {
    def apply(entity: T): String = JsonHelper.toJson(transform(entity))

    def apply(entities: Seq[T]): String = JsonHelper.toJson(entities.map(transform))

    def apply(entityOption: Option[T]): String = entityOption match {
      case Some(entity) => JsonHelper.toJson(transform(entity))
      case None         => halt(404, "Entity not found for given parameters")
    }

    def map(entity: T): Map[String, Any] = transform(entity)

    def map(entities: Seq[T]): Seq[Map[String, Any]] = entities.map(transform)
  }

  def parameter(name: String, scalatra: ScalatraServlet) = {
    implicit val _scalatra = scalatra
    Parameter(name)
  }

  @deprecated
  def parameter(name: String) = new OldParameterBase(name, this)
}
