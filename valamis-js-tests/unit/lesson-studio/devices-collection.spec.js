describe('Devices collection', function() {

    var collection;

    var responsesContent = {
        fetch: '[{"id":1,"name":"desktop","minWidth":1024,"maxWidth":0,"minHeight":768,"margin":40},{"id":2,"name":"tablet","minWidth":768,"maxWidth":1023,"minHeight":1024,"margin":30},{"id":3,"name":"phone","minWidth":375,"maxWidth":767,"minHeight":667,"margin":20}]'
    };

    beforeAll(function() {

        slidesApp.slideCollection = new lessonStudio.Entities.LessonPageCollection();
        slidesApp.slideCollection.add({
            slideElements: []
        });
        slidesApp.slideElementCollection = new lessonStudio.Entities.LessonPageElementCollection();

    });

    beforeEach(function (){

        jasmine.Ajax.install();

        collection = new lessonStudioCollections.LessonDeviceCollection;

    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    it('Fetch URL', function() {

        collection.fetch();

        expect(jasmine.Ajax.requests.mostRecent().url).toBe('../delegate/devices/');

    });

    it('Default must have one selected device and one active device', function() {

        jasmine.Ajax.stubRequest('../delegate/devices/').andReturn({
            'status': 200,
            'contentType': 'text/plain',
            'responseText': responsesContent.fetch
        });

        collection.on('sync', function(){
            this.setSelectedDefault();
        });
        collection.fetch();

        expect(collection.where({ selected: true }).length).toEqual(1);
        expect(collection.where({ active: true }).length).toEqual(1);

    });

});