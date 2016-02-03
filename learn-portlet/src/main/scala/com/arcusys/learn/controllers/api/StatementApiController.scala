package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.models.request.CertificateRequest
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.models.response.certificates.AvailableStatementResponse
import com.arcusys.valamis.certificate.model.CertificateSortBy
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.valamis.ActivityIdLanguageMap

import scala.util.{Failure, Success}

class StatementApiController extends BaseJsonApiController {

  private lazy val lrsReader = inject[LrsClientManager]

  get("/statements(/)") {
    val req = CertificateRequest(this)
    val (sortTimeFirst, timeSort, nameSort) = req.sortBy match {
      case CertificateSortBy.Name => (false, false, req.isSortDirectionAsc)
      case CertificateSortBy.CreationDate => (true, req.isSortDirectionAsc, true)
      case _ => (false, false, true)
    }

    lrsReader.verbApi { api =>
      api.getWithActivities(
        Some(req.filter),
        req.count,
        (req.page - 1) * req.count,
        nameSort,
        timeSort,
        sortTimeFirst)
      match {
        case Success(v) =>
          val stmntResp = v.seq map { case ((verb, ActivityIdLanguageMap(id, Some(langMap)), date)) =>
            AvailableStatementResponse(verb.id, verb.display, id, langMap, date.toString)
          }
          CollectionResponse(req.page, stmntResp, v.count)

        case Failure(e) => throw e
      }
    }
  }
}
