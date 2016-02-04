package com.arcusys.learn.liferay.activity

import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.settings.storage.StatementToActivityStorage
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait StatementActivityCreator {
  def create(companyId: Long, statements: Seq[Statement], userId: Long): Unit
}

class StatementActivityCreatorImpl(implicit val bindingModule: BindingModule)
  extends StatementActivityCreator with Injectable {
  private lazy val lrsToActivitySettingStorage = inject[StatementToActivityStorage]

  def create(companyId: Long, statements: Seq[Statement], userId: Long) {
    //TODO: try to avoid read all
    lazy val rules = lrsToActivitySettingStorage.getAll

    for {
      statement <- statements
      if rules.exists(isMatch(statement))
    } {
      SocialActivityLocalServiceHelper.addWithSet(
        companyId,
        userId,
        classOf[Statement].getName,
        extraData = Some(JsonHelper.toJson(statement, new StatementSerializer))
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
