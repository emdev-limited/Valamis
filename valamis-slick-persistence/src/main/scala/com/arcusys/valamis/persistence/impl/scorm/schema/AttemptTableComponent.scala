package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.AttemptModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait AttemptTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile with ScormUserComponent =>

  import driver.simple._

  class AttemptTable(tag: Tag) extends LongKeyTable[AttemptModel](tag, "SCO_ATTEMPT") {

    def userId = column[Long]("USER_ID")

    def packageId = column[Long]("PACKAGE_ID")

    def organizationId= column[String]("ORGANIZATION_ID", O.DBType(varCharMax))

    def isComplete = column[Boolean]("IS_COMPLETE")

    def * = (id.?,
      userId,
      packageId,
      organizationId,
      isComplete) <> (AttemptModel.tupled, AttemptModel.unapply)


    def update = (userId,
      packageId,
      organizationId,
      isComplete) <>(tupleToEntity, entityToTuple)



    def idxPackageId = index("ATTEMPT_PACKADEID", packageId)

    def idxUserId = index("ATTEMPT_USERID", userId)

    def idxUserIdPackageIdComplete = index("ATT_USERID_PACKID_ISCOMP", (userId, packageId, isComplete))

    def userFK = foreignKey(fkName("ATT_TOUSER"), userId, scormUsersTQ)(_.userId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }


  }

  val attemptTQ = TableQuery[AttemptTable]
}

