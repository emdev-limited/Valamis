package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.lrs.tincan.LanguageMap
import com.arcusys.valamis.web.servlet.course.CourseResponse

case class PackageGoalResponse(goalId: Long,
                               certificateId: Long,
                               packageId: Long,
                               title: String,
                               periodValue: Int,
                               periodType: String,
                               course: Option[CourseResponse],
                               isDeleted: Boolean = false,
                               arrangementIndex: Int,
                               isOptional: Boolean = false,
                               groupId: Option[Long])

case class StatementGoalResponse(goalId: Long,
                                 certificateId: Long,
                                 obj: String,
                                 objName: Option[LanguageMap],
                                 verb: String,
                                 periodValue: Int,
                                 periodType: String,
                                 arrangementIndex: Int,
                                 isOptional: Boolean = false,
                                 groupId: Option[Long])

case class ActivityGoalResponse(goalId: Long,
                                certificateId: Long,
                                count: Int,
                                activityName: String,
                                periodValue: Int,
                                periodType: String,
                                arrangementIndex: Int,
                                isOptional: Boolean = false,
                                groupId: Option[Long])

case class CourseGoalResponse(goalId: Long,
                              courseId: Long,
                              certificateId: Long,
                              title: String,
                              url: String,
                              periodValue: Int,
                              periodType: String,
                              arrangementIndex: Int,
                              lessonsAmount: Int,
                              isOptional: Boolean = false,
                              groupId: Option[Long])

case class AssignmentGoalResponse(goalId: Long,
                                  certificateId: Long,
                                  assignmentId: Long,
                                  title: String,
                                  periodValue: Int,
                                  periodType: String,
                                  isDeleted: Boolean = false,
                                  arrangementIndex: Int,
                                  isOptional: Boolean = false,
                                  groupId: Option[Long])