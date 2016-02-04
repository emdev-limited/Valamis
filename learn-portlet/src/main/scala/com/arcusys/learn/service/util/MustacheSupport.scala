package com.arcusys.learn.service.util

import com.arcusys.valamis.util.mustache.Mustache

trait MustacheSupport {

  def getRealPath(path: String): String

  def mustache(viewModel: Any, templatePath: String, partialPaths: Map[String, String] = Map()): String = {
    val rootTemplate = mustacheTemplate(templatePath)
    val partialTemplates = partialPaths.map { case (key, path) => (key, mustacheTemplate(path)) }

    rootTemplate.render(viewModel, partialTemplates)
  }

  def mustacheTemplate(templatePath: String) = {
    val path = getRealPath(templatePath)

    new Mustache(scala.io.Source.fromFile(path).mkString)
  }
}