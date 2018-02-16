package com.arcusys.valamis.web.service.export

import com.liferay.portal.kernel.log.LogFactoryUtil
import scala.collection.mutable.HashMap


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object TaskManager {

  private val futures = HashMap[String, Future[_]]()
  private val states = HashMap[String, ExportState]()
  private val log = LogFactoryUtil.getLog(this.getClass)



  def addTask(guid: String, task: Future[_]): String = {

    synchronized {
      states.put(guid, ExportState(isFinished = false, data = ""))
      futures.put(guid, task)
    }

    task.onSuccess {
      case s => log.debug(s)
    }

    guid.toString
  }

  def setState(guid: String, state: ExportState): Unit = {
    synchronized {
      if(getState(guid).forall(!_.isCancelled)) {
        states.update(guid, state)
      }
    }
  }

  def getState(guid: String): Option[ExportState] = {
    states.get(guid)
  }

  def removeTask(guid: String): Option[Future[_]] = {
    synchronized {
      getState(guid).foreach(st => setState(guid, st.copy(data = "Completed", isCancelled = true)))
      futures.remove(guid)
    }
  }
}
