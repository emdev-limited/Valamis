package com.arcusys.valamis.persistence.common

/**
  * Created by igrebenik on 01.09.16.
  */
package object filters {

  trait ColumnFiltering {
    this: SlickProfile =>

    import driver.api._

    val InLimit = 1000

    implicit class RepExtensions(val rep: Rep[Long]) {
      /**
        * apply slick inSet filter by chunks
        * (ID inSet (1,2,3,..) or ID inSet (100,101,102,..) or ...)
        */
      def containsIn(values: Seq[Long]): Rep[Boolean] = {
        if (values.isEmpty) {
          LiteralColumn(false)
        }
        else {
          val filters = for {
            offset <- 0 until(values.size, InLimit)
          } yield {
            val valuesPart = values.slice(offset, offset + InLimit)
            rep inSet valuesPart
          }

          filters.reduceLeft((f1, f2) => f1 || f2)
        }
      }
    }
  }

}
