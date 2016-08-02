package com.arcusys.valamis.web.service

import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.settings.storage.StatementToActivityStorage
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

trait StatementActivityCreator {
  def create(companyId: Long, statements: Seq[Statement], userId: Long): Unit
}

class StatementActivityCreatorImpl(implicit val bindingModule: BindingModule)
  extends StatementActivityCreator
    with Injectable {

  lazy val lrsToActivitySettingStorage = inject[StatementToActivityStorage]
  lazy val statementSocialActivityHelper = new SocialActivityHelper[Statement]

  def create(companyId: Long, statements: Seq[Statement], userId: Long) {
    //TODO: try to avoid read all
    lazy val rules = lrsToActivitySettingStorage.getAll

    for {
      statement <- statements
      if rules.exists(isMatch(statement))
    } {
      statementSocialActivityHelper.addWithSet(
        companyId,
        userId,
        extraData = Some(JsonHelper.toJson(statement, new StatementSerializer)),
        createDate = statement.timestamp
      )
    }
  }

  private def isMatch(statement: Statement)(rule: StatementToActivity): Boolean = {
    val isActivityMatched =
      rule.mappedActivity.isEmpty || (statement.obj match {
        case act: Activity => rule.mappedActivity.get == act.id
        case _ => false
      })

    val isVerbMatched =
      rule.mappedVerb.isEmpty ||
        (rule.mappedVerb.get == statement.verb.id)

    isActivityMatched && isVerbMatched
  }
}
