package com.arcusys.valamis.storyTree.service.impl

import java.net.URI

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.{CompanyHelper, UserLocalServiceHelper}
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.lesson.model.{Lesson, LessonStates}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.lrs.api.StatementApi
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import com.arcusys.valamis.lrssupport.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.utils.TincanHelper
import com.arcusys.valamis.lrs.tincan.{Activity, Agent}
import TincanHelper._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.storyTree.model._
import com.arcusys.valamis.storyTree.service.StoryTreeStatusService
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import com.arcusys.valamis.storyTree.storage.query.StoryQueries
import com.arcusys.valamis.util.Joda._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver._
import slick.jdbc._

abstract class StoryTreeStatusServiceImpl(val db: JdbcBackend#DatabaseDef,
                                          val driver: JdbcProfile)
  extends StoryTreeStatusService
    with StoryTreeTableComponent
    with SlickProfile
    with StoryQueries
    with DatabaseLayer {

  def lessonService: LessonService
  def lessonCategoryGoalService: LessonCategoryGoalService
  def lrsClient: LrsClientManager
  def lessonResultService: UserLessonResultService
  def teacherGradeService: TeacherLessonGradeService
  def lessonStatementReader: LessonStatementReader

  import driver.api._
  import DatabaseLayer._

  override def get(treeId: Long, userId: Long): StoryTreeStatus = execSyncInTransaction {
    val companyId = CompanyHelper.getCompanyId
    trees.filterById(treeId).result.headOption ifSomeThen { tree =>
      for {
        ns <- nodes.filterByTreeId(treeId).result
        packageItems <- packages.filterByTreeId(treeId).result
      } yield {
        val user = UserLocalServiceHelper().getUser(userId)
        CompanyHelper.setCompanyId(companyId)
        lrsClient.statementApi {
          getTreeStatus(tree, ns, packageItems)(_, user)
        }(CompanyHelper.getCompanyId)
      }
    } map(_.getOrElse(throw new EntityNotFoundException(s"Story with id $treeId not found")))
  }

  private def getTreeStatus(tree: Story,
                            nodes: Seq[StoryNode],
                            packages: Seq[StoryPackageItem])
                           (implicit statementReader: StatementApi,
                            user: LUser): StoryTreeStatus = {
    val nodesStatuses = nodes
      .filter(_.parentId.isEmpty)
      .map(getNodeStatus(_, nodes, packages))

    val progress = if (nodesStatuses.isEmpty) 0.0
    else nodesStatuses.map(_.progress).sum / nodesStatuses.size

    StoryTreeStatus(
      tree.id.get,
      progress,
      maxDate(nodesStatuses.flatMap(_.lastDate)),
      tree.title,
      tree.description,
      nodesStatuses
    )
  }

  private def getNodeStatus(node: StoryNode,
                            nodes: Seq[StoryNode],
                            packages: Seq[StoryPackageItem])
                           (implicit statementReader: StatementApi,
                            user: LUser): StoryNodeStatus = {

    val nodesStatus = nodes
      .filter(_.parentId == node.id)
      .map(getNodeStatus(_, nodes, packages))

    val packagesStatus = packages
      .filter(_.nodeId == node.id.get)
      .map(getPackageStatus)

    val items = nodesStatus.map(_.progress) ++ packagesStatus.map(_.progress)
    val progress = if (items.isEmpty) 0.0
    else items.sum / items.size

    StoryNodeStatus(
      node.id.get,
      progress,
      maxDate(nodesStatus.flatMap(_.lastDate)),
      node.title,
      node.description,
      node.comment,
      nodesStatus,
      packagesStatus
    )
  }

  private def getPackageStatus(packageItem: StoryPackageItem)
                              (implicit statementReader: StatementApi,
                               user: LUser): StoryPackageStatus = {
    val lesson = lessonService.getLesson(packageItem.packageId)

    val activityId = lesson.map(lessonService.getRootActivityId)

    if (lesson.isDefined && activityId.isDefined)
      getPackageStatus(packageItem, lesson.get, activityId.get)
    else
      getEmptyPackageStatus(packageItem)
  }

  private def getEmptyPackageStatus(packageItem: StoryPackageItem): StoryPackageStatus = {
    StoryPackageStatus(
      packageItem.id.get,
      packageItem.packageId,
      progress = 0,
      lastDate = None,
      title = None,
      description = None,
      relationComment = None,
      topics = Seq()
    )
  }

  private def getPackageStatus(packageItem: StoryPackageItem,
                               lesson: Lesson,
                               rootActivityId: String)
                              (implicit statementReader: StatementApi,
                               user: LUser): StoryPackageStatus = {

    val agent = user.getAgentByUuid

    val lessonResult = lessonResultService.get(lesson, user)
    val teacherGrade = teacherGradeService.get(user.getUserId, lesson.id).flatMap(_.grade)
    val state = lesson.getLessonStatus(lessonResult, teacherGrade)

    val categoryGoal = lessonCategoryGoalService.get(lesson.id)

    val experienceStatements = lessonStatementReader.getExperienced(agent, rootActivityId)

    val lastDate = lessonResult.lastAttemptDate

    val topics = categoryGoal.map { goal =>
      val categoryGoalStatements = experienceStatements
        .filter(s =>
          s.context.isDefined &&
            s.context.get.contextActivities.isDefined &&
            s.context.get.contextActivities.get.category.exists(c => c.id == goal.category)
        )

      val count = categoryGoalStatements.map(_.obj).distinct.size

      val progress = if (count == 0) 0.0
      else if (count >= goal.count) 1.0
      else count / goal.count

      new StoryTopicStatus(
        goal.id.get,
        progress,
        lastDate,
        goal.name
      )
    }

    val progress = if (topics.isEmpty) { if (state.contains(LessonStates.Finished)) 1.0 else 0.0 }
    else topics.map(_.progress).sum / topics.size

    StoryPackageStatus(
      packageItem.id.get,
      lesson.id,
      progress,
      lastDate,
      Some(lesson.title),
      Some(lesson.description),
      packageItem.comment,
      topics
    )
  }

  private def maxDate(dates: Seq[DateTime]): Option[DateTime] = {
    if (dates.isEmpty) None
    else Some(dates.max)
  }
}
