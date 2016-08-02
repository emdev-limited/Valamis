package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityModel
import slick.driver.MySQLDriver

trait ActivityTableComponent extends  TypeMapper { self: SlickProfile =>

  import driver.simple._

  class ActivityTable(tag: Tag) extends Table[ActivityModel](tag, tblName("SCO_ACTIVITY")) {

    def indexNumber = column[Long]("INDEX_NUMBER", O.PrimaryKey, O.AutoInc)

    def id = column[Option[String]]("ID", O.Length(512, true))

    def packageId = column[Option[Long]]("PACKAGE_ID")

    def organizationId = column[String]("ORGANIZATION_ID", O.Length(512, true))

    def parentId = column[Option[String]]("PARENT_ID", O.Length(512, true))

    def title = column[String]("TITLE", O.DBType(varCharMax))

    def identifierRef = column[Option[String]]("IDENTIFIER_REF", O.DBType(varCharMax))

    def resourceParameters = column[Option[String]]("RESOURCE_PARAMETERS", O.DBType(varCharMax))

    def hidelmsui = column[Option[String]]("HIDE_LMSUI", O.DBType(varCharMax))

    def visible = column[Boolean]("VISIBLE")

    def objectivesGlobalToSystem = column[Option[Boolean]]("OBJECTIVES_GLOBAL_TO_SYSTEM")

    def sharedDataGlobalToSystem = column[Option[Boolean]]("SHARED_DATA_GLOBAL_TO_SYSTEM")

    def masteryScore = column[Option[String]]("MASTERY_SCORE", O.DBType(varCharMax))

    def maxTimeAllowed = column[Option[String]]("MAX_TIME_ALLOWED", O.DBType(varCharMax))

    def * = (indexNumber,
      id,
      packageId,
      organizationId,
      parentId,
      title,
      identifierRef,
      resourceParameters,
      hidelmsui,
      visible,
      objectivesGlobalToSystem,
      sharedDataGlobalToSystem,
      masteryScore,
      maxTimeAllowed) <>(ActivityModel.tupled, ActivityModel.unapply)

    def idxPackageId = index("ACT_PACKAGEID", packageId)
    
    if (!slickDriver.isInstanceOf[MySQLDriver]) {
      def idxPackageAndId = index("ACT_PACKAGE_AND_ID", (packageId, id))

      def idxPackageIdOrganizationId = index("ACT_PACKAGEID_ORGID", (packageId, organizationId))

      def idxPackageIdParentId = index("ACT_PACKAGEID_PARENTID", (packageId, parentId))
    }
  }

  val activityTQ = TableQuery[ActivityTable]
}

