package com.arcusys.learn.models.response.certificates

import com.arcusys.learn.models.CourseResponse
import com.arcusys.valamis.lrs.tincan.LanguageMap

case class PackageGoalResponse(
  certificateId: Long,
  packageId: Long,
  title: String,
  periodValue: Int,
  periodType: String,
  course: Option[CourseResponse],
  isDeleted:Boolean=false)

case class StatementGoalResponse(
  certificateId: Long,
  tincanStmntObj: String,
  tincanStmntObjName: Option[LanguageMap],
  tincanStmntVerb: String,
  periodValue: Int,
  periodType: String)

case class ActivityGoalResponse(
  certificateId: Long,
  activityCount: Int,
  activityId: String,
  periodValue: Int,
  periodType: String)

case class CourseGoalResponse(
  courseGoalId: Long,
  certificateId: Long,
  title: String,
  url: String,
  periodValue: Int,
  periodType: String,
  arrangementIndex: Int,
  lessonsAmount: Int)