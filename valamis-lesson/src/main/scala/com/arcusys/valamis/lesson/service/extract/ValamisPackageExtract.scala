package com.arcusys.valamis.lesson.service.extract

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.lesson.model.{BaseManifest, LessonType, PackageState, ValamisPackage}
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.scorm.storage.tracking.{ActivityStateTreeStorage, AttemptStorage}
import com.arcusys.valamis.lesson.service.{LessonLimitChecker, LessonStatementReader, TagServiceContract}
import com.arcusys.valamis.lesson.tincan.model.TincanManifest
import com.arcusys.valamis.ratings.RatingService

/**
 * Created by mminin on 06.03.15.
 */
trait ValamisPackageExtract {

  protected def tagService: TagServiceContract
  protected def passingLimitChecker: LessonLimitChecker
  protected def attemptStorage: AttemptStorage
  protected def activityStateTreeStorage: ActivityStateTreeStorage
  protected def statementReader: LessonStatementReader
  private lazy val ratingService = new RatingService[BaseManifest]

  def getRootActivityId(packageId: Long): String

  protected def toValamisPackage(manifest: TincanManifest, user: LUser): ValamisPackage = {
    val activityId = getRootActivityId(manifest.id)
    val isFinished = statementReader.hasCompletedSuccess(user, activityId)

    lazy val lastAttempt = statementReader.getLastAttempted(user, activityId)
    lazy val completedStatements = statementReader.getCompleted(user, activityId)
    lazy val isLastCompleted = lastAttempt.flatMap(_.id).exists(id => completedStatements
      .flatMap(_.context)
      .flatMap(_.contextActivities)
      .flatMap(_.groupingIds)
      .contains(id.toString)
    )

    val packageState = if (isFinished) PackageState.Finished
    else if (lastAttempt.isEmpty) PackageState.None
    else if (isLastCompleted) PackageState.Attempted
    else PackageState.Suspended

    val attemptsCount = if (manifest.passingLimit <= 0) 0
    else completedStatements.size

    val rating = ratingService.getRating(user.getUserId, manifest.id)

    ValamisPackage(manifest.id,
      manifest.title,
      manifest.summary.map(_.replaceAll("\n", "")),
      Some(""),
      manifest.visibility.getOrElse(false),
      manifest.isDefault,
      LessonType.Tincan,
      manifest.logo,
      None,
      manifest.passingLimit,
      manifest.rerunInterval,
      manifest.rerunIntervalType,
      attemptsCount,
      packageState,
      tagService.getEntryTags(manifest),
      manifest.beginDate,
      manifest.endDate,
      rating
    )
  }

  protected def toValamisPackage(manifest: Manifest, user: LUser): ValamisPackage = {
    val userId = user.getUserId.toInt
    val activityId = getRootActivityId(manifest.id)
    val isFinished = statementReader.hasCompletedSuccess(user, activityId)

    val suspendedId = attemptStorage.getActive(userId, manifest.id.toInt)
      .flatMap(a => activityStateTreeStorage.get(a.id).map(_.item.activity.id))

    val attemptsCount = attemptStorage.getAllComplete(userId, manifest.id).size

    val packageState = if (isFinished) PackageState.Finished
    else if (attemptsCount == 0) PackageState.None
    else if (suspendedId.isDefined) PackageState.Suspended
    else PackageState.Attempted

    val rating = ratingService.getRating(user.getUserId, manifest.id)

    ValamisPackage(manifest.id,
      manifest.title,
      manifest.summary.map(_.replaceAll("\n", "")),
      manifest.version,
      manifest.visibility.getOrElse(false),
      manifest.isDefault,
      LessonType.Scorm,
      manifest.logo,
      suspendedId,
      manifest.passingLimit,
      manifest.rerunInterval,
      manifest.rerunIntervalType,
      attemptsCount,
      packageState,
      tagService.getEntryTags(manifest),
      manifest.beginDate,
      manifest.endDate,
      rating
    )
  }
}
