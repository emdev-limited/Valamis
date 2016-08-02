/**
 * Created by aklimov on 13.08.14.
 */

lessonStudio.module("Entities", function(Entities, lessonStudio, Backbone, Marionette, $, _){
    Entities.LessonModel = lessonStudioModels.LessonModel;
    Entities.LessonPageModel = lessonStudioModels.LessonPageModel;
    Entities.LessonPageElementModel = lessonStudioModels.LessonPageElementModel;
    Entities.LessonPageTemplateModel = lessonStudioModels.LessonPageTemplateModel;
    Entities.LessonPageThemeModel = lessonStudioModels.LessonPageThemeModel;

    Entities.Filter = lessonStudioCollections.Filter;
    Entities.LessonCollection = lessonStudioCollections.LessonCollection;
    Entities.LessonPageCollection = lessonStudioCollections.LessonPageCollection;
    Entities.LessonPageElementCollection = lessonStudioCollections.LessonPageElementCollection;
    Entities.LessonPageThemeCollection = lessonStudioCollections.LessonPageThemeCollection;
});