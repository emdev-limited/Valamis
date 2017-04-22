var PptxElementModule = slidesApp.module('PptxElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(PptxElementModule, slidesApp, Backbone, Marionette, $, _){

        PptxElementModule.CreateModel = function() {
            return new PptxElementModule.Model();
        }
    }
});