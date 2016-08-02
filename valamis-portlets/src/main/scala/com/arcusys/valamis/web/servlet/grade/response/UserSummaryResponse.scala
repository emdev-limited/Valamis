package com.arcusys.valamis.web.servlet.grade.response

/**
 * Created by igorborisov on 18.06.15.
 */
case class UserSummaryResponse(
          certificatesReceived: Int,
          lessonsCompleted: Int,
          learningGoalsAchived: Int,
          certificatesInProgress: Int,
          piedata: Seq[PieData])

case class PieData(label: String, value: Int)