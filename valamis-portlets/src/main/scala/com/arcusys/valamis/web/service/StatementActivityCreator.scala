package com.arcusys.valamis.web.service

import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.tincan.{Activity, Statement, StatementObject, Verb}
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.settings.storage.StatementToActivityStorage
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

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
    !isRuleEmpty(rule) &&
      isActivityMatched(statement.obj, rule) &&
      isVerbMatched(statement.verb, rule)
  }

  private def isRuleEmpty(rule: StatementToActivity): Boolean = {
    rule.mappedActivity.exists(_.isEmpty) &&
      rule.mappedVerb.exists(_.isEmpty)
  }

  private def isActivityMatched(statementObj: StatementObject, rule: StatementToActivity): Boolean = {
    (rule.mappedActivity, statementObj) match {
      case m@(None, _) => true
      case m@(Some(""), _) => true
      case m@(ruleActivity, activity: Activity) => ruleActivity.contains(activity.id)
      case _ => false
    }
  }

  private def isVerbMatched(statementVerb: Verb, rule: StatementToActivity): Boolean = {
    (rule.mappedVerb, statementVerb) match {
      case m@(None, _) => true
      case m@(Some(""), _) => true
      case m@(ruleVerb, verb: Verb) => ruleVerb.contains(verb.id)
      case _ => false
    }
  }
}