describe('Test Ajax', function() {
    
    beforeEach(function (){
        jasmine.Ajax.install();
    });
    
    afterEach(function() {
        jasmine.Ajax.uninstall();
    });
    
    beforeAll(function() {
        
    });

    afterAll(function() {
        
    });
    
    it('Test XMLHttpRequest', function() {
        
        var doneFn = jasmine.createSpy('success');
        
        jasmine.Ajax.stubRequest('/example/url').andReturn({
            'status': 200,
            'contentType': 'text/plain',
            'responseText': 'immediate response'
        });
        
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(args) {
            if (this.readyState == this.DONE) {
                doneFn(this.responseText);
            }
        };
        
        xhr.open('GET', '/example/url');
        xhr.send();
        
        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/example/url');
        expect(doneFn).toHaveBeenCalledWith('immediate response');
        
    });
    
    it('Test jQuery.ajax', function() {
        
        var doneFn = jasmine.createSpy('success'),
            failFn = jasmine.createSpy('error');
        
        jasmine.Ajax.stubRequest('/example/url').andReturn({
            'status': 200,
            'contentType': 'text/plain',
            'responseText': '{"name":"John","location":"Boston"}'
        });
        
        $.ajax({
            method: 'POST',
            dataType: 'json',
            url: '/example/url',
            data: { name: 'John', location: 'Boston' }
        })
        .done(function( msg ) {
            doneFn( msg );
        })
        .fail(function( jqXHR, textStatus ) {
            failFn( jqXHR, textStatus );
        });
        
        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/example/url');
        expect(failFn).not.toHaveBeenCalled();
        expect(doneFn).toHaveBeenCalledWith( { name:'John', location:'Boston' } );
        
    });
    
});
