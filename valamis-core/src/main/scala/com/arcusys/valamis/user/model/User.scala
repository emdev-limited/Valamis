package com.arcusys.valamis.user.model

import com.arcusys.learn.liferay.LiferayClasses.LUser

case class User(id: Long, name: String) {
  def this(user: LUser) = {
    this(user.getUserId, user.getFullName)
  }
}


