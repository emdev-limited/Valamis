package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.valamis.gradebook.model.Statistic
import com.arcusys.valamis.web.servlet.course.CourseResponse

case class CourseWithStatisticResponse(course: CourseResponse,
                                       statistic: Statistic)
