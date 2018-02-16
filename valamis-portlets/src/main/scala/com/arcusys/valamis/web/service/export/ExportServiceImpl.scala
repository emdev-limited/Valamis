package com.arcusys.valamis.web.service.export

import java.io.{File, PrintWriter}
import java.net.URI
import java.util.UUID

import com.arcusys.json.JsonHelper
import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.export.model.ExportModel
import com.arcusys.valamis.export.service.ExportService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lrs.serializer.{AgentSerializer, StatementSerializer}
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.lrs.tincan.{Actor, AuthorizationScope, StatementResult}
import com.arcusys.valamis.util.FileSystemUtil
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.log.LogFactoryUtil
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class ExportServiceImpl(implicit val bindingModule: BindingModule) extends ExportService with Injectable {
  private lazy val lrsReader = inject[LrsClientManager]
  private lazy val fileService = inject[FileService]
  private lazy val lrsRegistration = inject[LrsRegistration]
  private val log = LogFactoryUtil.getLog(this.getClass)


  override def export(count: Option[Long],
                      actor: Option[String],
                      activity: Option[String],
                      verb: Option[String],
                      format: Option[String],
                      since: Option[String],
                      until: Option[String],
                      registration: Option[String],
                      relatedAgents: Option[String],
                      relatedActivities: Option[String]): String = {
    val guid = UUID.randomUUID.toString
    implicit val companyId = CompanyHelper.getCompanyId
    val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
      host = PortalUtilHelper.getLocalHostUrl).auth

    val f = Future {
      CompanyHelper.setCompanyId(companyId)
      exportImpl(
        agent = actor.map(ag => JsonHelper.fromJson[Actor](ag, new AgentSerializer)),
        verb = verb.map(new URI(_)),
        activity = activity.map(new URI(_)),
        registration = registration.map(UUID.fromString),
        since = since.map(DateTime.parse),
        until = until.map(DateTime.parse),
        relatedActivities = relatedActivities.exists(_.toBoolean),
        relatedAgents = relatedAgents.exists(_.toBoolean),
        format = format,
        count = count,
        guid = guid,
        lrsAuth = lrsAuth)
    }

    TaskManager.addTask(guid, f)
  }

  private def exportImpl(agent: Option[Actor],
                         verb: Option[URI],
                         activity: Option[URI],
                         registration: Option[UUID],
                         since: Option[DateTime],
                         until: Option[DateTime],
                         relatedActivities: Boolean,
                         relatedAgents: Boolean,
                         format: Option[String],
                         count: Option[Long],
                         guid: String,
                         lrsAuth: String): String = {
    TaskManager.setState(guid, ExportState(isFinished = false, data = "Connecting to LRS"))


    TaskManager.setState(guid, ExportState(isFinished = false, data = "Import statements..."))

    val file = FileSystemUtil.getTempFile("statements", "json")
    val output = new PrintWriter(file.getAbsolutePath)
    try {
      output.write("[")
      var isFirst = true
      var offset = 0
      val limit = 25

      var result = StatementResult(Seq())
      do {
        val l = count.map(c => if (offset + limit > c) (c - offset).toInt else limit).getOrElse(limit)
        result = lrsReader.statementApi(sa =>
          sa.getByParams(
            agent = agent,
            verb = verb,
            activity = activity,
            registration = registration,
            since = since,
            until = until,
            relatedActivities = relatedActivities,
            relatedAgents = relatedAgents,
            limit = Some(l),
            format = format,
            offset = Some(offset)
          ) match {
            case Success(value) => value
            case Failure(value) => throw new Exception("Fail:" + value)
          }, Some(lrsAuth))(CompanyHelper.getCompanyId)
        result.statements.foreach(s => {
          if (!isFirst) {
            output.write(",")
          }
          isFirst = false
          output.write(JsonHelper.toJson(s, new StatementSerializer))
        })
        TaskManager.setState(guid, ExportState(isFinished = false, data = s"Import statements: $offset"))
        offset += limit
      } while (result.statements.nonEmpty &&
        count.map(c => c > result.statements.size + offset).getOrElse(true) &&
        TaskManager.getState(guid).map(s => !s.isCancelled).getOrElse(false))
      TaskManager.setState(guid, ExportState(isFinished = false, data = "Import statements...completed"))

    }
    catch {
      case e: Throwable => e.printStackTrace
    }
    finally {
      output.write("]")
      output.flush()
      output.close()
    }
    try {
      def checkCancel: Unit = {
        if (TaskManager.getState(guid).map(s => s.isCancelled).getOrElse(false)) throw new CancelException
      }

      checkCancel

      val csvFile = FileSystemUtil.getTempFile("statements", "csv")

      checkCancel

      JsonToCSVConverter.convert(file, csvFile)

      checkCancel

      fileService.setFileContent("json", file.getName, getContent(file), deleteFolder = false)

      checkCancel

      fileService.setFileContent("csv", csvFile.getName, getContent(csvFile), deleteFolder = false)

      file.delete
      csvFile.delete

      TaskManager.setState(guid, ExportState(isFinished = true,
        data = "Completed",
        linkCSV = csvFile.getName,
        linkJSON = file.getName))
    }
    catch {
      case e: CancelException => log.debug("cancelled")
    }

    "ok"
  }

  override def cancel(guid: String): Unit = {
    TaskManager.removeTask(guid)
  }

  override def getStatus(guid: String): ExportModel = {
    val state = TaskManager.getState(guid)
    ExportModel(guid, isFinished = state.forall(_.isFinished),
      data = state.map(_.data).getOrElse(""),
      linkCSV = state.map(_.linkCSV).getOrElse(""),
      linkJSON = state.map(_.linkJSON).getOrElse(""))
  }

  private def getContent(file: File) = {
    val contentSource = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
    val content = contentSource.map(_.toByte).toArray
    contentSource.close()
    content
  }
}