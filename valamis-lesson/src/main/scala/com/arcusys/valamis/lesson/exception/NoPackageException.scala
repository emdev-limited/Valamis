package com.arcusys.valamis.lesson.exception

import com.arcusys.valamis.exception.EntityNotFoundException

class NoPackageException(val id: Long) extends EntityNotFoundException(s"no package with id: $id")
