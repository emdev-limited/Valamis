package com.arcusys.valamis.user.model

import com.arcusys.valamis.model.{Order, SortBy}

case class UserFilter(companyId: Option[Long] = None,
                      namePart: String = "",
                      certificateId: Option[Long] = None,
                      groupId: Option[Long] = None,
                      organizationId: Option[Long] = None,
                      isUserJoined: Boolean = true, // take users joined / not-joined to the certificate
                      sortBy: Option[UserSort] = None)

object UserSortBy extends Enumeration {
  type UserSortBy = Value
  val Name, CreationDate, UserJoined = Value
  def apply(v: String): UserSortBy = v.toLowerCase match {
    case "name"         => Name
    case "creationdate" => CreationDate
    case "userjoined"   => UserJoined
    case _              => throw new IllegalArgumentException()
  }
}

case class UserSort(sortBy: UserSortBy.UserSortBy, order: Order.Value) extends SortBy(sortBy, order)
