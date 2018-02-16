package com.arcusys.valamis.web.servlet.liferay

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{QueryUtilHelper, WorkflowConstantsHelper}
import com.arcusys.learn.liferay.helpers.JournalArticleHelpers
import com.arcusys.learn.liferay.services.JournalArticleLocalServiceHelper
import com.arcusys.valamis.file.service.FileEntryService
import com.arcusys.valamis.model.RangeResult
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.liferay.request.LiferayRequest
import com.arcusys.valamis.web.servlet.liferay.response.FileEntryModel
import com.arcusys.valamis.web.servlet.request.Parameter
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import scala.collection.JavaConverters._

class LiferayServlet extends BaseJsonApiController  with JournalArticleHelpers {

  private lazy val fileService = inject[FileEntryService]

  get("/liferay/images(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getImages(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.filter,
      req.skip,
      req.count,
      req.ascending
    )

    CollectionResponse(
      req.page,
      result.records map toResponse,
      result.total
    )
  }
  get("/liferay/video(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getVideo(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.skip,
      req.count
    )

    CollectionResponse(
      req.page,
      result.records map toResponse,
      result.total
    )
  }

  get("/liferay/audio(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getAudio(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.skip,
      req.count
    )

    RangeResult(
      result.total,
      result.records map toResponse
    )
  }

  get("/liferay/article(/)") {
    JsonHelper.toJson(getJournalArticles.map(getMap))
  }

  private def toResponse(x: LDLFileEntry) = FileEntryModel(
      x.getFileEntryId,
      x.getTitle,
      x.getFolderId,
      x.getVersion,
      x.getMimeType,
      x.getGroupId,
      x.getUuid
  )

  private def getJournalArticles: Seq[LJournalArticle] = {
    val companyId = Parameter("companyID")(this).longRequired
    JournalArticleLocalServiceHelper.getCompanyArticles(companyId,
      WorkflowConstantsHelper.STATUS_APPROVED, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS).asScala
      // get last approved version
      .groupBy {
      article => (article.getArticleId, article.getGroupId)
    }.values.map { _.maxBy(_.getVersion) }.toSeq

    /*
    // will get only last version of article, ignore previous edits
    val subQuery = DynamicQueryFactoryUtil.forClass(classOf[JournalArticle], "articleSub", PortalClassLoaderUtil.getClassLoader)
      .add(PropertyFactoryUtil.forName("articleId").eqProperty("articleParent.articleId"))
      .setProjection(ProjectionFactoryUtil.max("id"))

    val query = DynamicQueryFactoryUtil.forClass(classOf[JournalArticle], "articleParent", PortalClassLoaderUtil.getClassLoader)
      .add(PropertyFactoryUtil.forName("id").eq(subQuery))
      .addOrder(OrderFactoryUtil.desc("createDate"))

    JournalArticleLocalServiceUtil.dynamicQuery(query).asScala.map(_.asInstanceOf[JournalArticle])
*/
  }
}
