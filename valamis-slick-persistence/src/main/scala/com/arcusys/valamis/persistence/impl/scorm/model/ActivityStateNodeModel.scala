package com.arcusys.valamis.persistence.impl.scorm.model

case class ActivityStateNodeModel(id: Option[Long],
                     parentId: Option[Long],
                     treeId: Option[Long],
                     availableChildrenIds: Option[String])
