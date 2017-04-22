package com.arcusys.valamis.member.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.learn.liferay.services.{OrganizationLocalServiceHelper => OrganizationHelper}
import com.arcusys.learn.liferay.services.{RoleLocalServiceHelper => RoleHelper}
import com.arcusys.learn.liferay.services.{UserGroupLocalServiceHelper => UserGroupHelper}
import com.arcusys.learn.liferay.services.{UserLocalServiceHelper => UserHelper}
import com.arcusys.valamis.utils.SeqCutExtension

class MemberService {

  def getMembers(memberIds: Seq[Long],
                 contains: Boolean,
                 memberType: MemberTypes.Value,
                 companyId: Long,
                 namePattern: Option[String],
                 ascending: Boolean,
                 skipTake: Option[SkipTake]): RangeResult[Member] = {
    val nameLike = namePattern.filterNot(_.isEmpty).map(text => s"%$text%")
    val startEnd = skipTake.map(x => (x.skip, x.skip + x.take))

    memberType match {

      case MemberTypes.User =>
        val totalCount = UserHelper().getCount(memberIds, contains, companyId, nameLike)

        val items = UserHelper().getUsers(memberIds, contains, companyId, nameLike, ascending, startEnd, None)
          .map(u => Member(u.getUserId, u.getFullName))

        RangeResult(totalCount, items)

      case MemberTypes.UserGroup =>
        val totalCount = UserGroupHelper.getCount(memberIds, contains, companyId, nameLike)

        val items = UserGroupHelper.getUserGroups(memberIds, contains, companyId, nameLike, ascending, startEnd)
          .map(u => Member(u.getUserGroupId, u.getName))

        RangeResult(totalCount, items)

      case MemberTypes.Organization =>
        val totalCount = OrganizationHelper.getCount(memberIds, contains, companyId, nameLike)

        val items = OrganizationHelper.getOrganizations(memberIds, contains, companyId, nameLike, ascending, startEnd)
          .map(u => Member(u.getOrganizationId, u.getName))

        RangeResult(totalCount, items)

      case MemberTypes.Role =>
        val roleType = RoleHelper.RoleTypeRegular
        val totalCount = RoleHelper.getCount(memberIds, contains, companyId, nameLike, roleType)

        val items = RoleHelper.getRoles(memberIds, contains, companyId, nameLike, roleType, ascending, startEnd)
          .map(u => Member(u.getRoleId, u.getName))

        RangeResult(totalCount, items)
    }
  }

  def getUserMembers(memberIds: Seq[Long],
                     contains: Boolean,
                     companyId: Long,
                     namePattern: Option[String],
                     ascending: Boolean,
                     skipTake: Option[SkipTake],
                     organizationId: Option[Long]): RangeResult[LUser] = {

    val startEnd = skipTake.map(x => (x.skip, x.skip + x.take))
    val totalCount = UserHelper().getCount(memberIds, contains, companyId, nameLike(namePattern), organizationId)
    val items = UserHelper().getUsers(memberIds,
      contains,
      companyId,
      nameLike(namePattern),
      ascending,
      startEnd,
      organizationId)

    RangeResult(totalCount, items)
  }

  def getUserAndGroupMembers(memberIds: Seq[Long],
                             groupsIds: Seq[Long],
                             contains: Boolean,
                             companyId: Long,
                             namePattern: Option[String],
                             ascending: Boolean,
                             skipTake: Option[SkipTake],
                             organizationId: Option[Long]): RangeResult[LUser] = {

    val membersResult = UserHelper().getUsers(memberIds,
      contains,
      companyId,
      nameLike(namePattern),
      ascending,
      None,
      organizationId)


    val groupMembers = groupsIds.flatMap {
      getMembers(_, MemberTypes.UserGroup)
    }

    val members = (membersResult ++ groupMembers).distinct
    val total = members.size
    val items = if (ascending) {
      members.sortBy(u => u.getFullName)
    }
    else {
      members.sortBy(u => u.getFullName).reverse
    }

    RangeResult(total, items.skip(skipTake))
  }


  def getMembers(viewerId: Long, viewerType: MemberTypes.Value): Seq[LUser] = {
    viewerType match {
      case MemberTypes.User => Seq(UserHelper().getUser(viewerId))
      case MemberTypes.UserGroup =>
        UserHelper().getUserGroupUsers(viewerId)
      case MemberTypes.Organization =>
        UserHelper().getOrganizationUsers(viewerId)
      case MemberTypes.Role => UserHelper().getRoleUsers(viewerId)
    }
  }

  private def nameLike(namePattern: Option[String]) = namePattern.filterNot(_.isEmpty).map(text => s"%$text%")
}
