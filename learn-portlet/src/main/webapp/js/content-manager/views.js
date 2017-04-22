/**
 * Created by igorborisov on 10.04.15.
 */

contentManager.module("Views", function (Views, ContentManager, Backbone, Marionette, $, _) {

    Views.EditCategoryView = Marionette.ItemView.extend({
        template: '#contentManagerCategoryEditView',
        updateModel: function () {
            var title = this.$('.js-category-title').val();
            if (title.length === 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['titleIsEmptyError']);
                return false;
            }
            this.model.set({
                title: title,
                description: ''
            });
            return true;
        },
        onShow:function(){
            this.$('.js-category-title').focus();
        }
    });

    Views.AddCategoryView = Marionette.ItemView.extend({
        template: '#contentManagerCategoryAddView',
        updateModel: function () {
            var title = this.$('.js-category-title').val() || Valamis.language['newCategoryDefaultTitle'];
            if (title.length === 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['titleIsEmptyError']);
                return false;
            }
            this.model.set({
                title: title,
                description: ''
            });
            return true;
        },
        onShow:function(){
            this.$('.js-category-title').focus();
        }
    });

    Views.ToolbarView = Marionette.ItemView.extend({
        className: 'div-table',
        events: {
            'click .js-add-category': 'addCategory',
            'click .js-add-question' : 'addQuestion',
            'click .js-add-content' : 'addContent',
            'click .js-select-all' : 'selectAll',
            'click .js-delete-items': 'deleteItems',
            'click .js-export-items': 'exportItems',
            'click .js-move-items': 'moveItems',
            'click .js-sortby-items': 'sortItems',
            'keyup .js-filter-by-title': 'filterByTitle',
            'click .js-filter-by-type li': 'filterByType'
        },
        initialize: function(){
            this.selectAllValue = false;
            this.filterModel = new ContentManager.Entities.Filter();

            var that = this;
            this.filterModel.on('change', function() {
                that.model.nodes.filterNodes(that.filterModel.toJSON())
            });

            this.template = (this.options.selectMode)
                ? '#contentManagerToolbarTemplate'
                : '#contentManagerFilterToolbarTemplate';

        },
        behaviors: {
            ValamisUIControls: {}
        },
        templateHelpers: function(){
            var sortbyIndex = ContentManager.Entities.options.sortbyIndex,
                sortOptions = [
                    {
                        name: Valamis.language.byTitleLabel,
                        value: 0,
                        selected: sortbyIndex === 0
                    },
                    {
                        name: Valamis.language.byTypeLabel,
                        value: 1,
                        selected: sortbyIndex === 1
                    }
                ];
            return {
                sortByCurrent: sortOptions[ sortbyIndex ].name,
                sortByList: sortOptions
            }
        },
        onRender: function() {
            this.filterModel.set(this.filterModel.defaults, {silent: true});
        },
        addCategory: function(){
            var newCategory = new contentManager.Entities.TreeCategory({parentId : this.model.get('id'), courseId: this.model.get('courseId')});
            contentManager.execute('category:add', newCategory, this.model);
        },
        addQuestion: function(){
            var newQuestion = new contentManager.Entities.TreeQuestion({
                categoryID : this.model.get('id'),
                courseId: this.model.get('courseId')
            });
            contentManager.execute('question:add', newQuestion, this.model);
        },
        addContent: function(){
            var newQuestion = new contentManager.Entities.TreeQuestion({
                categoryID : this.model.get('id'),
                courseId: this.model.get('courseId'),
                questionType: 8
            });
            contentManager.execute('content:add', newQuestion, this.model);
        },
        selectAll:function(){
            var that = this;
            that.selectAllValue = !that.selectAllValue;

            that.model.nodes.each(function(item){
                item.set('selected', that.selectAllValue);
            });
            contentManager.execute('selected:questions:update');
        },
        getSelectedItems:function(){
            var selectedItems = this.model.nodes.filter(function(item){
                return item.get('selected') && !item.get('hidden');
            });
            return selectedItems;
        },
        deleteItems:function(){
            var selectedItems = this.getSelectedItems();
            if (this.checkSelectedItem(selectedItems))
            contentManager.execute('content:items:delete', selectedItems);
        },
        exportItems:function(){
            var selectedItems = this.getSelectedItems();
            if (this.checkSelectedItem(selectedItems))
            contentManager.execute('content:items:export', selectedItems);
        },
        moveItems:function(){
            var selectedItems = this.getSelectedItems();
            contentManager.execute('content:items:move:to:course', selectedItems, this.model);
        },
        sortItems: function(e){
            var target = e.currentTarget || e.target,
                sortbyValue = parseInt( this.$(target).data('value') );
            if( ContentManager.Entities.options.sortby[ sortbyValue ]
                && sortbyValue != ContentManager.Entities.options.sortbyIndex ){
                ContentManager.Entities.options.sortbyIndex = sortbyValue;
                this.model.nodes.sort();
            }
        },
        checkSelectedItem : function(selectedItems){
            if (selectedItems.length == 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['overlaySelectedMessageLabel']);
                return false;
            }
            return true;
        },
        filterByTitle: function(e) {
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.filterModel.set({'titlePattern': $(e.target).val()});
            }, 800);
        },
        filterByType: function(e) {
            var dataValue = $(e.target).attr('data-value');
            if (dataValue.indexOf('type') > -1) {
                var value = dataValue.replace('type', '');
                this.filterModel.set({'type': 'question', 'typeValue': value});
            } else
                this.filterModel.set({'type': dataValue, 'typeValue': ''});
        }
    });


    Views.ContentItemView = Marionette.ItemView.extend({
        tagName: 'li',
        events: {
            'click .js-edit-content': 'editItem',
            'click .js-delete-content': 'deleteItem',
            'click .js-clone-content': 'cloneItem',
            'click .js-select-entity': 'selectItem',
            'click' : 'activateItem'
        },
        modelEvents: {
            'change:title': 'render',
            'change:questionType': 'render',
            'change:selected': 'selectedChanged',
            'change:hidden':'hiddenChanged'
        },
        activateItem: function(e) {
            // do not show right panel if select question
            var isCheckboxTarget = $(e.target).hasClass('js-select-entity-label') || $(e.target).hasClass('js-select-entity');

            if (!isCheckboxTarget) {
                var self = this;
                clearTimeout(window.timer);
                window.timer = setTimeout(function () {//exclude double click
                    self.triggerMethod('activate:item');
                    self.$el.addClass('active-item');
                }, 200);
            }
        },
        onRender: function(){
            this.$el.attr('data-id', this.model.get('id'));
            this.$el.toggleClass('hidden', this.model.get('hidden'));
        },
        selectItem: function(){
            this.model.set('selected', this.$('.js-select-entity').is(':checked'), {silent: true});
            var updateValue = ((this.model.get('selected')) ? 1 : -1) * (this.model.get('childrenAmount') || 1);
            contentManager.execute('selected:questions:update', updateValue);
        },
        selectedChanged: function(){
            this.$('.js-select-entity').prop('checked', this.model.get('selected'));
        },
        cloneItem: function(e){
            e.stopPropagation();
            this.triggerMethod('content:item:clone');
        },
        hiddenChanged: function() {
            this.$el.toggleClass('hidden', this.model.get('hidden'));
        }
    });

    Views.QuestionContentItemView = Views.ContentItemView.extend({
        className: 'question lesson-item-li',
        template: '#contentManagerContentQuestionTemplate',
        editItem : function(e){
            e.stopPropagation();

            this.triggerMethod('activate:item');
            this.$el.addClass('active-item');

            if(this.model.isContent()){
                contentManager.execute('content:edit', this.model);
            }else{
                contentManager.execute('question:edit', this.model);
            }
        },
        deleteItem: function(e){
            e.stopPropagation();
            contentManager.execute('question:delete', this.model);
        }
    });

    Views.PlainTextContentItemView = Views.ContentItemView.extend({
        className: 'plaintext lesson-item-li',
        template: '#contentManagerContentQuestionTemplate',
        editItem : function(e){
            e.stopPropagation();

            this.triggerMethod('activate:item');
            this.$el.addClass('active-item');

            if(this.model.isContent()){
                contentManager.execute('content:edit', this.model);
            }else{
                contentManager.execute('question:edit', this.model);
            }
        },
        deleteItem: function(e){
            e.stopPropagation();
            contentManager.execute('question:delete', this.model);
        }
    });

    Views.CategoryContentItemView = Views.ContentItemView.extend({
        className: 'category lesson-item-li',
        template: '#contentManagerContentCategoryTemplate',
        events: {
            'dblclick' : 'openCategory'
        },
        initialize: function(){
            _.extend(this.events, Views.ContentItemView.prototype.events);
        },
        editItem : function(e){
            e.stopPropagation();
            contentManager.execute('category:edit', this.model);
            this.triggerMethod('activate:item');
            this.$el.addClass('active-item');
        },
        deleteItem: function(e){
            e.stopPropagation();
            contentManager.execute('category:delete', this.model);
        },
        openCategory: function(e){
            e.preventDefault();
            clearTimeout(window.timer);
            contentManager.openCategory(this.model.get('id'));
        }
    });

    Views.CategoryContentList = Marionette.CompositeView.extend({
        'tagName': 'div',
        'className': 'val-lesson-content',
        template: '#contentManagerContentListTemplate',
        childViewContainer: "ul.js-content-list",
        initialize: function(){
            this.collection = this.model.nodes;
            this.isSortable = this.options.isSortable;

            var that = this;
            this.collection.on('sync', function() {
                that.triggerMethod('content:list:reload');
            })
        },
        getChildView: function(model){
            if(model.get('contentType') == 'question') {
                return Views.QuestionContentItemView;
            }
            if(model.get('contentType') == 'category') {
                return Views.CategoryContentItemView;
            }
            if (model.get('contentType') == 'plaintext') {
                return Views.PlainTextContentItemView;
            }
        },
        childEvents: {
            'activate:item' : function(childView){
                this.$('li').removeClass('active-item');
                this.triggerMethod('content:list:activate:item', childView.model);
            },
            'content:item:clone': function(childView){
                this.triggerMethod('content:list:clone:item', childView.model);
            }
        },
        onRender: function(){
            if (this.isSortable)
                this.sortable();

            this.$el.on( "sortstart", function( event, ui ) {
//                ui.item.find(".js-tree-item-drag").show();
//                ui.item.find(".js-tree-item").hide();
            });

            this.$el.on( "sortstop", function( event, ui ) {
//                ui.item.find(".js-tree-item").show();
//                ui.item.find(".js-tree-item-drag").hide();
            });
        },
        //updateOrdering: function(movedModel, index, parentId, contentType){
        //    contentManager.execute('move:content:item', movedModel, index, parentId, contentType);
        //},
        sortable: function() {
            var that = this;

            that.$('.js-content-list')
                .sortable({
                    placeholder: 'ui-state-highlight',
                    start: function(e, ui){
                        var item_id = ui.item.data('id');
                        //blur clone in tree
                        if( ui.item.is('.category') ){
                            var contentsTreeRegion = contentManager.mainRegion.currentView.regionManager.get('contents');
                            contentsTreeRegion.currentView.$('.category[data-id="'+item_id+'"]')
                                .children('.tree-item')
                                .droppable('option', 'disabled', true);
                        }
                        //hide controls
                        ui.item
                            .css({
                                transform: 'scale(0.7)',
                                transformOrigin: 'center center',
                                opacity: 0.7
                            })
                            .find('.lesson-item-controls')
                            .addClass('invisible');
                        jQueryValamis.removeData( ui.item, 'dropped' );
                    },
                    stop: function(e, ui){
                        var item_id = ui.item.data('id');
                        //show clone in tree
                        if( ui.item.is('.category') ){
                            var contentsTreeRegion = contentManager.mainRegion.currentView.regionManager.get('contents');
                            contentsTreeRegion.currentView.$('.category[data-id="'+item_id+'"]')
                                .children('.tree-item')
                                .droppable('option', 'disabled', false);
                        }
                        //show controls
                        ui.item
                            .css({
                                transform: 'scale(1)',
                                opacity: 1
                            })
                            .find('.lesson-item-controls')
                            .removeClass('invisible');
                    },
                    change: function(e, ui) {

                        var isTree = ui.placeholder.closest('.val-tree').size() > 0;
                        var isAllowed = false;
                        if ( !isTree ) {
                            if (ui.item.hasClass('question') && ui.placeholder.next('.category').size() == 0)
                                isAllowed = true;
                            if (ui.item.hasClass('category') && ui.placeholder.prev('.question').size() == 0)
                                isAllowed = true;
                        }
                        ui.placeholder.toggleClass('ui-state-error',!isAllowed);

                    },
                    update: function(e, ui){
                        if( ui.placeholder.is('.ui-state-error') || ui.item.data('dropped') ){
                            that.$('.js-content-list').sortable( 'cancel' );
                            return;
                        }
                        //var id = ui.item.attr('data-id'),
                            //contentType = Views.getElementContentType( ui.item ),
                            //cid = Views.getElementUniqueId( ui.item, contentType );

                        //var index = contentType ? ui.item.prevAll('.' + contentType).size() : 0;
                        //var movedModel = that.collection.findWhere({uniqueId: cid});
                        //
                        //if( movedModel ){
                        //    var parentId = that.model.get('id');
                        //    parentId = parentId ? parseInt( parentId ) : '';
                        //    contentManager.Entities.options.sortbyIndex = 0;
                        //    that.updateOrdering(movedModel, index, parentId, contentType);
                        //}

                    }
                })
                .disableSelection();
        }
    });

    Views.ContentLayout = Marionette.LayoutView.extend({
        tagName: 'div',
        template: '#contentManagerContentLayoutTemplate',
        className: 'min-height400 content-container div-row',
        initialize:function(){
            this.activeItemId = '';
            this.selectMode = this.options.selectMode;
        },
        regions:{
            'toolbar' : '#contentManagerToolbar',
            'content': '#categoryContentView',
            'preview': '#contentManagerContentPreview'
        },
        childEvents: {
            'content:list:activate:item': function(childView, model){
                if (this.activeItemId === model.get('uniqueId')) {

                    var mainRegionWidth = jQueryValamis('#contentManagerAppRegion').width();
                    if (mainRegionWidth < 768) { // mobile view
                        if (this.preview.currentView.$el.is(':visible'))
                            this.preview.currentView.$el.hide();
                        else
                            this.preview.show(new Views.PreviewLayout({model: model, parent: this.model}));
                    }
                }
                else{
                    this.preview.show(new Views.PreviewLayout({model: model, parent: this.model}));
                }
                this.activeItemId = model.get('uniqueId');
            },
            'content:list:clone:item': function(childView, model){
                contentManager.execute('content:clone', model, this.model);
            },
            'content:list:reload': function(childView, model) {
                this.toolbar.currentView.render();
            }
        },
        onRender: function() {
            var that = this;
            var toolbarView = new Views.ToolbarView({
                model: that.model,
                selectMode: that.selectMode
            });
            that.toolbar.show(toolbarView);

            var contentView = new Views.CategoryContentList({
                model : that.model,
                isSortable: !that.selectMode
            });

            that.content.show(contentView);
        }
    });

    Views.TopbarLayoutView = Marionette.ItemView.extend({
        ADD_TYPE: {
            RANDOM: 'random',
            DEFAULT: 'default'
        },
        template: '#contentManagerTopbarTemplate',
        className: 'div-table',
        templateHelpers: function() {
            return {
                randomValue: this.ADD_TYPE.RANDOM,
                defaultValue: this.ADD_TYPE.DEFAULT
            }
        },
        behaviors: {
            ValamisUIControls: {}
        },
        events: {
            'change input[name="addType"]': 'selectAddType',
            'keyup .js-random-amount': 'updateRandomAmount'
        },
        modelEvents: {
            'change:selectedQuestions': 'render'
        },
        selectAddType: function(e) {
            var isRandom = (jQueryValamis(e.target).val() == this.ADD_TYPE.RANDOM);
            this.model.set('isRandom', isRandom);
            this.$('.js-random-amount').attr('disabled', !isRandom);
        },
        updateRandomAmount: function() {
            this.model.set('randomQuestions', parseInt(this.$('.js-random-amount').val()));
        }
    });

    Views.AppLayoutView = Marionette.LayoutView.extend({
        tagName: 'div',
        template: '#contentManagerLayoutTemplate',
        regions:{
            'topbar': '#contentManagerTopbarView',
            'contents': '#contentManagerContentsView',
            'content': '#contentManagerContentView'
        },
        initialize: function(options) {
            this.showGlobalBase = options.showGlobalBase;
            this.selectMode = options.selectMode;
        },
        childEvents:{
            'contents:clean:active': function(childView){
                this.contents.currentView.$('li').removeClass('selected-entity');
            },
            'contents:activate:category': function(childView, category){

                var self = this;

                var contentLayout = new Views.ContentLayout({
                    model: category,
                    selectMode: self.selectMode
                });
                self.content.show(contentLayout);
                contentManager.execute('selected:questions:update');
            }
        },
        onRender: function() {

            var that = this;

            var mainRegionWidth = jQueryValamis('#contentManagerAppRegion').width();
            if (mainRegionWidth < 768) // mobile view
                this.$('.portlet-wrapper').addClass('sidebar-hidden');

            if(this.showGlobalBase) {
                var courses = new valamisApp.Entities.LiferaySiteCollection();
                courses.fetch({ reset: true });

                var courseList = new contentManager.TreeViews.CoursesView({
                    collection: courses
                });

                that.contents.show(courseList);

            }else{

                var courseId = Utils.getCourseId();
                var treeNode = new contentManager.Entities.TreeCategory({
                    title: Valamis.language['treeRootElement'],
                    courseId: courseId
                });

                contentManager.rootNodes[courseId] = treeNode;

                treeNode.fetchChildren({reset:true}).then(function(){
                    var contentsTree = new contentManager.TreeViews.ContentsTree({
                        model: treeNode
                    });
                    that.contents.show(contentsTree);
                    treeNode.updateContentAmount();
                    contentsTree.selectThis();
                    contentManager.openCategory();
                });

            }

            if (this.selectMode) {
                var topbarModel = new contentManager.Entities.TopbarModel();
                var topbarView = new contentManager.Views.TopbarLayoutView({
                    model: topbarModel
                });

                this.topbar.show(topbarView);
            }
        }
    });

    Views.getElementContentType = function( element ){
        var contentType = '';
        if(element.hasClass('category')){
            contentType = 'category';
        }else if(element.hasClass('question')){
            contentType = 'question';
        } else if (element.hasClass('plaintext')) {
            contentType = 'plaintext'
        }
        return contentType;
    };

    Views.getElementUniqueId = function( element, contentType ){
        var id = element.attr('data-id');
        if( !contentType ){
            contentType = Views.getElementContentType( element );
        }
        var cid = id;
        if(contentType == 'category'){
            cid = 'c_' + id;
        } else if(contentType == 'question'){
            cid = 'q_' + id;
        } else if (contentType == 'plaintext'){
            cid = 't_' + id;
        }
        return cid;
    };

});