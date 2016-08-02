navigationProxy = null;

TincanNode1DCollection = NavigationNodeCollection.extend({
    linearRepresentation: null,
    toggle: function(indexH){
        this.toggleHelper(this.linearRepresentation[indexH]);
    },
    navigate: function(model){
        navigationProxy.slide(model.index1D);
    },
    afterInitialization: function() {
        this.linearRepresentation = [];
        var index1D = 0;
        var that = this;
        this.each(function (model) {
            if (model.get('elementType') == 'directory') {
                model.internalCollection = new NavigationNodeCollection(model.get('childElements'));
                model.internalCollection.each(function (qModel) {
                    that.listenTo(qModel,'internal-navigate', that.navigate);
                    that.linearRepresentation.push(qModel);
                    qModel.index1D = index1D;
                    index1D += 1;
                });
            } else {
                that.listenTo(model,'internal-navigate', that.navigate);
                that.linearRepresentation.push(model);
                model.index1D = index1D;
                index1D += 1;
            }
        });
    }
});

TincanNode2DCollection = NavigationNodeCollection.extend({
    toggle: function(indexH, indexV){
        if(!navigationProxy.tinCanPackageRendererInitialized()) return;

        var modelToToggle = this.models[indexH].get('elementType') == 'directory' ?
            this.models[indexH].internalCollection.models[indexV]:
            this.models[indexH];
        this.toggleHelper(modelToToggle);
    },
    navigate: function(model){
        navigationProxy.slide(model.indexH, model.indexV);
    },
    afterInitialization: function(){
        var indexH = 0,
            indexV = 0;
        var that = this;
        this.each(function(model){
            if(model.get('elementType') == 'directory') {
                model.internalCollection = new NavigationNodeCollection(model.get('childElements'));
                model.internalCollection.each(function (qModel) {
                    that.listenTo(qModel,'internal-navigate', that.navigate);
                    qModel.indexH = indexH;
                    qModel.indexV = indexV;
                    indexV += 1;
                });
            } else {
                that.listenTo(model,'internal-navigate', that.navigate);
                model.indexH = indexH;
                model.indexV = indexV;
            }
            indexH += 1;
            indexV = 0;
        });
    }
});


navigationProxy = {
    destroyNavigation: function(){
        this.getTreeContainer().html("");
    },
    getTreeContainer: function(){
        return jQueryValamis('#playerTree');
    },
    navigationNodeCollection: null,
    display1DRandomizedNavigation: function(randomizedIndexes, serializedQuestionData){
        var mappedQuestionData = _.map(serializedQuestionData, this.parseModel1D, this);

        var linearizedMappedQuestions = [];
        (function fillLinearizedMappedQuestion(nodes){
            _(nodes).forEach(function(node) {
                if (node.elementType == "directory") fillLinearizedMappedQuestion(node.childElements);
                else linearizedMappedQuestions.push(node);
            })
        })(mappedQuestionData);

        this.data = _.map(randomizedIndexes, function(index){
            return _.find(linearizedMappedQuestions, function(mappedQuestion){
                return mappedQuestion.id === index;
            });
        });

        this.navigationNodeCollection = new TincanNode1DCollection(this.data);
        this.displayNavigationHelper();
    },
    display1DNavigation: function(serializedQuestionData){
        this.data = _.map(serializedQuestionData, this.parseModel1D, this);
        this.navigationNodeCollection = new TincanNode1DCollection(this.data);
        this.displayNavigationHelper();
    },
    display2DNavigation: function(serializedQuestionData){
        this.data = this.parseData2D(serializedQuestionData);
        this.navigationNodeCollection = new TincanNode2DCollection(this.data);
        this.displayNavigationHelper();
    },
    displayNavigationHelper: function(){
        this.navigationNodeCollection.afterInitialization();
        lessonViewer.playerRegion.currentView.buildTincanTree(this.navigationNodeCollection);
    },
    getSlideTitle2D: function(event){
        if(this.tinCanPackageRendererInitialized()) return this.data[event.indexh].childElements[event.indexV];
    },
    toggle2D: function(indexh, indexv){
        this.navigationNodeCollection.toggle(indexh, indexv);
    },
    toggle1D: function(indexh){
        this.navigationNodeCollection.toggle(indexh);
    },
    playerDisplayContentIframe: function() {
        return jQueryValamis("#playerDataOutput")[0].contentWindow
    },
    slide: function(indexH, indexV){
        this.playerDisplayContentIframe().Reveal.slide(indexH, indexV);
    },
    parseModel: function(model){
        var id = model.id;
        var elementType;
        var title = model.title;
        var duration = model.duration;

        return {
            id: id,
            title: title,
            elementType: elementType,
            duration: duration
        }
    },
    parseModel1D: function(model){
        var parsedModel = this.parseModel(model);
        return model.jsonClass == "TinCanQuizPackageGenerator$Category" ?
            _.extend(parsedModel, {childElements: _.map(model.categoryQuestions, this.parseModel1D, this)}) :
            parsedModel;
    },
    parseData2D: function(data){
        var result = [];

        var first = _.find(data, function(slide){
            return typeof slide.leftSlideId == 'undefined' && typeof slide.topSlideId == 'undefined';
        });
        result.push({
            title: first.title,
            elementType: 'directory',
            duration: first.duration,
            childElements: [],
            prevModel: first
        });

        (function composeHorizontal(leftSlide, array){
            var nextHorizontal = _.find(array, function(el){
                return leftSlide.id == el.leftSlideId
            });

            if(typeof nextHorizontal != 'undefined'){
                result.push({
                    title: nextHorizontal.title,
                    elementType: 'directory',
                    duration: nextHorizontal.duration,
                    childElements: [],
                    prevModel: nextHorizontal
                });
                composeHorizontal(nextHorizontal, array)
            }
        })(first, data);

        function composeVertical(topSlide, directoryElement, array){
            var nextVertical = _.find(array, function(el){
                return typeof el.topSlideId != 'undefined' && topSlide.prevModel.id == el.topSlideId
            });

            if(typeof nextVertical != 'undefined'){
                var n = {
                    title: nextVertical.title,
                    elementType: 'page',
                    duration: nextVertical.duration,
                    prevModel: nextVertical
                };
                directoryElement.childElements.push(n);
                composeVertical(n, directoryElement, array);
            }
        }

        _.forEach(result, function(elem){
            elem.childElements.push({
                title: elem.title,
                elementType: 'page',
                duration: elem.duration,
                prevModel: elem.prevModel
            });
            composeVertical(elem, elem, data);
        });

        return result;
    },
    tinCanPackageRendererInitialized: function(){ //FIXME: remove, shouldn't depend on slidesApp
        var iframe = this.playerDisplayContentIframe();
        return iframe.slidesApp && !iframe.slidesApp.initializing ? true : false
    }
};