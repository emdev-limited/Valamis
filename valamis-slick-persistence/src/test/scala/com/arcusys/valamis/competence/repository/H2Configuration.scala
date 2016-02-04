package com.arcusys.valamis.competence.repository

import com.arcusys.valamis.competence.schema._
import com.arcusys.valamis.core.{SlickProfile, SlickDBInfo}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule, NewBindingModule}
import org.mockito.Mockito._

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}


trait H2Configuration {


  implicit val h2Configuration = new NewBindingModule(implicit module => {

    import module._

    val h2Driver = scala.slick.driver.H2Driver
    val db = h2Driver.profile.backend.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    val slickDBInfoMock = mock(classOf[SlickDBInfo])

    bind[JdbcProfile].toSingle(h2Driver)
    bind[SlickDBInfo].toSingle(slickDBInfoMock)
    bind[JdbcBackend#DatabaseDef].toSingle(db)

    when(slickDBInfoMock.slickProfile).thenReturn(inject[JdbcProfile](None))
    when(slickDBInfoMock.databaseDef).thenReturn(inject[JdbcBackend#DatabaseDef](None))
  })
}

class CompetenceTableCreator(implicit val bindingModule: BindingModule)
  extends CategoryTableComponent
  with CompetenceLevelTableComponent
  with CompetenceTableComponent
  with SkillTableComponent
  with CompetenceCertificateTableComponent
  with SlickProfile
  with Injectable{

  val driver = inject[SlickDBInfo].slickProfile
  val db = inject[SlickDBInfo].databaseDef

  import driver.simple._

  def reCreateTables(): Unit = {
    db.withSession { implicit session =>
      StaticQuery.updateNA("DROP ALL OBJECTS").execute
      (categories.ddl ++ skills.ddl ++ competenceLevels.ddl ++ competences.ddl ++ competenceCertificates.ddl).create
    }
  }
}