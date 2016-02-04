package com.arcusys.learn.scorm.tracking.impl.liferay

import com.arcusys.learn.persistence.liferay.model.LFUser
import com.arcusys.learn.persistence.liferay.service.{ LFAttemptLocalServiceUtil, LFUserLocalServiceUtil }
import com.arcusys.valamis.user.model.{ScormUser, User}
import com.arcusys.valamis.user.storage.UserStorage
import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Created by mminin on 16.10.14.
 */
class UserStorageImpl extends UserStorage {

  override def renew(): Unit = {
    LFUserLocalServiceUtil.removeAll()
    createAndGetID(new ScormUser(-1, "Guest", 0, "en", 0, 0))
  }

  override def getAll: Seq[ScormUser] = {
    LFUserLocalServiceUtil.getLFUsers(-1, -1).asScala.map(extract)
  }

  override def modify(user: ScormUser): Unit = {
    val lfEntity = LFUserLocalServiceUtil.findByUserId(user.id.toInt)

    lfEntity.setName(user.name)
    lfEntity.setPreferredAudioLevel(user.preferredAudioLevel.toDouble)
    lfEntity.setPreferredLanguage(user.preferredLanguage)
    lfEntity.setPreferredAudioCaptioning(user.preferredAudioCaptioning)
    lfEntity.setPreferredDeliverySpeed(user.preferredDeliverySpeed.toDouble)

    LFUserLocalServiceUtil.addLFUser(lfEntity)
  }

  override def getUsersWithAttempts: Seq[ScormUser] = {
    val userIDs = LFAttemptLocalServiceUtil.getLFAttempts(-1, -1).asScala.map(_.getUserID).toArray
    if (userIDs.length == 0) Nil else
      LFUserLocalServiceUtil.findByUserIds(userIDs).asScala.map(extract)
  }

  override def getByID(userId: Int): Option[ScormUser] = {
    Try(LFUserLocalServiceUtil.findByUserId(userId)).toOption.map(extract)
  }

  override def getByName(name: String): Seq[ScormUser] = getAll.filter(_.name.toLowerCase.contains(name))

  override def delete(userId: Int): Unit = {
    LFUserLocalServiceUtil.removeByUserId(userId)
  }

  override def createAndGetID(user: ScormUser): Int = {
    val lfEntity = LFUserLocalServiceUtil.createLFUser()

    lfEntity.setId(user.id.toInt)
    lfEntity.setName(user.name)
    lfEntity.setPreferredAudioLevel(user.preferredAudioLevel.toDouble)
    lfEntity.setPreferredLanguage(user.preferredLanguage)
    lfEntity.setPreferredAudioCaptioning(user.preferredAudioCaptioning)
    lfEntity.setPreferredDeliverySpeed(user.preferredDeliverySpeed.toDouble)

    LFUserLocalServiceUtil.updateLFUser(lfEntity)
    lfEntity.getId
  }

  def extract(lfEntity: LFUser) =
    if (lfEntity == null) null
    else ScormUser(lfEntity.getId.toLong, lfEntity.getName)
}
