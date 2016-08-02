package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateNodeModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait ActivityStateNodeTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>


  import driver.simple._
  class ActivityStateNodeTable(tag: Tag) extends LongKeyTable[ActivityStateNodeModel](tag, "SCO_ACTIVITY_STATE_NODE") {

    def parentId = column[Option[Long]]("PARENT_ID")

    def treeId = column[Option[Long]]("TREE_ID")

    def availableChildrenIds = column[Option[String]]("AVAILABLE_CHILDRENS_IDS", O.DBType(varCharMax))

    def * = (id.?, parentId, treeId, availableChildrenIds) <> (ActivityStateNodeModel.tupled, ActivityStateNodeModel.unapply)


    def update = (parentId, treeId, availableChildrenIds) <> (tupleToEntity, entityToTuple)


    def idxTreeId = index("ACTIV_STATE_NODE_TREEID", treeId)
    def idxTreeIdParentId = index("ACTIV_STATE_NODE_TRID_PRID", (treeId, parentId))



    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val activityStateNodeTQ = TableQuery[ActivityStateNodeTable]
}
