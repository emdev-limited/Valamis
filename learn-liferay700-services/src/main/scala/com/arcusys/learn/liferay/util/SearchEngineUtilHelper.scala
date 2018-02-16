package com.arcusys.learn.liferay.util

import com.liferay.portal.kernel.search.{Document, IndexWriterHelperUtil, SearchEngineUtil}

object SearchEngineUtilHelper {

  val SearchContentFileName = "search_content.txt"
  val SearchContentFileCharset = "UTF-8"

  def updateDocuments(searchEngineId: String,
    companyId: Long,
    documents: java.util.Collection[Document]) =
    SearchEngineUtil.updateDocuments(searchEngineId, companyId, documents)

  def updateDocument(searchEngineId: String,
    companyId: Long,
    document: Document) =
    SearchEngineUtil.updateDocument(searchEngineId, companyId, document)

  /**
    * Commit deletion immediately.
    * @param searchEngineId
    * @param companyId
    * @param document
    */
  def deleteDocument(searchEngineId: String,
                     companyId: Long,
                     document: Document) {
    IndexWriterHelperUtil.deleteDocument(searchEngineId, companyId, document.getUID, true);
  }
}
