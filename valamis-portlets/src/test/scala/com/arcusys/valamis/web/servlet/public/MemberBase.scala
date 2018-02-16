package com.arcusys.valamis.web.servlet.public

import com.arcusys.valamis.lesson.service.LessonMembersService
import com.arcusys.valamis.lesson.service.impl.LessonMembersServiceImpl
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.user.model.User
import com.arcusys.valamis.user.service.UserService
import com.escalatesoft.subcut.inject.NewBindingModule

/**
  * Created by pkornilov on 2/20/17.
  */
trait MemberBase { self: LessonServletBaseTest =>

  private val users = Map(
    1L -> User(1, "Vanya"),
    2L -> User(2, "Petya"),
    3L -> User(3, "Anatoliy")
  )

  private val userGroups = Map(
    1L -> Member(1, "Group 1"),
    2L -> Member(2, "Group 2"),
    3L -> Member(3, "Group 3")
  )

  private val roles = Map(
    1L -> Member(1, "Role 1"),
    2L -> Member(2, "Role 2"),
    3L -> Member(3, "Role 3")
  )

  private val organizations = Map(
    1L -> Member(1, "Organization 1"),
    2L -> Member(2, "Organization 2"),
    3L -> Member(3, "Organization 3")
  )

  def initMocks(): Unit = {
    val _userService = mock[UserService]
    (_userService.getWithDeleted _).expects(*).onCall { id: Long =>
      users(id)
    }.anyNumberOfTimes()

    val _memberService = mock[MemberService]
    (_memberService.getMembers(_: Seq[Long], _: Boolean, _: MemberTypes.Value, _: Long,
      _: Option[String], _: Boolean, _: Option[SkipTake])).expects(*, true, *, companyId, None, true, None).onCall {
      (ids: Seq[Long], _: Boolean, tpe: MemberTypes.Value, _: Long, _: Option[String], _: Boolean, _: Option[SkipTake]) =>
        val members = tpe match {
          case MemberTypes.UserGroup => ids map userGroups
          case MemberTypes.Role => ids map roles
          case MemberTypes.Organization => ids map organizations
        }
        RangeResult(members.size, members)
    }.anyNumberOfTimes()

    _bindingModule <~ new NewBindingModule(fn = implicit module => {
      module.bind[LessonMembersService] toSingle new LessonMembersServiceImpl(db, driver) {

        override protected def getCompanyIdByCourseId(courseId: Long): Long = companyId

        lazy val memberService = _memberService
        lazy val userService = _userService
      }
    })
  }

}
