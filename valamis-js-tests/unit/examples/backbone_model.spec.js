describe('Test Backbone models', function() {
    
    var book;
    var BookModel = Backbone.Model.extend({
        urlRoot: '/books/'
    });
    
    beforeEach(function (){
        
        jasmine.Ajax.install();
        
        book = new BookModel({
            id: 123,
            title: 'The Rough Riders',
            author: 'Theodore Roosevelt'
        });
        
    });
    
    afterEach(function() {
        jasmine.Ajax.uninstall();
    });
    
    it('Test save URL', function() {
        
        book.save();
        
        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/books/123');
        
    });
    
    it('Test save response', function() {
        
        var doneFn = jasmine.createSpy('success');
        
        jasmine.Ajax.stubRequest('/books/123').andReturn({
            'status': 200,
            'contentType': 'text/plain',
            'responseText': '{"id":123,"title":"The Rough Riders","author":"Theodore Roosevelt"}'
        });
        
        book.save(null, {
            success: function(model, response, options){
                doneFn( model, response );
            }
        });
        
        expect(doneFn.calls.first().args).toEqual( [ book, {id: 123, title: "The Rough Riders", author: "Theodore Roosevelt"} ] );
        
    });
    
});
