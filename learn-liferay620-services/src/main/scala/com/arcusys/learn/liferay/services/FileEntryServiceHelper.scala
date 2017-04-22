package com.arcusys.learn.liferay.services

import java.io.InputStream
import com.liferay.portal.kernel.util.MimeTypesUtil
import com.liferay.portal.kernel.workflow.WorkflowConstants
import com.liferay.portal.service.ServiceContext
import com.liferay.portlet.documentlibrary.model.{DLFileEntry, DLFolderConstants}
import com.liferay.portlet.documentlibrary.service.{DLAppServiceUtil, DLFileEntryServiceUtil}
import com.liferay.portlet.documentlibrary.util.{AudioProcessorUtil, ImageProcessorUtil, VideoProcessorUtil}

import scala.util.Try

/**
 * User: Yulia.Glushonkova
 * Date: 11.07.14
 */
object FileEntryServiceHelper {
  def getImages(groupID: Int, skip: Int, take: Int, filter: String, sortAscDirection: Boolean): List[DLFileEntry] = {
    val images = DLFileEntryServiceUtil.getGroupFileEntries(groupID, 0, 0, imageMimeTypes, 0, -1, -1, null)
      .toArray()
      .filter(i => i.asInstanceOf[DLFileEntry].getTitle.toLowerCase.contains(filter.toLowerCase))
      .sortBy(i => i.asInstanceOf[DLFileEntry].getTitle)

    (if (sortAscDirection) images else images.reverse).slice(skip, skip + take)
      .map(i => {
        i.asInstanceOf[DLFileEntry]
      }).toList
  }

  def getImagesCount(groupID: Int, filter: String): Int = {
    DLFileEntryServiceUtil.getGroupFileEntries(groupID, 0, 0, imageMimeTypes, 0, -1, -1, null)
      .toArray().count(i => i.asInstanceOf[DLFileEntry].getTitle.toLowerCase.contains(filter.toLowerCase))
  }

  def getVideo(groupID: Int, skip: Int, take: Int): List[DLFileEntry] = {
    val video = DLFileEntryServiceUtil.getGroupFileEntries(groupID, 0, 0, videoMimeTypes, 0, -1, -1, null)
      .toArray()
      .sortBy(i => i.asInstanceOf[DLFileEntry].getTitle)

    video.slice(skip, skip + take)
      .map(i => {
        i.asInstanceOf[DLFileEntry]
      }).toList
  }

  def getVideoCount(groupID: Int): Int = {
    DLFileEntryServiceUtil.getGroupFileEntriesCount(groupID, 0, 0, videoMimeTypes, 0)
  }

  def getAudio(groupID: Int, skip: Int, take: Int): List[DLFileEntry] = {
    val audio = DLFileEntryServiceUtil.getGroupFileEntries(groupID, 0, 0, audioMimeTypes, 0, -1, -1, null)
      .toArray()
      .sortBy(i => i.asInstanceOf[DLFileEntry].getTitle)

    audio.slice(skip, skip + take)
      .map(i => {
        i.asInstanceOf[DLFileEntry]
      }).toList
  }

  def getAudioCount(groupID: Int): Int = {
    DLFileEntryServiceUtil.getGroupFileEntriesCount(groupID, 0, 0, audioMimeTypes, 0)
  }

  private def imageMimeTypes() = {
    ImageProcessorUtil.getImageMimeTypes().toArray.map(i => i.asInstanceOf[String])
  }
  private def videoMimeTypes() = {
    VideoProcessorUtil.getVideoMimeTypes().toArray.map(i => i.asInstanceOf[String])
  }

  private def audioMimeTypes() = {
    AudioProcessorUtil.getAudioMimeTypes().toArray.map(i => i.asInstanceOf[String])
  }

  def getFile(fileEntryID: Long, version: String): Array[Byte] = {
    val stream = DLFileEntryServiceUtil.getFileAsStream(fileEntryID, version)

    toByteArray(stream)
  }

  def getFile(uuid: String, groupId: Long): Array[Byte] = {
    val stream = getFileEntry(uuid, groupId).getContentStream

    toByteArray(stream)
  }

  private def toByteArray(inputStream: InputStream): Array[Byte] = {
    Stream.continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray
  }

  def getFileEntry(uuid: String, groupId: Long): DLFileEntry = {
    val fileEntry = DLFileEntryServiceUtil.getFileEntryByUuidAndGroupId(uuid, groupId)
    fileEntry
  }

  def addFileToDocumentLibrary(filename: String, groupId: Long, videoTitle: String, extension: String, mimeType: String, size: Long): String = {

    val repositoryId = DLFolderConstants.getDataRepositoryId(groupId, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID)
    val folderId = DLFolderConstants.getFolderId(groupId, repositoryId)

    val existedFileEntry = Try(DLAppServiceUtil.getFileEntry(repositoryId, folderId, videoTitle)).toOption
    if (existedFileEntry.isDefined && existedFileEntry.get.getSize == size) return existedFileEntry.get.getUuid

    val sourceFileName = filename + "." + extension
    val file = new java.io.File(filename)
    val mimeTypeForEntry = if (mimeType.isEmpty) MimeTypesUtil.getContentType(file)
    else mimeType

    val title = if (existedFileEntry.isDefined && existedFileEntry.get.getTitle == videoTitle) videoTitle + " new"
    else videoTitle

    val serviceContext = new ServiceContext()
    serviceContext.setScopeGroupId(groupId)
    serviceContext.setAddGroupPermissions(true)
    serviceContext.setAddGuestPermissions(true)
    serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH)

    val fileEntry = DLAppServiceUtil.addFileEntry(repositoryId,
      folderId,
      sourceFileName,
      mimeTypeForEntry,
      title,
      "",
      "",
      file,
      serviceContext
    )
    fileEntry.getUuid
  }
}
