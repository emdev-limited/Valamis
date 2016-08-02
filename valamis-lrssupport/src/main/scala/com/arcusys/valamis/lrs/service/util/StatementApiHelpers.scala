package com.arcusys.valamis.lrs.service.util

import java.net.URI

import com.arcusys.valamis.lrs.api.StatementApi
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.tincan.Statement

object StatementApiHelpers {
  implicit class apiWithFilter(val statementApi: StatementApi) extends AnyVal {

    def getByFilter(filter: StatementFilter): Seq[Statement] = {
      statementApi.getByParams(
        agent = filter.agent,
        verb = filter.verb.map(URI.create),
        activity = filter.activity.map(URI.create),
        registration = filter.registration,
        since = filter.since,
        until = filter.until,
        relatedActivities = filter.relatedActivities.getOrElse(false),
        relatedAgents = filter.relatedAgents.getOrElse(false),
        limit = filter.limit,
        format = filter.format.map(_.toString),
        attachments = filter.attachments.getOrElse(false),
        ascending = filter.ascending.getOrElse(false),
        offset = filter.offset
      ).get.statements
    }
  }
}