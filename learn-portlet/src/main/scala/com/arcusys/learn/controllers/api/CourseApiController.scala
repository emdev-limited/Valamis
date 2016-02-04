package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.facades.CourseFacadeContract
import com.arcusys.learn.liferay.permission.PermissionUtil
import com.arcusys.learn.models.CourseConverter
import com.arcusys.learn.models.request.{CourseRequest, UserRequest}
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.valamis.course.CourseService

class CourseApiController extends BaseJsonApiController with CourseConverter {

  private lazy val courseService = inject[CourseService]
  private lazy val courseFacade = inject[CourseFacadeContract]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/courses(/)") {
    val courseRequest = CourseRequest(this)
    val courses =
      courseService.getAll(
        PermissionUtil.getCompanyId,
        courseRequest.skipTake,
        courseRequest.filter,
        courseRequest.isSortDirectionAsc)
      .map(toResponse)

    CollectionResponse(courseRequest.page, courses.items, courses.total)
  }

  get("/courses/my(/)") {
    val request = UserRequest(this)
    val result = courseFacade.getProgressByUserId(
      PermissionUtil.getUserId,
      request.skipTake,
      request.isSortDirectionAsc
    )

    CollectionResponse (
      request.page,
      result.items,
      result.total
    )
  }
}
