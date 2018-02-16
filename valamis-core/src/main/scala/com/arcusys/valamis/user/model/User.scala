package com.arcusys.valamis.user.model

import com.arcusys.learn.liferay.LiferayClasses.LUser

case class User(id: Long, name: String, isDeleted: Boolean = false) {
  def this(user: LUser) = {
    this(user.getUserId, user.getFullName)
  }
}


