package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses.LLayoutSetPrototype

/**
  * Created by amikhailov on 23.11.16.
  */
object TemplateConverter {
  def toResponse(template: LLayoutSetPrototype): TemplateResponse =
    TemplateResponse(
      template.getLayoutSetPrototypeId,
      template.getNameCurrentValue
    )
}