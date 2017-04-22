package com.arcusys.valamis.file.service

import com.arcusys.learn.liferay.LiferayClasses.{LDLFileEntry, LUser}
import com.arcusys.valamis.model.RangeResult

/**
  * User: Yulia.Glushonkova
  * Date: 27.10.2014
  */
trait FileEntryService {
  def getImages(user: LUser, groupID: Int, filter: String, skip: Int, count: Int, isSortDirectionAsc: Boolean): RangeResult[LDLFileEntry]

  def getVideo(user: LUser, groupID: Int, skip: Int, count: Int): RangeResult[LDLFileEntry]

  def getAudio(user: LUser, groupID: Int, skip: Int, count: Int): RangeResult[LDLFileEntry]
}
