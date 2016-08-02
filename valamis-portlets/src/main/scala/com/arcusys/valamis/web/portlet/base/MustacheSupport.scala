package com.arcusys.valamis.web.portlet.base

import javax.portlet.GenericPortlet

import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.util.mustache.Mustache

import scala.io.Source

trait MustacheSupport {
  self: GenericPortlet =>
  def resourceReader: ResourceReader

  def mustache(viewModel: Any, templatePath: String, partialPaths: Map[String, String] = Map()): String = {
    val rootTemplate = mustacheTemplate(templatePath)
    val partialTemplates = partialPaths.map { case (key, path) => (key, mustacheTemplate(path)) }

    rootTemplate.render(viewModel, partialTemplates)
  }

  def mustacheTemplate(templatePath: String): Mustache = {
    val stream = resourceReader.getResourceAsStream(this, templatePath)
    new Mustache(Source.fromInputStream(stream).mkString)
  }
}