package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.valamis.certificate.model.{AssignmentFilter, AssignmentStatuses, UserStatuses}
import com.arcusys.valamis.certificate.service.AssignmentService
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.certificate.request.AssignmentRequest
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

class AssignmentServlet extends BaseJsonApiController {

  private lazy val assignmentService = inject[AssignmentService]
  private lazy val req = AssignmentRequest(this)
  implicit override val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(UserStatuses) + DateTimeSerializer

  get("/assignments(/)") {
    val status = req.status.map(AssignmentStatuses.withName)
    val filter = AssignmentFilter(req.textFilter, req.groupId, status, req.sort)
    assignmentService.getBy(filter, req.skipTake)
  }

  get("/assignments/:id(/)") {
    assignmentService.getById(req.id)
  }

  get("/assignments/:id/users(/)") {
    assignmentService.getAssignmentUsers(req.id, req.skipTake, req.textFilter)
  }

  get("/assignments/course/:groupId/user/:userId(/)") {
    assignmentService.getUserAssignments(req.userId, req.groupId, req.skipTake, req.sort, req.textFilter)
  }

  get("/assignments/all-courses/user/:userId(/)") {
    assignmentService.getUserAssignments(req.userId, skipTake = req.skipTake, sortBy = req.sort, textFilter = req.textFilter)
  }
}