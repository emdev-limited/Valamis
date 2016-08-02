package com.arcusys.valamis.hook.utils

import com.liferay.portal.model.{Layout, LayoutTypePortlet}

import scala.collection.JavaConverters._

case class PageLayout(name: String,
                      title: String,
                      description: String,
                      friendlyUrl: String,
                      templateId: String,
                      isPrivate: Boolean,
                      children: Seq[PageLayout])

object PageLayout {
  val DefaultTemplateId = "1_column"

  def apply(name: String,
            templateId: String = DefaultTemplateId,
            children: Seq[PageLayout] = Seq()): PageLayout = {
    new PageLayout(
      name,
      title = name,
      description = "",
      friendlyUrl = "/" + name.toLowerCase.replaceAll(" ", "-"),
      templateId,
      isPrivate = false,
      children
    )
  }

  def apply(layout: Layout): PageLayout = {
    new PageLayout(
      layout.getName,
      layout.getTitle,
      layout.getDescription,
      layout.getFriendlyURL,
      layout.getLayoutType.asInstanceOf[LayoutTypePortlet].getLayoutTemplateId,
      layout.isPrivateLayout,
      layout.getChildren.asScala.map(PageLayout(_))
    )
  }
}