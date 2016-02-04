package com.arcusys.valamis.util.serialization

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

object JsonHelper {

  def fromJson[T](raw: String)(implicit man: Manifest[T], formats: Formats = DefaultFormats): T = {
    parse(raw, useBigDecimalForDouble = true).extract[T]
  }

  def fromJson[T](raw: String, serializer: Serializer[_])(implicit man: Manifest[T]): T = {
    implicit val jsonFormats = DefaultFormats + serializer
    fromJson(raw)
  }

  def toJson[T](obj: T)(implicit formats: Formats = DefaultFormats): String = compact(render(Extraction.decompose(obj)))

  def toJson[T](obj: T, serializer: Serializer[_])(implicit man: Manifest[T]): String = {
    implicit val jsonFormats = DefaultFormats + serializer
    toJson(obj)
  }

  implicit class StringOpts(val json: String) extends AnyVal {
    def parseTo[A](implicit ev: Manifest[A], fs: Formats = DefaultFormats): A = JsonHelper.fromJson(json)
  }

  implicit class AnyOpts(val any: Any) extends AnyVal {
    def toJson(implicit fs: Formats = Serialization.formats(NoTypeHints)): String = JsonHelper.toJson(any)
  }
}

