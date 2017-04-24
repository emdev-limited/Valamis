package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.{SlideElementPropertyEntity, SlideElement}
import com.arcusys.valamis.slide.storage.SlideElementRepository
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class SlideElementRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
    extends SlideElementRepository
      with SlickProfile
      with DatabaseLayer
      with SlideTableComponent {

  import driver.api._

  override def create(element: SlideElement): SlideElement = execSync {
    val createElement = (slideElements returning slideElements.map(_.id)).into { (row, newId) =>
      row.copy(id = newId)
    } += element

    for {
      elem <- createElement
      _ <- if (element.properties.nonEmpty) {
        slideElementProperties ++= createProperties(element, elem.id)
      }
      else {
        DBIO.successful()
      }
    } yield elem
  }

  override def getById(id: Long): Option[SlideElement] = execSync {
    slideElements.filterById(id).result.headOption
  }

  override def getBySlideId(slideId: Long): Seq[SlideElement] = execSync {
    slideElements.filter(_.slideId === slideId).result
  }

  override def update(element: SlideElement): SlideElement = execSync {
    val updateElement = slideElements.filterById(element.id)
      .map(e => (
        e.zIndex,
        e.content,
        e.slideEntityType,
        e.slideId,
        e.correctLinkedSlideId,
        e.incorrectLinkedSlideId,
        e.notifyCorrectAnswer))
      .update(
        element.zIndex,
        element.content,
        element.slideEntityType,
        element.slideId,
        element.correctLinkedSlideId,
        element.incorrectLinkedSlideId,
        element.notifyCorrectAnswer
      )
    lazy val deleteProperties = slideElementProperties.filter(_.slideElementId === element.id).delete
    lazy val addProperties = slideElementProperties ++= createProperties(element, element.id)

    for {
      elem <- updateElement
      _ <- if (element.properties.nonEmpty) {
        deleteProperties >> addProperties
      }
      else {
        DBIO.successful()
      }
    } yield element
  }

  override def updateContent(id: Long, content: String): Unit = execSync {
    slideElements
      .filterById(id)
      .map(_.content)
      .update(content)
  }

  override def delete(id: Long): Unit = execSync {
    slideElements.filterById(id).delete
  }

  private def createProperties(slideElement: SlideElement, newSlideSetId: Long): Seq[SlideElementPropertyEntity] = {
    slideElement.properties.flatMap { property =>
      property.properties.map(pr =>
        SlideElementPropertyEntity(
          newSlideSetId,
          property.deviceId,
          pr.key,
          pr.value)
      )
    }
  }

  implicit class SlideElementQueryExt(query: Query[SlideElementTable, SlideElementTable#TableElementType, Seq]) {

    private type SlideElementQuery = Query[SlideElementTable, SlideElement, Seq]

    def filterById(id: Long): SlideElementQuery = query.filter(_.id === id)
  }

}

