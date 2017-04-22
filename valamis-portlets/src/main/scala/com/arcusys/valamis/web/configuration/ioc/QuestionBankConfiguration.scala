package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.content.export.{QuestionExportProcessor, QuestionImportProcessor, QuestionMoodleImportProcessor}
import com.arcusys.valamis.content.service._
import com.arcusys.valamis.content.storage.impl.{AnswerStorageImpl, CategoryStorageImpl, PlainTextStorageImpl, QuestionStorageImpl}
import com.arcusys.valamis.content.storage.{AnswerStorage, CategoryStorage, PlainTextStorage, QuestionStorage}
import com.arcusys.valamis.persistence.common.{DatabaseLayer, Slick3DatabaseLayer, SlickDBInfo}
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class QuestionBankConfiguration(db: => SlickDBInfo)
                               (implicit configuration: BindingModule)
  extends NewBindingModule(module => {
    import configuration.inject
    import module.bind

    bind[DatabaseLayer].toSingle {
      new Slick3DatabaseLayer(db.databaseDef, db.slickProfile)
    }

    //plain text
    bind[PlainTextStorage].toSingle {
      new PlainTextStorageImpl(db.databaseDef, db.slickProfile)
    }

    //categories
    bind[CategoryStorage].toSingle {
      new CategoryStorageImpl(db.databaseDef, db.slickProfile)
    }

    //questions
    bind[QuestionStorage].toSingle {
      new QuestionStorageImpl(db.databaseDef, db.slickProfile)
    }

    //answers
    bind[AnswerStorage].toSingle {
      new AnswerStorageImpl(db.databaseDef, db.slickProfile)
    }

    bind[CategoryService] toSingle new CategoryServiceImpl {
      lazy val categories = inject[CategoryStorage](None)
      lazy val questionStorage = inject[QuestionStorage](None)
      lazy val plainTextStorage = inject[PlainTextStorage](None)
      lazy val plainTextService = inject[PlainTextService](None)
      lazy val questionService = inject[QuestionService](None)
      lazy val dbLayer = inject[DatabaseLayer](None)
    }
    bind[ContentService] toSingle new ContentServiceImpl {
      lazy val categoryStorage = inject[CategoryStorage](None)
      lazy val plainTextStorage = inject[PlainTextStorage](None)
      lazy val questionStorage = inject[QuestionStorage](None)
      lazy val answerStorage = inject[AnswerStorage](None)
      lazy val dbLayer = inject[DatabaseLayer](None)
    }
    bind[PlainTextService] toSingle new PlainTextServiceImpl {
      lazy val plainTexts = inject[PlainTextStorage](None)
      lazy val cats = inject[CategoryStorage](None)
      lazy val dbLayer = inject[DatabaseLayer](None)
    }
    bind[QuestionService] toSingle new QuestionServiceImpl {
      lazy val categoryStorage = inject[CategoryStorage](None)
      lazy val questionStorage = inject[QuestionStorage](None)
      lazy val answerStorage = inject[AnswerStorage](None)
      lazy val dbLayer = inject[DatabaseLayer](None)
    }

    bind[QuestionExportProcessor] toSingle new QuestionExportProcessor {
      lazy val questionService = inject[QuestionService](None)
      lazy val plainTextService = inject[PlainTextService](None)
      lazy val catService = inject[CategoryService](None)
    }

    bind[QuestionImportProcessor] toSingle new QuestionImportProcessor {
      lazy val questionService = inject[QuestionService](None)
      lazy val plainTextService = inject[PlainTextService](None)
      lazy val catService = inject[CategoryService](None)
    }

    bind[QuestionMoodleImportProcessor] toSingle new QuestionMoodleImportProcessor {
      lazy val questionService = inject[QuestionService](None)
      lazy val plaintextService = inject[PlainTextService](None)
      lazy val categoryService = inject[CategoryService](None)
    }
  })
