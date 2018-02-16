package com.arcusys.valamis

import com.arcusys.valamis.model.SkipTake

package object utils {

  implicit class SeqCutExtension[T](val items: Seq[T]) extends AnyVal {
    def skip(skipTake: Option[SkipTake]): Seq[T] = {
      skipTake match {
        case Some(SkipTake(skip, take)) => items.slice(skip, skip + take)
        case None => items
      }
    }
  }

}
