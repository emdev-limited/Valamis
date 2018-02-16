package com.arcusys.valamis.persistence.impl.contentProviders

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.{DatabaseLayer, DbNameUtils, SlickProfile}
import com.arcusys.valamis.persistence.impl.contentProviders.schema.ContentProviderTableComponent
import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider
import com.arcusys.valamis.slide.storage.ContentProviderRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
  * Created By:
  * User: zsoltberki
  * Date: 29.9.2016
  */
class ContentProviderRepositoryImpl(val db: JdbcBackend#DatabaseDef,
                                    val driver: JdbcProfile)
  extends ContentProviderRepository
    with SlickProfile
    with DatabaseLayer
    with ContentProviderTableComponent {

  import driver.api._

  override def getAll(skipTake: Option[SkipTake],
                      namePattern: Option[String],
                      sortAscDirection: Boolean,
                      companyId: Long): Seq[ContentProvider] = execSync {

    contentProviders
      .filter(_.companyId === companyId)
      .filterByName(namePattern)
      .sortByTitle(sortAscDirection)
      .slice(skipTake)
      .result
  }

  override def update(contentProvider: ContentProvider): ContentProvider = {
    val providerQ = contentProviders.filter(_.id === contentProvider.id)

    val updateQ = providerQ
      .map(s => (
        s.name,
        s.description,
        s.image,
        s.url,
        s.width,
        s.height,
        s.isPrivate,
        s.customerKey,
        s.customerSecret,
        s.isSelective))
      .update(contentProvider.name,
        contentProvider.description,
        contentProvider.image,
        contentProvider.url,
        contentProvider.width,
        contentProvider.height,
        contentProvider.isPrivate,
        contentProvider.customerKey,
        contentProvider.customerSecret,
        contentProvider.isSelective)

    val action = updateQ andThen providerQ.result.head
    execSync(action.transactionally)
  }

  override def delete(id: Long): Unit = {
    val deleteProviderQ = contentProviders.filter(_.id === id).delete
    val action = DBIO.seq(deleteProviderQ)
    execSync(action)
  }

  override def create(contentProvider: ContentProvider): ContentProvider = execSync {
    (contentProviders returning contentProviders.map(_.id)).into { (row, newId) =>
      row.copy(id = newId)
    } += contentProvider
  }

  private type ContentProviderQuery = Query[ContentProviderTable, ContentProviderTable#TableElementType, Seq]

  implicit class ContentProviderExt(val query: ContentProviderQuery) {
    def filterByName(title: Option[String]): ContentProviderQuery = title match {
      case Some(value) => query.filter(_.name.toLowerCase like DbNameUtils.likePattern(value.toLowerCase))
      case _ => query
    }

    def slice(skipTake: Option[SkipTake]): ContentProviderQuery = {
      skipTake match {
        case Some(SkipTake(skip, take)) => query.drop(skip).take(take)
        case None => query
      }
    }

    def sortByTitle(ascending: Boolean): ContentProviderQuery = {
      if (ascending) {
        query.sortBy(x => x.name)
      }
      else {
        query.sortBy(_.name.desc)
      }
    }
  }
}


