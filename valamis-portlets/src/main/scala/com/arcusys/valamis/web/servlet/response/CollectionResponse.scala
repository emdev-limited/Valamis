package com.arcusys.valamis.web.servlet.response

import com.arcusys.valamis.model.RangeResult

object CollectionResponseHelper {

  implicit class RangeResultExt[T](val r: RangeResult[T]) extends AnyVal {
    def toCollectionResponse(pageNumber: Int): CollectionResponse[T] = {
      CollectionResponse(pageNumber, r.records, r.total)
    }
  }

}

@deprecated
case class CollectionResponse[T](page: Int, records: Iterable[T], total: Long)
