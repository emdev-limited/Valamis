package com.arcusys.valamis.certificate.exception

import com.arcusys.valamis.exception.EntityNotFoundException

class NoAssignmentException(val id: Long) extends EntityNotFoundException(s"no assignment with id: $id")