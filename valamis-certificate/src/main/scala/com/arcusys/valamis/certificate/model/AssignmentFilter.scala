package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.certificate.AssignmentSort

case class AssignmentFilter(titlePattern: Option[String] = None,
                            groupId: Option[Long] = None,
                            status: Option[AssignmentStatuses.Value] = None,
                            sortBy: Option[AssignmentSort] = None)