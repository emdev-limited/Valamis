var PptxElementModule = slidesApp.module('PptxElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(PptxElementModule, slidesApp, Backbone, Marionette, $, _){

        PptxElementModule.CreateModel = function() {
            return new PptxElementModule.Model();
        }
    }
});

PptxElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'PPT', label: 'PPT', slideEntityType: 'pptx'});
});