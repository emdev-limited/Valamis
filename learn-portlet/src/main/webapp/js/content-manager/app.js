/**
 * Created by igorborisov on 10.04.15.
 */

var ContentManager = Marionette.Application.extend({
    channelName: 'contentManager',
    rootNodes: [],
    options: {
        initialized: false,
        currentCategory: 0
    },
    initialize: function(options) {
        this.addRegions({
            mainRegion: '#contentManagerAppRegion'
        });
    },
    onStart: function(options){
        this.selectMode = options.selectMode || false;
        this.options.currentCategory = this.getCurrentCategoryId();
        var layoutView = new contentManager.Views.AppLayoutView({
            showGlobalBase : options.showGlobalBase,
            selectMode: this.selectMode
        } );
        this.mainRegion.show(layoutView);
        this.mainRegion.currentView.contents
            .on('show', function(){
                contentManager.initialized = true;
            });
        jQueryValamis(document).bind('click', this.blurEventAction.bind(this));
    },
    onStop: function(){
        this.initialized = false;
        jQueryValamis(document).unbind('click', this.blurEventAction);
    },
    blurEventAction: function(e){
        if( !this.initialized ) return;

        var self = this;
        var target = e.target || e.currentTarget,
            workArea = jQueryValamis('#categoryContentView')
                .add('#contentManagerContentPreview')
                .add('#contentManagerContentsView')
                .add('.dropdown-menu'),
            context = this.mainRegion.currentView.$el,
            contentRegion = self.mainRegion.currentView.regionManager.get('content').currentView,
            isButton = jQueryValamis(target).is('button') || jQueryValamis(target).parent().is('button');

        //if CM opened in modal window
        if( self.mainRegion.currentView.$el.closest('.val-modal').size() > 0 ){
            workArea = workArea.add('#valamisAppModalRegion .val-modal:gt(0)');
        } else {
            workArea = workArea.add('#valamisAppModalRegion');
        }

        if( !!contentRegion && !isButton  &&  jQueryValamis(target).closest(workArea,context).size() === 0 ){
            contentRegion.$el.find('.js-content-list li').removeClass('active-item');
            contentRegion.activeItemId = '';
            if( contentRegion.preview.currentView ){
                contentRegion.preview.currentView.$el.hide();
            }
        }

    },
    getCurrentCategoryId: function(){
        var output = localStorage.getItem('ValamisCMCategory');
        return output && !isNaN( output ) ? parseInt( output ) : '';
    },
    openCategory: function( categoryId ){
        var self = this;
        categoryId = categoryId || self.options.currentCategory;

        var contentsTreeRegion = self.mainRegion.currentView.regionManager.get('contents'),
            categoryView = contentsTreeRegion.currentView;

        if( categoryId ){
            var categoriesIds = self.TreeViews.getTreeIds(categoryId);
            categoriesIds.reverse();
            categoriesIds.forEach(function(model_id){
                if( categoryView = categoryView.findByModelId(model_id) ){
                    categoryView.collapsed = false;
                    categoryView.updateCollapsed();
                }
            });
        }
        if( categoryView ){
            categoryView.selectNode();
        }
    }
});

var contentManager = new ContentManager();


contentManager.commands.setHandler('selected:questions:update', function(updateValue){
    if (contentManager.selectMode) {
        var model = contentManager.mainRegion.currentView.topbar.currentView.model;
        if (updateValue) {
            var oldValue = model.get('selectedQuestions');
            model.set({'selectedQuestions': oldValue + updateValue});
        }
        else {
            var selectedQuestions = contentManager.mainRegion.currentView.content.currentView.toolbar.currentView.getSelectedItems();
            var totalNodes = 0;
            _.each(selectedQuestions, function (node) {
                totalNodes += node.get('childrenAmount') || 1;
            });
            model.set({'selectedQuestions': totalNodes});
        }
    }
});

//category methods
contentManager.commands.setHandler('category:edit', function(category){

    var editCategoryView = new contentManager.Views.EditCategoryView({
        model: category
    });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['editCategoryLabel'],
        contentView: editCategoryView,
        customClassName: 'content-manager-new-category light-val-modal',
        beforeSubmit: function() {
            return this.contentView.updateModel();
        },
        submit: function(){
            category.save();
        },
        onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });
    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});

contentManager.commands.setHandler('category:delete', function(category){
    valamisApp.execute('valamis:confirm', {message: Valamis.language['warningDeleteMessageLabel']}, function(){
        category.destroy();
    });
});

contentManager.commands.setHandler('category:add', function(category, parent) {
    var addCategoryView = new contentManager.Views.AddCategoryView({
        model: category
    });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['createNewCategoryLabel'],
        contentView: addCategoryView,
        customClassName: 'content-manager-new-category light-val-modal',
        beforeSubmit: function() {
            return addCategoryView.updateModel();
        },
        submit: function(){
            category.save().then(function(model){
                var newCategory = new contentManager.Entities.TreeCategory(model);
                newCategory.set('defaultIndex', parent.nodes.length);
                parent.nodes.add(newCategory);
                newCategory.fetchChildren();
            });
        },
        onDestroy: function () {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });

    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});

contentManager.commands.setHandler('content:items:update', function() {

    var contentRegion = contentManager.mainRegion.currentView.regionManager.get('content'),
        contentListRegion = contentRegion.currentView.regionManager.get('content'),
        contentModel = contentListRegion.currentView.model;

    if( contentModel ){
        var courseId = contentModel.get('courseId'),
            modelId = contentModel.get('id'),
            rootNode = contentManager.rootNodes[courseId];
        var treeContent = (!modelId || modelId <= 0) ? rootNode : rootNode.getChildNode( contentModel.get('id') );

        contentModel.nodes.reset( treeContent.nodes.toJSON() );
        contentRegion.currentView.toolbar.currentView.render();
    }

});


//question methods
contentManager.commands.setHandler('question:edit', function(question){

    var editQuestionView = new contentManager.Views.EditQuestionLayout({
        model: question
    });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['buttonEditQuestionTooltipLabel'],
        customClassName: 'content-edit-modal',
        contentView: editQuestionView,
        beforeSubmit: function() {
            return editQuestionView.validate();
        },
        submit: function(){
            editQuestionView.updateModel();
            question.save();
        },
        onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });

    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});

contentManager.commands.setHandler('question:delete', function(question){

    valamisApp.execute('valamis:confirm', {message: Valamis.language['warningDeleteMessageLabel']}, function(){
        question.destroy();
    });
});

contentManager.commands.setHandler('question:add', function(question, parent){

    var editQuestionView = new contentManager.Views.EditQuestionLayout({
        model: question
    });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['newQuestionLabel'],
        contentView: editQuestionView,
        beforeSubmit: function() {
            return editQuestionView.validate();
        },
        submit: function(){
            editQuestionView.updateModel();

            question.save().then(function(){
                question.set('defaultIndex', parent.nodes.length);
                parent.nodes.add(question);
            });
        },
        onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });

    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});


contentManager.commands.setHandler('content:clone',function(model, parent) {
    var contentType = model.get('contentType');
    var clone = model.clone();
    clone.set('copyFromId', model.get('id'));
    clone.unset('id');
    clone.unset('uniqueId');

    if(contentType == 'category'){
        contentManager.execute('category:add', clone, parent);
    }else if(contentType == 'question' || contentType == 'plaintext'){
        if(model.isContent()) {
            contentManager.execute('content:add', clone, parent);
        }else{
            contentManager.execute('question:add', clone, parent);
        }
    }

});


contentManager.commands.setHandler('content:add', function(content, parent){

    var editQuestionView = new contentManager.Views.EditPlainTextQuestionView({
        model: content
    });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['createNewContentLabel'],
        contentView: editQuestionView,
        beforeSubmit: function() {
            return editQuestionView.validate();
        },
        submit: function(){
            editQuestionView.updateModel();

            content.save().then(function(){
                content.set('defaultIndex', parent.nodes.length);
                parent.nodes.add(content);
            });
        },
        onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });

    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});

contentManager.commands.setHandler('content:edit', function(content){

    var editQuestionView = new contentManager.Views.EditPlainTextQuestionView({
        model: content
    });

    var modalView = new valamisApp.Views.ModalView({
        contentView: editQuestionView,
        customClassName: 'content-edit-modal',
        beforeSubmit: function() {
            return editQuestionView.validate();
        },
        submit: function(){
            editQuestionView.updateModel();
            content.save();
        },
        onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
        }
    });

    valamisApp.execute('modal:show', modalView);
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
});

contentManager.commands.setHandler('content:delete', function(content){
    valamisApp.execute('valamis:confirm', {message: Valamis.language['warningDeleteMessageLabel']}, function(){
        content.destroy();
    });
});

contentManager.commands.setHandler('content:items:delete', function(contentItems){

    valamisApp.execute('valamis:confirm', {message: Valamis.language['warningDeleteMessageLabel']}, function(){
        _.each(contentItems, function(item){
            item.destroy();
        });
    });
});

contentManager.commands.setHandler('content:items:export', function(contentItems){

    var export_url = path.root + path.api.files + 'export/?action=EXPORT&contentType=question&courseId=' + Utils.getCourseId();
    _.each(contentItems, function(item){
        var id = item.get('id');
        if(item.get('contentType') == 'category'){
            export_url += '&categoryID=' + id;
        } else if(item.get('contentType') == 'question'){
            export_url += '&id=' + id;
        } else if (item.get('contentType') == 'plaintext') {
            export_url += '&plainTextId=' + id;
        }
    });

    window.location = export_url;
});

contentManager.commands.setHandler('content:items:move:to:course', function(contentItems, parent){

    if(_.isEmpty(contentItems)) {
        //TODO some info about empty list?
        return;
    }

    var siteSelectView = new valamisApp.Views.SelectSite.LiferaySiteSelectLayout({ singleSelect: true });

    var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['moveToCourseLabel'],
        contentView: siteSelectView
    });

    siteSelectView.on('liferay:site:selected', function(site) {

        var newCourseId = site.get('id');

        var categories = contentItems.filter(function(item){
            return item.get('contentType') == 'category';
        });

        var questions = contentItems.filter(function(item){
            return item.get('contentType') == 'question';
        });

        var plainTexts = contentItems.filter(function(item){
            return item.get('contentType') == 'plaintext';
        });


        var categoryIds = categories.map(function(item){return item.get('id')});
        var questionIds = questions.map(function(item) {return item.get('id')});
        var plainTextIds = plainTexts.map(function(item) {return item.get('id')});

        //TODO refactor this

        var itemCount = questionIds.length+plainTextIds.length

        var successCallBack = function(){
            valamisApp.execute('modal:close', modalView);

            //var newParent = contentManager.rootNodes[newCourseId]
            //newParent.fetchChildren().then(function(){
            //    newParent.updateContentAmount();
            //});

            parent.fetchChildren().then(function(){
                parent.updateContentAmount();
            });

            valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        };

        var errorCallback = function(){
            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        };

        var sendContentItems = function() {
            var isOk=true;

            if (!_.isEmpty(questionIds)) {
                window.LearnAjax.post(path.root + path.api.questions+"moveToCourse", {
                    'questionIDs': questionIds,
                    'newCourseID': newCourseId,
                    'courseId': Utils.getCourseId()
                }).fail(function () {
                    isOk=false;
                });
            }

            if (!_.isEmpty(plainTextIds)) {
                window.LearnAjax.post(path.root + path.api.plainText+"moveToCourse", {
                    'contentIDs': plainTextIds,
                    'newCourseID': newCourseId,
                    'courseId': Utils.getCourseId()
                }).fail(function () {
                    isOk=false;
                });
            }

            if (isOk) {
                successCallBack();
            } else {
                errorCallback();
            }
        };

        var sendCategories = function() {
            return window.LearnAjax.post(path.root + path.api.category+"moveToCourse", {
                'categoryIDs': categoryIds,
                'newCourseID': newCourseId,
                'courseId': Utils.getCourseId()
            })
        }

        if (!_.isEmpty(categoryIds) && itemCount!=0) {
            sendCategories().success(function() {
                sendContentItems();
            }).fail(function () {
                errorCallback();
            });
        }

        if (_.isEmpty(categoryIds) && itemCount!=0) {
            sendContentItems();
        }

        if (!_.isEmpty(categoryIds) && itemCount==0) {
            sendCategories().success(function () {
                successCallBack();
            }).fail(function () {
                errorCallback();
            });
        }
    });

    valamisApp.execute('modal:show', modalView);
});

contentManager.commands.setHandler('move:content:item', function(movedModel, index, parentId, contentType){
    var oldParentId = movedModel.getParentId();
    var newParentId = parentId;

    movedModel.move({}, {parentId: newParentId, index: index}).then(function(){
        var courseId = movedModel.get('courseId');
        var rootNode = contentManager.rootNodes[courseId];
        var oldParent = (!oldParentId || oldParentId <= 0) ? rootNode : rootNode.getChildNode(oldParentId);

        oldParent.fetchChildren({reset:true}).then(function(){
            oldParent.updateContentAmount();
            contentManager.execute('content:items:update');
        });

        if(oldParentId !== newParentId) {
            var newParent = (!newParentId || newParentId <= 0)? rootNode : rootNode.getChildNode(newParentId);

            if (typeof newParent.fetchChildren !== 'function') {//newParent in another course
                //so search for it in another courses
                contentManager.rootNodes.some(function(rNode) {
                    var childNode = rNode.getChildNode(newParentId)
                    if (typeof childNode.fetchChildren === 'function') {
                        newParent = childNode
                        return true;
                    } else {
                        return false;
                    }
                })
            }

            newParent.fetchChildren({reset: true}).then(function () {
                newParent.updateContentAmount();
            });

        }
    });

});