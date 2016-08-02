package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.model.PeriodTypes

/**
 * Created by Iliya Tryapitsin on 02.06.2014.
 */
case class CertificateWithUserStatusResponse(
    id: Long,
    title: String,
    shortDescription: String,
    description: String,
    logo: String,
    isPublished: Boolean,
    periodType: PeriodTypes.PeriodType,
    periodValue: Int,
    courseCount: Int,
    statementCount: Int,
    activityCount: Int,
    packageCount: Int,
    assignmentCount: Int,
    userCount: Int,
    status: Option[String],
    isJoined: Boolean = true,
    isOpenBadgesIntegration: Boolean = false) extends CertificateResponseContract
