package com.arcusys.valamis.hook.utils

/**
  * Created by Igor Borisov on 20.07.16.
  */

trait Info {
  val key: String
  val name: String
  val description: String
}

case class StructureInfo(key: String, name: String, description: String) extends Info

object StructureInfo{
  def apply(key: String, name: String): StructureInfo = StructureInfo(key, name, name + "-description")
}

case class TemplateInfo(key: String, name: String, description: String) extends Info

object TemplateInfo{
  def apply(key: String, name: String): TemplateInfo = TemplateInfo(key, name, name + "-description")
}