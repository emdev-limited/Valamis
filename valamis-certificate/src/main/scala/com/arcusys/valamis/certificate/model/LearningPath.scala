package com.arcusys.valamis.certificate.model

import org.joda.time.{DateTime, Period}

/**
  * Created by pkornilov on 4/4/17.
  */
case class LearningPathWithVersion(learningPath: LearningPath,
                                   version: LPVersion)

case class LPInfo(id: Long,
                  companyId: Long,
                  activated: Boolean,
                  title: String,
                  description: Option[String],
                  logoUrl: Option[String])

case class LPInfoWithUserStatus(id: Long,
                                companyId: Long,
                                activated: Boolean,
                                title: String,
                                validPeriod: Option[Period],
                                description: Option[String],
                                logoUrl: Option[String],
                                status: Option[CertificateStatuses.Value],
                                statusDate: Option[DateTime],
                                progress: Option[Double])

case class LearningPath(id: Long,
                        activated: Boolean,
                        companyId: Long,
                        userId: Long,
                        hasDraft: Boolean,
                        currentVersionId: Option[Long])

case class LPVersion(learningPathId: Long,
                     title: String,
                     description: Option[String],
                     logo: Option[String],

                     courseId: Option[Long],
                     validPeriod: Option[Period],
                     expiringPeriod: Option[Period],

                     openBadgesEnabled: Boolean,
                     openBadgesDescription: Option[String],

                     published: Boolean,
                     createdDate: DateTime,
                     modifiedDate: DateTime)
