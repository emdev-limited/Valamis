package com.arcusys.valamis.persistence.impl.course

import com.arcusys.valamis.course.model.CourseCertificate
import com.arcusys.valamis.course.storage.CourseCertificateRepository
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.course.schema.CourseCertificateTableComponent
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.language.postfixOps

class CourseCertificateRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CourseCertificateRepository
    with CourseCertificateTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  override def getByCourseId(id: Long): Seq[CourseCertificate] = execSync {
    courseCertificates.filter(_.courseId === id).result
  }

  override def update(courseId: Long, certificateIds: Seq[Long], modifiedDate: DateTime): Unit = {
    val deleteOldRows = courseCertificates.filter {
      _.courseId === courseId
    } filterNot {
      _.certificateId inSet certificateIds
    } delete

    val insertNewRows = courseCertificates ++= certificateIds.filter {
      !isExist(courseId, _)
    } map {
      CourseCertificate(courseId, _, modifiedDate)
    }
    execSyncInTransaction(deleteOldRows >> insertNewRows)
  }

  override def isExist(courseId: Long, certificateId: Long): Boolean = execSync {
    courseCertificates.filter { ct =>
      ct.courseId === courseId && ct.certificateId === certificateId
    }.exists.result
  }

  override def deleteCertificates(courseId: Long): Unit = execSync {
    courseCertificates filter (_.courseId === courseId) delete
  }

  override def deleteByCertificateId(certificateId: Long): Unit = execSync {
    courseCertificates filter (_.certificateId === certificateId) delete
  }
}
