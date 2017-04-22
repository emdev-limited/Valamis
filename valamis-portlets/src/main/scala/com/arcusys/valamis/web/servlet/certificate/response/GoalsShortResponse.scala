package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.certificate.model.goal.Goal
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.web.servlet.course.CourseResponse
import org.joda.time.DateTime

case class PackageGoalShortResponse(goalId: Long,
                                    certificateId: Long,
                                    packageId: Long,
                                    title: String,
                                    course: Option[CourseResponse],
                                    isSubjectDeleted: Boolean = false,
                                    isDeleted: Boolean) extends Goal

case class StatementGoalShortResponse(goalId: Long,
                                      certificateId: Long,
                                      obj: String,
                                      objName: Option[LanguageMap],
                                      verb: String,
                                      isDeleted: Boolean) extends Goal

case class CourseGoalShortResponse(goalId: Long,
                                   certificateId: Long,
                                   courseId: Long,
                                   title: String,
                                   url: String,
                                   lessonsAmount: Int,
                                   isSubjectDeleted: Boolean = false,
                                   isDeleted: Boolean) extends Goal

case class AssignmentGoalShortResponse(goalId: Long,
                                       certificateId: Long,
                                       assignmentId: Long,
                                       title: String,
                                       isSubjectDeleted: Boolean = false,
                                       isDeleted: Boolean) extends Goal

case class ActivityGoalShortResponse(goalId: Long,
                                     certificateId: Long,
                                     count: Int,
                                     activityName: String,
                                     title: String,
                                     isDeleted: Boolean) extends Goal