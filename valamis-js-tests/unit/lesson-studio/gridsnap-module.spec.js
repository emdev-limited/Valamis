describe('Grid snap', function() {

    var app;

    beforeAll(function() {
        app = new Marionette.Application();
    });

    beforeEach(function() {
        app.module('gridSnapModule', ValamisGridSnapModule);
        Object.defineProperty(app.gridSnapModule, 'getLayoutSize', {
            value: function(){
                return {
                    width: 1024,
                    height: 768,
                    margin: 40
                };
            }
        });
        app.gridSnapModule.configure({
            partsQuantity: 12,
            square: true
        });
        app.gridSnapModule.start();// generate grid
    });

    afterEach(function() {
        app.gridSnapModule.stop();
    });

    it('Module available', function() {
        expect(app.gridSnapModule).toBeDefined();
    });

    it('Generate grid', function(){
        var properties = app.gridSnapModule.getConfig();
        expect(properties.vertical.length).toEqual(properties.partsQuantityGrid);
    });

    it('Find near width', function(){
        var positionLeft = 10,
            width = 188;
        var newWidth = app.gridSnapModule.findNearWidth(positionLeft, width);
        expect(newWidth).toEqual(190);
    });

    it('Find near height', function(){
        var positionTop = 10,
            height = 115;
        var newHeight = app.gridSnapModule.findNearHeight(positionTop, height);
        expect(newHeight).toEqual(112);
    });

});