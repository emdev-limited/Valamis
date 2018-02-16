package com.arcusys.valamis.file.service

import com.arcusys.learn.liferay.LiferayClasses.LDLFileEntry
import com.arcusys.learn.liferay.services.FileEntryServiceHelper
import com.arcusys.valamis.file.storage.FileStorage

abstract class FileServiceImpl extends FileService {

  def fileStorage: FileStorage

  private def getPath(folder: String, name: String): String =
    s"files/$folder/$name".replace("//", "/")

  private def getFolderPath(folder: String): String =
    s"files/$folder/".replace("//", "/")

  private def getPath(name: String): String = s"files/$name"

  //TODO: method should not remove all folder, need to check references and fix
  override def setFileContent(folder: String,
                              name: String,
                              content: Array[Byte],
                              deleteFolder: Boolean = true): String = {

    val filePath = getPath(folder, name)

    if (deleteFolder)
      fileStorage.delete(getFolderPath(folder), asDirectory = true)
    else
      fileStorage.delete(filePath, asDirectory = false)

    fileStorage.store(filePath, content)
    filePath
  }

  override def replaceFileContent(folder: String, name: String, content: Array[Byte]): Unit = {
    fileStorage.delete(getPath(folder, name))
    fileStorage.store(getPath(folder, name), content)
  }

  override def getFileContentOption(name: String): Option[Array[Byte]] = {
    fileStorage.getFile(name).flatMap(_.content)
  }

  override def getFileContent(folder: String, name: String): Array[Byte] = {
    val content = getFileContentOption(getPath(folder, name))
    if (content.isEmpty) {
      throw new NoSuchElementException("file not found: " + name)
    }
    content.get
  }

  override def getFileContent(uuid: String, groupId: Long): Array[Byte] = {
    FileEntryServiceHelper.getFile(uuid, groupId)
  }

  override def getFileContent(fileEntryId: Long, version: String): Array[Byte] = {
    FileEntryServiceHelper.getFile(fileEntryId, version)
  }

  override def copyFile(sourceFolder: String,
                        sourceName: String,
                        destFolder: String,
                        destName: String,
                        deleteFolder: Boolean = true): Unit = {
    setFileContent(destFolder, destName, getFileContent(sourceFolder, sourceName), deleteFolder)
  }

  @deprecated("""Shouldn't require "files/" in path. see deleteFile""")
  override def deleteFileStoryTree(name: String): Unit =
    fileStorage.delete(name, asDirectory = false)

  override def deleteFile(folder: String, name: String): Unit =
    fileStorage.delete(getPath(folder, name))

  override def deleteFile(name: String): Unit =
    fileStorage.delete(getPath(name))

  override def deleteByPrefix(prefix: String): Unit =
    fileStorage.delete(getFolderPath(prefix), asDirectory = true)

  override def addToDocumentLibrary(filename: String,
                                    groupId: Long,
                                    videoTitle: String,
                                    extension: String,
                                    mimeType: String,
                                    size: Long): String = {

    FileEntryServiceHelper.addFileToDocumentLibrary(
      filename,
      groupId,
      videoTitle,
      extension,
      mimeType,
      size
    )
  }

  override def getFileEntry(uuid: String, groupId: Long): LDLFileEntry = {
    FileEntryServiceHelper.getFileEntry(uuid, groupId)
  }
}
