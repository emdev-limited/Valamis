package com.arcusys.learn.models.response

import com.arcusys.valamis.model.RangeResult

object CollectionResponseHelper {

  implicit class RangeResultExt[T](val r: RangeResult[T]) extends AnyVal {
    def toCollectionResponse(pageNumber: Int): CollectionResponse[T] = {
      CollectionResponse(pageNumber, r.items, r.total)
    }
  }

}

case class CollectionResponse[T](page: Int, records: Iterable[T], total: Long)
