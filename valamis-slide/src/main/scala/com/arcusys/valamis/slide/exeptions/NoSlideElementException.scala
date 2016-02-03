package com.arcusys.valamis.slide.exeptions

import com.arcusys.valamis.exception.EntityNotFoundException

class NoSlideElementException (val id: Long) extends EntityNotFoundException(s"slide element with id: $id doesn't exist")
