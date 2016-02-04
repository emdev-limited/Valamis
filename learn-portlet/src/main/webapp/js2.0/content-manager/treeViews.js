/**
 * Created by igorborisov on 27.05.15.
 */

contentManager.module("TreeViews", function (TreeViews, ContentManager, Backbone, Marionette, $, _) {

    TreeViews.CourseItemView = Marionette.CompositeView.extend({
        template: '#contentManagerContentRootNodeTemplate',
        childViewContainer: '.tree-items',
        childView: TreeViews.ContentsTree
    });

    TreeViews.BaseTreeView = Marionette.CompositeView.extend({
        events: {
            'click .js-tree-item': 'selectNode',
            'click .js-tree-item-icon': 'toggleExpand'
        },
        toggleExpand: function () {
            this.collapsed = !this.collapsed;
            this.updateCollapsed();
        },
        updateCollapsed: function () {
            if (!this.collection.hasChildNodes()) {
                this.$el.removeClass('expanded').removeClass('collapsed');
                this.$('> .js-tree-item > .js-tree-item-icon').removeClass('val-icon-arrow-down-two').removeClass('val-icon-arrow-right-two');
            } else if (this.collapsed) {
                this.$el.removeClass('expanded').addClass('collapsed');
                this.$('> .js-tree-item > .js-tree-item-icon').removeClass('val-icon-arrow-down-two').addClass('val-icon-arrow-right-two');
            } else {
                this.$el.removeClass('collapsed').addClass('expanded');
                this.$('> .js-tree-item > .js-tree-item-icon').removeClass('val-icon-arrow-right-two').addClass('val-icon-arrow-down-two');
            }
        },
        loadChildren: function () {
            var that = this;
            this.model.fetchChildren().then(function () {
                that.collapsed = true;
                that.childrenFetched = true;
                //TODO try to avoid this
                that.render();
            });
        },
        addChild: function (child, ChildView, index) {
            var that = this;
            if (child.isNode()) {
                Marionette.CompositeView.prototype.addChild.apply(this, arguments);
                that.updateCollapsed();
            }
        }
    });

    TreeViews.TreeView = TreeViews.BaseTreeView.extend({
        template: '#contentManagerContentNodeTemplate',
        tagName: 'li',
        className: 'category collapsed',
        childView: TreeViews.TreeView,
        childViewContainer: '.tree-items',
        childEvents: {
            'select:node': function (childView, model) {
                this.triggerMethod('select:node', model);
            }
        },
        initialize: function () {
            this.collapsed = true;
            this.childrenFetched = true;
            this.collection = this.model.nodes;
        },
        modelEvents: {
            'change title': function () {
                this.$('> .js-tree-item > .js-tree-item-title').text(this.model.get('title'));
            },
            'change childrenAmount': function (arg) {
                if( this.model.get('childrenAmount') > 0 ){
                    this.$('> .js-tree-item > .js-tree-item-title').attr('data-count', this.model.get('childrenAmount'));
                }
                else {
                    this.$('> .js-tree-item > .js-tree-item-title').removeAttr('data-count');
                }
            }
        },
        onRender: function () {
            if (this.childrenFetched) {
                if (this.collection.length > 0) {
                    this.updateCollapsed();
                }
            }
            this.$el.attr('data-id', this.model ? this.model.get('id') : '');
            TreeViews.droppableInit(this.$el.children('.js-tree-item'));
        },
        templateHelpers: function () {
            return {
                needShowChildrenAmount: this.model.get('childrenAmount') > 0
            }
        },
        selectNode: function (e) {
            if( e ) e.stopPropagation();
            this.triggerMethod('contents:clean:active');
            this.triggerMethod('contents:activate:category', this.model);

            jQueryValamis('.tree-active-branch').removeClass('tree-active-branch');
            this.$el.addClass('selected-entity');
            this.selectParent(this.$el);

            if (!this.childrenFetched) {
                this.loadChildren();
            }
            ContentManager.options.currentCategory = this.model.get('id');
            localStorage.setItem('ValamisCMCategory',ContentManager.options.currentCategory);
            this.model.nodes.sort()
        },
        selectParent: function ($el) {
            if($el.hasClass('js-root'))
                return;

            $el = $el.parent().prev(); //Select previous span
            $el.addClass('tree-active-branch');
            this.selectParent($el.parent());
        },
        findByModelId: function(model_id){
            return _.find(this.children._views,function(view){
                return model_id === view.model.get('id');
            });
        }
    });

    TreeViews.ContentsTree = Marionette.CompositeView.extend({
        template: '#contentManagerContentRootNodeTemplate',
        childView: TreeViews.TreeView,
        childViewContainer: ".tree-items",
        events: {
            'click .js-root': 'selectThis',
            'click .js-tree-item-icon': 'toggleExpand'
        },
        modelEvents: {
            'change childrenAmount': function () {
                this.$('.js-root > .js-tree-item > .js-tree-item-title').attr('data-count', this.model.get('childrenAmount'));
            }
        },
        toggleExpand: function () {
            this.collapsed = !this.collapsed;
            this.updateCollapsed();
        },
        updateCollapsed: function () {
            if (!this.collection.hasChildNodes()) {
                this.$el.removeClass('expanded').removeClass('collapsed');
                this.$('> .js-tree-item > .js-tree-item-icon').removeClass('val-icon-arrow-down-two').removeClass('val-icon-arrow-right-two');
            } else if (this.collapsed) {
                this.$('> ul > li').removeClass('expanded').addClass('collapsed');
                this.$('> ul > li > .js-tree-item > .js-tree-item-icon').first().removeClass('val-icon-arrow-down-two').addClass('val-icon-arrow-right-two');
            } else {
                this.$('> ul > li').removeClass('collapsed').addClass('expanded');
                this.$('> ul > li > .js-tree-item > .js-tree-item-icon').first().removeClass('val-icon-arrow-right-two').addClass('val-icon-arrow-down-two');
            }
        },
        childEvents: {
            'select:node': function (childView, model) {
                this.selectNode(model);
            }
        },
        addChild: function (child, ChildView, index) {
            if (child.isNode()) {
                Marionette.CompositeView.prototype.addChild.apply(this, arguments);
            }
        },
        initialize: function () {
            this.collapsed = true;
            this.childrenFetched = false;
            if (this.model.nodes.length > 0) {
                this.collapsed = false;
                this.childrenFetched = true;
            }
            this.collection = this.model.nodes;
        },
        onRender: function () {
            //this.sortable();
            this.updateCollapsed();
            TreeViews.droppableInit(this.$('span.js-tree-root-item'));
        },
        selectThis: function () {
            this.triggerMethod('contents:clean:active');
            this.selectNode(this.model);
            this.$('li.js-root').addClass('selected-entity');
        },
        selectNode: function (model) {
            var that = this;
            model = model || that.model;
            if (that.childrenFetched) {
                that.triggerMethod('contents:activate:category', model);
            } else {
                that.model.fetchChildren().then(function () {
                    that.collapsed = true;
                    that.childrenFetched = true;
                    //TODO try to avoid this
                    that.render();
                    that.triggerMethod('contents:activate:category', model);
                }, function () {
                    that.childrenFetched = true;
                    that.triggerMethod('contents:activate:category', model);
                });
            }
            ContentManager.options.currentCategory = '';
            localStorage.setItem('ValamisCMCategory','');
            that.model.nodes.sort()
        },
        updateSorting: function (movedModel, index, parentId) {
            contentManager.execute('move:content:item', movedModel, index, parentId, 'category');
        },
        loadChildren: function () {
            var that = this;
            this.model.fetchChildren().then(function () {
                that.collapsed = true;
                that.childrenFetched = true;
                //TODO try to avoid this
                that.render();
            });
        },

        //TODO content sortable
        sortable: function () {
            var that = this;

            this.$('ul.tree-items').nestedSortable({
                disabled: true,
                handle: '.js-tree-item',
                items: 'li',
                toleranceElement: '> span',
                listType: 'ul',
                tabSize: 50,
                isTree: true,
                doNotClear: true,
                expandedClass: 'expanded',
                collapsedClass: 'collapsed',
                expandOnHover: 800,
                disableNestingClass: 'question',
                connectWith: '.ui-sortable',
                forcePlaceholderSize: true,
                placeholder: 'ui-state-highlight',
                relocate: function (e, ui) {
                    var $uiItem = jQueryValamis(ui.item);
                    var id = $uiItem.attr('data-id');
                    var index = $uiItem.prevAll('.category').length + 1;

                    var rawParentId = $uiItem.parents('li').first().attr('data-id');
                    var parentId = parseInt(rawParentId) || '';

                    var movedModel = that.model.getChildNode(id);

                    that.updateSorting(movedModel, index, parentId);
                },
                isAllowed: function (next, parent, current) {
                    return true;
                }
            }).disableSelection();

        },
        findByModelId: function(model_id){
            return _.find(this.children._views,function(view){
                return model_id === view.model.get('id');
            });
        }
    });

    TreeViews.CoursesView = Marionette.CollectionView.extend({
        template: '#contentManagerContentRootNodeTemplate',
        childViewContainer: '.tree-items',
        childView: TreeViews.ContentsTree,
        addChild: function (child, ChildView, index) {

            var courseId = child.get('id');
            var node = new contentManager.Entities.TreeCategory({
                title: child.get('title'),
                id: '',
                courseId: courseId
            });
            node.updateContentAmount();

            arguments[0] = node;
            contentManager.rootNodes[courseId] = node;

            Marionette.CollectionView.prototype.addChild.apply(this, arguments);
        }
    });

    TreeViews.getTreeIds = function( parent_id ){
        var ids = [ parent_id ];
        var contentsTreeRegion = contentManager.mainRegion.currentView.regionManager.get('contents');
        var treeCategoryEl = contentsTreeRegion.currentView.$('.category[data-id="' + parent_id + '"]');
        treeCategoryEl.parents('.category').each(function(){
            ids.push( jQueryValamis(this).data('id') );
        });
        return ids;
    };

    TreeViews.droppableInit = function(elements){
        elements
            .droppable({
                greedy: true,
                accept: '.lesson-item-li',
                hoverClass: 'ui-state-selected',
                tolerance: 'pointer',
                drop: function(e, ui){
                    ui.draggable.data('dropped',true);
                    var id = ui.draggable.data('id'),
                        target = $(e.target).parent('li'),
                        parentId = target.data('id');

                    var contentRegion = contentManager.mainRegion.currentView.regionManager.get('content'),
                        contentListRegion = contentRegion.currentView.regionManager.get('content'),
                        nodesCollection = contentListRegion.currentView.model.nodes;

                    if( !nodesCollection ){
                        return;
                    }
                    var contentType = contentManager.Views.getElementContentType( ui.draggable ),
                        cid = contentManager.Views.getElementUniqueId( ui.draggable, contentType),
                        movedModel = nodesCollection.findWhere({uniqueId: cid});

                    if( movedModel ){
                        contentManager.execute('move:content:item', movedModel, 1, parentId);
                    }
                }
            });
    };

});
