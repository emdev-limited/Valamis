package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.member.model.MemberTypes

case class LessonViewer(lessonId: Long, viewerId: Long, viewerType: MemberTypes.Value)
