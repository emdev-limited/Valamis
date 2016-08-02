package com.arcusys.valamis.persistence.common

import DbNameUtils._
import slick.lifted.MappedProjection

trait LongKeyTableComponent { self: SlickProfile =>
  import driver.simple._

  abstract class LongKeyTable[E](tag: Tag, name: String) extends Table[E](tag, tblName(name)) {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    def update: MappedProjection[E, _]//TODO try to implicitly use this projection wherever needed (now it's used explicitly)

    //TODO try to move entityToTuple method here from child classes
    //problem is that we lack of type information for macros to work in case of entityToTuple located here
    /*def entityToTuple1(entity: E) = {
      Some(toTupleWithFilter(entity))
    }*/

    def tupleToEntity(tuple: Product): TableElementType = {
      //this code doesn't work, but it's ok)
      //because it's only needed to make code compile and is never invoked at runtime
      AnyRef.asInstanceOf[TableElementType]
    }

  }
}

