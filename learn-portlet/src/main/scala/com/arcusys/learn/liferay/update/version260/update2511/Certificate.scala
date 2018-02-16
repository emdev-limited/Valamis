package com.arcusys.learn.liferay.update.version260.update2511

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

/**
 * Certificate to be passed by user. Contains list of sites.
 *
 * @param id                  Unique internal ID
 * @param title               Short title
 * @param description         More detailed description
 */
case class Certificate(id: Long,
                       title: String,
                       description: String,
                       logo: String = "",
                       isPermanent: Boolean = true,
                       isPublishBadge: Boolean = false,
                       @deprecated
                       shortDescription: String = "",
                       companyId: Long,
                       validPeriodType: PeriodType = PeriodTypes.UNLIMITED,
                       validPeriod: Int = 0,
                       createdAt: DateTime,
                       isPublished: Boolean = false,
                       @deprecated
                       scope: Option[Long] = None)






