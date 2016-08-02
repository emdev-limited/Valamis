package com.arcusys.valamis.member.model

object MemberTypes extends Enumeration {
  val User, UserGroup, Organization, Role = Value
}

case class Member(id: Long, name: String)
