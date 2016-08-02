package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{CompletionRollupContribution, RollupContribution, SatisfactionRollupContribution}
import com.arcusys.valamis.lesson.scorm.storage.sequencing.RollupContributionStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.RollupContributionModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{RollupContributionTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class RollupContributionStorageImpl(val db: JdbcBackend#DatabaseDef,
                                    val driver: JdbcProfile)
  extends RollupContributionStorage
    with RollupContributionTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  override def create(sequencingId: Long, entity: RollupContribution): Unit = db.withSession { implicit s =>
    rollupContributionTQ.insert(convert(entity, sequencingId))
  }

  override def get(sequencingId: Long): Option[RollupContribution] = db.withSession { implicit s =>
    rollupContributionTQ.filter(_.sequencingId === sequencingId).firstOption.map(convert)
  }

  override def delete(sequencingId: Long): Unit = db.withSession { implicit s =>
    rollupContributionTQ.filter(_.sequencingId === sequencingId).delete
  }

  def convert(entity: RollupContributionModel): RollupContribution = {
    val contributeToSatisfied = entity.contributeToSatisfied
    val contributeToNotSatisfied = entity.contributeToNotSatisfied
    val contributeToCompleted = entity.contributeToCompleted
    val contributeToIncomplete = entity.contributeToIncompleted

    val satisfaction = if (contributeToSatisfied.isDefined && contributeToNotSatisfied.isDefined) {
      Some(new SatisfactionRollupContribution(contributeToSatisfied.get, contributeToNotSatisfied.get))
    } else None

    val completion = if (contributeToCompleted.isDefined && contributeToIncomplete.isDefined) {
      Some(new CompletionRollupContribution(contributeToCompleted.get, contributeToIncomplete.get))
    } else None

    new RollupContribution(satisfaction, completion, entity.objectiveMeasureWeight, entity.measureSatisfactionIfActive)
  }

  def convert(entity: RollupContribution, sequencingId: Long): RollupContributionModel = {
    RollupContributionModel(
      None,
      sequencingId,
      entity.satisfaction.map(_.contributeToSatisfied),
      entity.satisfaction.map(_.contributeToNotSatisfied),
      entity.completion.map(_.contributeToCompleted),
      entity.completion.map(_.contributeToIncomplete),
      entity.objectiveMeasureWeight,
      entity.measureSatisfactionIfActive
    )
  }

}
