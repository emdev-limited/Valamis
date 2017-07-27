/**
 *  GridSnapModule
 *
 */
var ValamisGridSnapModule = function(GridSnapModule, App, Backbone, Marionette, $, _) {

    this.startWithParent = false;

    var gridOpts = {
        vertical: [],
        verticalCenter: [],
        horizontal: [],
        horizontalCenter: [],
        sideOffset: {left: -1, top: -1, right: 0, bottom: 0, center: 0},
        incidence: 6,
        partsQuantityDefault: 2,
        partsQuantityGrid: 12,
        partsQuantity: 2,
        square: false,
        visible: false,
        lineColor: ['#E0CC1A', '#A8F4FF'],
        linesContainer: '.slides-work-area-wrapper',
        itemsPos: []
    };

    this.generateGrid = function() {
        var containerSize = this.getContainerSize();
        if(_.isEmpty(containerSize)){
            return;
        }
        var partH = Math.ceil((containerSize.width - containerSize.margin * 2) / gridOpts.partsQuantity);
        var partsRangeH = _.range(containerSize.margin, containerSize.width - containerSize.margin - 1, partH);
        gridOpts.horizontal = [ 0, (containerSize.width - containerSize.margin), containerSize.width ];
        Array.prototype.splice.apply(gridOpts.horizontal, [1, 0].concat(partsRangeH));

        gridOpts.vertical = [ 0, containerSize.height - containerSize.margin, containerSize.height ];
        var partV = gridOpts.square
            ? partH
            : Math.ceil((containerSize.height - containerSize.margin * 2) / gridOpts.partsQuantity);
        var partsRangeV = _.range(containerSize.margin, containerSize.height - containerSize.margin - 1, partV);
        Array.prototype.splice.apply(gridOpts.vertical, [1, 0].concat(partsRangeV));

        gridOpts.verticalCenter = [ Math.round(containerSize.height / 2) ];
        gridOpts.horizontalCenter = [ Math.round(containerSize.width / 2) ];

        //Display grid
        if(gridOpts.visible){
            this.displayGrid();
        }
    };

    var findNear = function(pos, posName){
        var posValues = _.range(Math.round(pos) - gridOpts.incidence, Math.round(pos) + gridOpts.incidence);
        var near = _.intersection(gridOpts[posName],posValues);
        if(near.length == 0 && _.contains(['vertical','horizontal'], posName)){
            for(var i = 0; i < gridOpts.itemsPos.length; i++){
                near = _.intersection(gridOpts.itemsPos[i][posName], posValues);
                if(near.length > 0){
                    break;
                }
            }
        }
        return near;
    };

    this.onStart = function() {
        this.generateGrid();
    };

    this.prepareItemsSnap = function() {
        if(!App.activeElement.view) { return; }
        gridOpts.itemsPos = [];
        var activeElement = App.activeElement.view.model,
            entireId = activeElement.getId();
        _.each(App.activeSlideModel.getElements({classHidden: ''}),function(item){
            var itemId = item.getId();
            if(itemId != entireId){
                gridOpts.itemsPos.push({
                    vertical: [ parseInt(item.get('top')), parseInt(item.get('top')) + parseInt(item.get('height')) ],
                    horizontal: [ parseInt(item.get('left')), parseInt(item.get('left')) + parseInt(item.get('width')) ]
                });
            }
        });
    };

    this.findNearPos = function(pos, posName, sideName, returnCurrent) {
        var near = findNear(pos, posName);
        if( near.length > 0 ){
            this.addLine(near[0], posName, sideName);
            near[0] += gridOpts.sideOffset[sideName];
            return near[0];
        }else{
            this.removeLines(posName,sideName);
            return returnCurrent ? pos : null;
        }
    };

    this.findNearPosDelta = function(pos, posName, sideName) {
        var newPos = this.findNearPos(pos, posName, sideName, false);
        return newPos ? newPos - pos : 0;
    };

    this.findNearWidth = function(pos, width, returnCurrent) {
        var posSideRight = this.findNearPos(pos + width, 'horizontal', 'right', false);
        if(posSideRight){
            return posSideRight - pos;
        }else{
            return returnCurrent ? width : null;
        }
    };

    this.findNearHeight = function(pos, height, returnCurrent) {
        var posSideBottom = this.findNearPos(pos + height, 'vertical', 'bottom', false);
        if(posSideBottom){
            return posSideBottom - pos;
        }else{
            return returnCurrent ? height : null;
        }
    };

    this.findNearPosRatio = function(pos, posName, sideName) {
        var newPos = this.findNearPos(pos, posName, sideName, true);
        return newPos / pos;
    };

    this.getPosSideTop = function(posSideTop) {
        posSideTop = this.findNearPos(posSideTop, 'vertical', 'top', true);
        var posSideBottom = this.findNearPos(
            posSideTop + parseInt(App.activeElement.view.$el.height()),
            'vertical',
            'bottom',
            false
        );
        if( posSideBottom ){
            posSideTop = posSideBottom - parseInt(App.activeElement.view.$el.height());
        }
        else {
            var posSideCenter = this.findNearPos(
                posSideTop + (parseInt(App.activeElement.view.$el.height()) / 2),
                'vertical',//vertical | verticalCenter
                'center',
                false
            );
            if(posSideCenter){
                posSideTop = posSideCenter - (parseInt(App.activeElement.view.$el.height()) / 2);
            }
        }
        return posSideTop;
    };

    this.getPosSideLeft = function(posSideLeft) {
        posSideLeft = this.findNearPos(posSideLeft, 'horizontal', 'left', true);
        var posSideRight = this.findNearPos(
            posSideLeft + parseInt(App.activeElement.view.$el.width()),
            'horizontal',
            'right',
            false
        );
        if( posSideRight ){
            posSideLeft = posSideRight - parseInt(App.activeElement.view.$el.width());
        }
        else {
            var posSideCenter = this.findNearPos(
                posSideLeft + (parseInt(App.activeElement.view.$el.width()) / 2),
                'horizontal',//horizontal | horizontalCenter
                'center',
                false
            );
            if(posSideCenter){
                posSideLeft = posSideCenter - (parseInt(App.activeElement.view.$el.width()) / 2);
            }
        }
        return posSideLeft;
    };

    this.snapSize = function(direction, pos, size, originalSize, aspectRatio) {
        var ratio;
        // width
        if(_.contains(['e','se','ne'], direction)){
            var new_width = this.findNearWidth(pos.left, size.width, false);
            if(new_width){
                ratio = originalSize.height / originalSize.width;
                if(direction == 'ne' && aspectRatio){
                    pos.top -= (new_width - size.width) * ratio;
                }
                size.width = new_width;
                if(aspectRatio)
                    size.height = size.width * ratio;
            }
        }
        // height
        if(_.contains(['s','se','sw'], direction)){
            var new_height = this.findNearHeight(pos.top, size.height, false);
            if(new_height){
                ratio = originalSize.width / originalSize.height;
                if(direction == 'sw' && aspectRatio){
                    pos.left -= (new_height - size.height) * ratio;
                }
                size.height = new_height;
                if(aspectRatio)
                    size.width = size.height * ratio;
            }
        }
        return size;
    };

    this.snapTopResize = function(direction, pos, size, originalSize, aspectRatio){
        var posSideTopDelta = this.findNearPosDelta(pos.top, 'vertical', 'top');
        var ratio = originalSize.width / originalSize.height;
        if(_.contains(['nw', 'sw'], direction) && aspectRatio){
            pos.left += posSideTopDelta * ratio;
        }
        pos.top += posSideTopDelta;
        size.height -= posSideTopDelta;
        if(aspectRatio){
            size.width = size.height * ratio;
        }
    };

    this.snapLeftResize = function(direction, pos, size, originalSize, aspectRatio) {
        var posSideLeftDelta = this.findNearPosDelta(pos.left, 'horizontal', 'left');
        var ratio = originalSize.height / originalSize.width;
        if(direction == 'nw'){
            pos.top += posSideLeftDelta * ratio;
        }
        pos.left += posSideLeftDelta;
        size.width -= posSideLeftDelta;
        if(aspectRatio){
            size.height = size.width * ratio;
        }
    };

    this.addLine = function(pos, posName, sideName, type, margin) {

        type = type || 'line';
        margin = margin || 0;
        var lineColor, offset;
        if( type == 'grid' ){
            lineColor = gridOpts.lineColor[1];
            offset = 0 - margin;
        } else {
            lineColor = gridOpts.lineColor[0];
            offset = 10 - margin;
        }

        var parent = $(gridOpts.linesContainer),
            isVertical = posName.indexOf('vertical') > -1,
            leftPos = isVertical ? 0 - offset : pos,
            topPos = isVertical ? pos : 0 - offset,
            height = isVertical ? 1 : parent.height() + offset * 2,
            width = isVertical ? parent.width() + offset * 2 : 1;

        if( $('#gridLine-' + [posName, sideName, pos].join('-') + '.' + type).length == 0 ){
            $('<div/>',{
                'id': 'gridLine-' + [posName, sideName, pos].join('-'),
                'class': 'grid-line ' + type + ' grid-line-' + [posName, sideName].join('-')
            })
                .css({
                    width: width,
                    height: height,
                    left: leftPos,
                    top: topPos,
                    backgroundColor: lineColor
                })
                .appendTo(parent);
        }

    };

    this.removeLines = function(posName, sideName) {
        if( typeof posName != 'undefined' ){
            $(gridOpts.linesContainer).find('.grid-line-' + [posName, sideName].join('-')).not('.grid').remove();
        }else{
            $('.grid-line', gridOpts.linesContainer).not('.grid').remove();
        }
    };

    this.removeGrid = function() {
        $('.grid-line.grid', gridOpts.linesContainer).remove();
    };

    this.disableGrid = function() {
        if( $(gridOpts.linesContainer).find('.grid-line').length > 0 ){
            this.displayGridToggle();
        }
    };

    this.getLayoutSize = function(){
        var deviceLayoutCurrent = App.devicesCollection.getCurrent();
        if(!deviceLayoutCurrent){
            return {};
        }
        var containerSize = {
            width: deviceLayoutCurrent.get('minWidth') || deviceLayoutCurrent.get('maxWidth'),
            height: App.activeSlideModel && App.activeSlideModel.get('height')
                ? App.activeSlideModel.get('height')
                : deviceLayoutCurrent.get('minHeight'),
            margin: deviceLayoutCurrent.get('margin')
        };
        return containerSize;
    };

    this.getContainerSize = function(){
        var containerSize = this.getLayoutSize();
        if(!_.isEmpty(containerSize)){
            containerSize = _.mapValues(containerSize, function(val){
                return parseInt(val, 10);
            });
            var excess = (containerSize.width - containerSize.margin * 2) % gridOpts.partsQuantityGrid;//Always depends on the grid size
            containerSize.margin = Math.ceil(containerSize.margin + excess / 2);
        }
        return containerSize;
    };

    this.displayGridToggle = function(){

        if( $(gridOpts.linesContainer).find('.grid-line').length > 0 ){
            this.removeGrid();
            this.configure({
                partsQuantity: gridOpts.partsQuantityDefault,
                square: false,
                visible: false
            });
            GridSnapModule.generateGrid();

        } else {

            this.configure({
                partsQuantity: gridOpts.partsQuantityGrid,
                square: true,
                visible: true
            });
            GridSnapModule.generateGrid();
        }

    };

    this.displayGrid = function(){
        this.removeGrid();
        var containerSize = this.getContainerSize();
        if(!_.isEmpty(containerSize)){
            _.each(gridOpts.horizontal, function(value, key){
                if( _.contains([0, gridOpts.horizontal.length - 1], key) ) return;
                GridSnapModule.addLine(value, 'horizontal', 'left', 'grid', containerSize.margin);
            });
            _.each(gridOpts.vertical, function(value, key){
                if( _.contains([0, gridOpts.vertical.length - 1], key) ) return;
                GridSnapModule.addLine(value, 'vertical', 'top', 'grid', containerSize.margin);
            });
        }
    };

    this.getConfig = function(){
        return gridOpts;
    };

    this.configure = function(opts){
        _.extend( gridOpts, opts );
    };

};
