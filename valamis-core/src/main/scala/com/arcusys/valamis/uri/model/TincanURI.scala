package com.arcusys.valamis.uri.model

object TincanURIType extends Enumeration {
  type TincanURIType = Value
  val Activity = Value("activity")
  val Package = Value("package")
  val Course = Value("course")
  val Category = Value("category")
  val Verb = Value("verb")
}

case class TincanURI(
  uri: String,
  objId: String,
  objType: TincanURIType.TincanURIType,
  content: String)