describe('Examples', function() {
    
    var foo, bar = null;
    
    beforeEach(function() {
        foo = {
            setBar: function(value) {
                bar = value;
            }
        };
        spyOn(foo, 'setBar');
        
    });
    
    afterEach(function() {
        
        
        
    });
    
    beforeAll(function() {
        
        $('<div/>',{
            id: 'example-id',
            "class": 'some-class'
        }).appendTo('body');
        
    });

    afterAll(function() {
        
        $('#example-id').remove();
        
    });
    
    it('The "toBe" matcher compares with ===', function() {
        var a = 1;
        expect(a===1).toBe(true);
    });
    
    it('And can have a negative case', function() {
        var a = 1, b = 3;
        expect(a===2).not.toBe(true);
        expect(b).toBeGreaterThan(a);
    });
    
    it('Should work for objects', function() {
        var foo = {
            a: 12,
            b: 34
        };
        var bar = {
            a: 12,
            b: 34
        };
        expect(foo).toEqual(bar);
    });
    
    it('The "toBeDefined" matcher compares against `undefined`', function() {
        var a = {
            foo: 'foo'
        };
        
        expect(a.foo).toBeDefined();
        expect(a.bar).not.toBeDefined();
    });
    
    it('Tracks if it was called at all', function() {
        
        expect(foo.setBar.calls.any()).toEqual(false);
        
        foo.setBar();
        
        expect(foo.setBar.calls.any()).toEqual(true);
        
    });
    
    it('Tracks the number of times it was called', function() {
        
        expect(foo.setBar.calls.count()).toEqual(0);
        
        foo.setBar();
        foo.setBar();
        
        expect(foo.setBar.calls.count()).toEqual(2);
        
    });
    
    it('Tracks the arguments of each call', function() {
        
        foo.setBar(123);
        foo.setBar(456, 'baz');
        
        expect(foo.setBar.calls.argsFor(0)).toEqual([123]);
        expect(foo.setBar.calls.argsFor(1)).toEqual([456, 'baz']);
        
    });

    describe("A spy", function() {

        var foo, bar = null;

        beforeEach(function() {
            foo = {
                setBar: function(value) {
                    bar = value;
                }
            };

            spyOn(foo, 'setBar');

            foo.setBar(123);
            foo.setBar(456, 'another param');
        });

        it("tracks that the spy was called", function() {
            expect(foo.setBar).toHaveBeenCalled();
        });

        it("tracks all the arguments of its calls", function() {
            expect(foo.setBar).toHaveBeenCalledWith(123);
            expect(foo.setBar).toHaveBeenCalledWith(456, 'another param');
        });

        it("stops all execution on a function", function() {
            expect(bar).toBeNull();
        });
    });
    
    it('Test with jasmine-jquery', function(){
        
        expect($('#example-id')[0]).toBeInDOM();
        expect($('#example-id')[0]).toHaveClass('some-class');
        
    });
    
});
