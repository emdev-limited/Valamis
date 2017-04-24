package com.arcusys.valamis.persistence.impl.lrs

import com.arcusys.valamis.lrsEndpoint.model.AuthType.AuthType
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.storage.LrsEndpointStorage
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class LrsEndpointStorageImpl(val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends LrsEndpointStorage
    with SlickProfile
    with LrsEndpointTableComponent
    with DatabaseLayer {

  import driver.api._

  private val selectQuery = Compiled { companyId: Rep[Long] =>
    lrsEndpoint
      .filter(s => s.companyId === companyId)
  }

  private val selectQueryNonExternal = Compiled { companyId: Rep[Long] =>
    lrsEndpoint
      .filter(s => s.companyId === companyId)
      .filterNot(_.authType === AuthType.INTERNAL)
  }

  private val selectByAuth = Compiled { (auth: Rep[AuthType], companyId: Rep[Long]) =>
    lrsEndpoint
      .filter(s => s.authType === auth && s.companyId === companyId)
  }

  override def getAll(companyId: Long): Seq[LrsEndpoint] =
    execSync(selectQuery(companyId).result)

  override def get(auth: AuthType, companyId: Long): Option[LrsEndpoint] =
    execSync(
      selectByAuth(auth, companyId).result.headOption
    )

  override def deleteAll(companyId: Long): Int =
    execSync(
      selectQuery(companyId)
        .delete //PACL with mySQL crashes here
    )

  override def deleteExternal(companyId: Long): Int =
    execSync(
      selectQueryNonExternal(companyId)
        .delete
    )

  override def create(entity: LrsEndpoint, companyId: Long): Unit =
    execSync(
      lrsEndpoint
        .map(s => (s.endpoint, s.authType, s.key, s.secret, s.customHost, s.companyId)) +=
        (entity.endpoint, entity.auth, entity.key, entity.secret, entity.customHost, companyId)
    )
}
