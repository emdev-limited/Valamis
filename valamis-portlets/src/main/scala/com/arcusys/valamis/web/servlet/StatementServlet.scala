package com.arcusys.valamis.web.servlet

import com.arcusys.valamis.certificate.model.CertificateSortBy
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.valamis.ActivityIdLanguageMap
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.certificate.request.CertificateRequest
import com.arcusys.valamis.web.servlet.certificate.response.AvailableStatementResponse
import com.arcusys.valamis.web.servlet.response.CollectionResponse

import scala.util.{Failure, Success}

class StatementServlet extends BaseJsonApiController {

  private lazy val lrsReader = inject[LrsClientManager]

  get("/statements(/)") {
    val req = CertificateRequest(this)
    val (sortTimeFirst, timeSort, nameSort) = req.sortBy match {
      case CertificateSortBy.Name => (false, false, req.ascending)
      case CertificateSortBy.CreationDate => (true, req.ascending, true)
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
