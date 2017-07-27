package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses._
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.model.MembershipRequestConstants
import com.liferay.portal.kernel.service.MembershipRequestLocalServiceUtil

import scala.collection.JavaConverters._

/**
  * Created By:
  * User: zsoltberki
  * Date: 10.5.2016
  */
object MembershipRequestServiceHelper {

  private val _log: Log = LogFactoryUtil.getLog(this.getClass)

  val STATUS_APPROVED = MembershipRequestConstants.STATUS_APPROVED
  val STATUS_DENIED = MembershipRequestConstants.STATUS_DENIED
  val STATUS_PENDING = MembershipRequestConstants.STATUS_PENDING

  def getRequests(groupId: Long, status: Int): Seq[LMembershipRequest] = {
    try {
      val total = getRequestCount(groupId, status)
      MembershipRequestLocalServiceUtil.search(groupId, status, 0, total).asScala
    } catch {
      case e: Exception => _log.error(e); throw e
    }
  }

  def getRequestCount(groupId: Long, status: Int): Int = {
    try {
      MembershipRequestLocalServiceUtil.searchCount(groupId, status)
    } catch {
      case e: Exception => _log.error(e); throw e
    }
  }

  def getUsersRequests(groupId: Long, userId: Long, status: Int) : Seq[LMembershipRequest] = {
    try {
      MembershipRequestLocalServiceUtil.getMembershipRequests(userId,groupId,status).asScala
    } catch {
      case e: Exception => _log.error(e); throw e
    }
  }

  def updateStatus(requestId: Long, updatingUserId: Long, comment: String, status: Int): Unit = {
    try {
      val serviceContext = new LServiceContext
      MembershipRequestLocalServiceUtil.updateStatus(updatingUserId,requestId,comment,status,false,serviceContext)
    } catch {
      case e: Exception => _log.error(e); throw e
    }
  }

  def addRequest(groupId: Long, userId: Long, comment: String): Unit = {
    try {
      val serviceContext = new LServiceContext
      if(getUsersRequests(groupId,userId,MembershipRequestConstants.STATUS_PENDING).isEmpty) {
        MembershipRequestLocalServiceUtil.addMembershipRequest(userId,groupId,comment,serviceContext)
      }
    } catch {
      case e: Exception => _log.error(e); throw e
    }
  }
}
