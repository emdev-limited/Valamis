MediaGalleryModel = Backbone.Model.extend({
    defaults: {
        title: '',
        version: '',
        mimeType: '',
        selected: false
    }
});

LiferayVideoService = new Backbone.Service({
    url: path.root,
    sync: {
        'read': {
            'path': path.api.liferay + "video/",
            data: function (collection, options) {
                return {
                    courseId: Utils.getCourseId(),
                    page: options.currentPage,
                    count: options.itemsOnPage
                }
            },
            'method': 'get'
        }
    }
});

LiferayAudioService = new Backbone.Service({
    url: path.root,
    sync: {
        'read': {
            'path': path.api.liferay + "audio/",
            data: function (collection, options) {
                return {
                    courseId: Utils.getCourseId(),
                    page: options.currentPage,
                    count: options.itemsOnPage
                }
            },
            'method': 'get'
        }
    }
});

LiferayVideoGallery = Backbone.Collection.extend({
    model: MediaGalleryModel,
    parse: function (response) {
        this.trigger('mediaCollection:updated', {
            total: response.total,
            currentPage: response.currentPage,
            listed: response.records.length
        });
        return response.records;
    }
}).extend(LiferayVideoService);

LiferayAudioGallery = Backbone.Collection.extend({
    model: MediaGalleryModel,
    parse: function (response) {
        this.trigger('mediaCollection:updated', {
            total: response.total,
            listed: response.records.length
        });
        return response.records;
    }
}).extend(LiferayAudioService);

var MediaGalleryView = Backbone.View.extend({
    initialize: function(options){
        this.type = options.type;
        this.title = options.title;
    },
    render: function () {
        var mustacheAccumulator = {};
        _.extend(mustacheAccumulator, this.model.toJSON(), Valamis.language);

        if (this.type == 'video') {
            this.collection = new LiferayVideoGallery();
        }
        else {
            this.collection = new LiferayAudioGallery();
        }

        this.collection.on('reset', this.renderVideoGallery, this);

        this.collection.on("mediaCollection:updated", function (details) {
            that.updatePagination(details, that);
        }, this);

        var that = this;
        this.paginator = new ValamisPaginator({el: this.$('#videoPaginator'), language: Valamis.language});
        this.paginator.setItemsPerPage(5);
        this.paginator.on('pageChanged', function () {
            that.collection.fetch({
                reset: true,
                currentPage: that.paginator.currentPage(),
                itemsOnPage: that.paginator.itemsOnPage()
            });
        });

        this.collection.fetch({reset: true, currentPage: 1, itemsOnPage: that.paginator.itemsOnPage()});
        return this;
    },
    updatePagination: function (details) {
        this.paginator.updateItems(details.total);
    },
    renderVideoGallery: function () {
        this.$('#dlvideo').html('');
        if( this.collection.length == 0 ){
            var message = Mustache.to_html(
                jQueryValamis('#galleryItemEmptyTemplate').html(),
                { title: this.title }
            );
            this.$('#dlvideo').append( message );
        } else {
            this.collection.each(this.addVideo, this);
        }
    },
    addVideo: function (item) {
        var view = new MediaGalleryElement({model: item});
        view.on('unselectAll', this.unselectAll, this);
        this.$('#dlvideo').append(view.render().$el);
    },
    unselectAll: function () {
        this.collection.each(function (i) {
            i.set({selected: false});
        }, this);
    },
    submit: function () {
        var selectedMedia = this.collection.find(function (item) {
            return item.get('selected');
        });

        this.model.set({
            title: this.$('.js-title-edit').val() || this.model.get('lessonId') ? 'New '+ this.type : (selectedMedia) ? selectedMedia.get('title') : 'New '+ this.type,
            mimeType: (selectedMedia) ? selectedMedia.get('mimeType') : '',
            uuid: (selectedMedia) ? selectedMedia.get('uuid') : '',
            groupID: (selectedMedia) ? selectedMedia.get('groupID') : '',
            fromDocLibrary: 'DL'
        });
        this.trigger('media:added', this.model);
    }
});

var MediaGalleryModal = Backbone.Modal.extend({
    submitEl: '.bbm-button',
    cancelEl: '.modal-close',
    className: 'val-modal',
    initialize: function(options){
        if(options.onDestroy) this.onDestroy = options.onDestroy;
        this.type = options.type;
        this.header = options.header;
        this.title = options.emptyText;
        var mustacheAccumulator = {};
        _.extend(mustacheAccumulator,  Valamis.language);
        _.extend(mustacheAccumulator, { header : this.header });
        this.template = _.template(Mustache.to_html(jQueryValamis('#view-video-template').html(), mustacheAccumulator));
    },
    onRender: function () {
        this.view = new MediaGalleryView({
            model: this.model,
            type: this.type,
            title: this.title,
            el: this.$('.js-modal-content')
        });
        this.view.render();
        var self = this;
        this.view.on('media:added', function(model) { self.trigger('media:added', model) });
    },
    submit: function () {
        if (this.view)
            this.view.submit(this.model);
    }
});

var MediaGalleryElement = Backbone.View.extend({
    events: {
        'click': 'toggleThis',
        'click .js-toggleButton': 'toggleThis'
    },
    tagName: 'tr',
    initialize: function () {
        this.model.on('change', this.render, this);
    },
    render: function () {
        var template = Mustache.to_html(jQueryValamis('#videoTemplate').html(), this.model.toJSON());
        this.$el.toggleClass('selected',this.model.get('selected'));
        this.$el.html(template);
        return this;
    },
    toggleThis: function (e) {
        e.preventDefault();
        e.stopPropagation();
        var selected = !this.model.get('selected');
        this.trigger('unselectAll', this);
        this.model.set({selected: selected});
    }
});