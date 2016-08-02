package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.certificate.model.goal.Goal
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.web.servlet.course.CourseResponse

case class PackageGoalShortResponse(goalId: Long,
                                    certificateId: Long,
                                    packageId: Long,
                                    title: String,
                                    course: Option[CourseResponse],
                                    isDeleted: Boolean = false) extends Goal

case class StatementGoalShortResponse(goalId: Long,
                                      certificateId: Long,
                                      obj: String,
                                      objName: Option[LanguageMap],
                                      verb: String) extends Goal

case class CourseGoalShortResponse(goalId: Long,
                                   certificateId: Long,
                                   courseId: Long,
                                   title: String,
                                   url: String,
                                   lessonsAmount: Int,
                                   isDeleted: Boolean = false) extends Goal

case class AssignmentGoalShortResponse(goalId: Long,
                                       certificateId: Long,
                                       assignmentId: Long,
                                       title: String,
                                       isDeleted: Boolean = false) extends Goal