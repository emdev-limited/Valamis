package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.lrs.tincan.LanguageMap

/**
  * Created by mminin on 03.03.15.
  */

case class GoalsStatusResponse(
                                courses: Iterable[CourseStatusResponse],
                                activities: Iterable[ActivityStatusResponse],
                                statements: Iterable[StatementStatusResponse],
                                packages: Iterable[PackageStatusResponse],
                                assignments: Iterable[AssignmentStatusResponse]
                              )

case class ActivityStatusResponse(id: Long,
                                  activityName: String,
                                  status: String,
                                  dateFinish: String,
                                  title: String)

case class CourseStatusResponse(id: Long,
                                courseId: Long,
                                status: String,
                                dateFinish: String,
                                title: String)

case class StatementStatusResponse(
                                    id: Long,
                                    obj: String,
                                    objName: Option[LanguageMap],
                                    verb: String,
                                    status: String,
                                    dateFinish: String
                                  )

case class PackageStatusResponse(id: Long,
                                 packageId: Long,
                                 status: String,
                                 dateFinish: String,
                                 title: String)

case class AssignmentStatusResponse(id: Long, assignmentId: Long, status: String, dateFinish: String)
