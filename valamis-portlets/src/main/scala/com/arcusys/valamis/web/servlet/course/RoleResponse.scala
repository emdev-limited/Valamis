package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses.LRole

/**
  * Created By:
  * User: zsoltberki
  * Date: 4.6.2016
  */
case class RoleResponse (
  id:Long,
  name:String,
  description:String
  )

object RoleConverter {
  def toResponse(role: LRole): RoleResponse =
    RoleResponse(
      role.getRoleId,
      role.getName,
      //TODO read the language from the request
      role.getDescription("en_Us", true)
    )
}
