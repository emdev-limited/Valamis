package com.arcusys.valamis.web.servlet.liferay.response

case class FileEntryModel(
  id: Long,
  title: String,
  folderId: Long,
  version: String,
  mimeType: String,
  groupID: Long,
  uuid: String)
