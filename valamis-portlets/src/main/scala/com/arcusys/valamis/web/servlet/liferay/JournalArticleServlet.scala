package com.arcusys.valamis.web.servlet.liferay

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{QueryUtilHelper, WorkflowConstantsHelper}
import com.arcusys.learn.liferay.helpers.JournalArticleHelpers
import com.arcusys.learn.liferay.services.JournalArticleLocalServiceHelper
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.request.Parameter

import scala.collection.JavaConverters._

class JournalArticleServlet extends BaseApiController with JournalArticleHelpers {

  get("/") {
    JsonHelper.toJson(getJournalArticles.map(getMap))
  }

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
