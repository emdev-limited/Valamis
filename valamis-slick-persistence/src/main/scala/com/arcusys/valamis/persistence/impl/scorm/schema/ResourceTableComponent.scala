package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ResourceModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait ResourceTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  import driver.simple._

  class ResourceTable(tag: Tag) extends LongKeyTable[ResourceModel](tag, "SCO_RESOURCE") {

    def packageId = column[Option[Long]]("PACKAGE_ID")

    def scormType = column[String]("SCORM_TYPE", O.DBType(varCharMax))

    def resourceId = column[Option[String]]("RESOURCE_ID", O.Length(3000, true))

    def href = column[Option[String]]("HREF", O.DBType(varCharMax))

    def base = column[Option[String]]("BASE", O.DBType(varCharMax))

    def * = (id.?,
      packageId,
      scormType,
      resourceId,
      href,
      base) <> (ResourceModel.tupled, ResourceModel.unapply)


    def update = (packageId,
      scormType,
      resourceId,
      href,
      base) <>(tupleToEntity, entityToTuple)


    def idPackageId = index("RESOURCE_PACKAGEID", packageId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val resourceTQ= TableQuery[ResourceTable]
}
