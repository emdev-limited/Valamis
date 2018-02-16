package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.lrs.tincan.LanguageMap
import com.arcusys.valamis.web.servlet.course.CourseResponse
import org.joda.time.DateTime

case class PackageGoalResponse(goalId: Long,
                               certificateId: Long,
                               packageId: Long,
                               title: String,
                               periodValue: Int,
                               periodType: String,
                               course: Option[CourseResponse],
                               isSubjectDeleted: Boolean = false,
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
                              isSubjectDeleted: Boolean = false,
                              arrangementIndex: Int,
                              lessonsAmount: Int,
                              isOptional: Boolean = false,
                              groupId: Option[Long])

case class TrainingEventGoalResponse(goalId: Long,
                                     certificateId: Long,
                                     eventId: Long,
                                     title: String,
                                     endDate: DateTime,
                                     periodValue: Int,
                                     periodType: String,
                                     isSubjectDeleted: Boolean = false,
                                     arrangementIndex: Int,
                                     isOptional: Boolean = false,
                                     groupId: Option[Long])