LiferayVideoModel = Backbone.Model.extend({
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

LiferayVideoGallery = Backbone.Collection.extend({
    model: LiferayVideoModel,
    parse: function (response) {
        this.trigger('videoCollection:updated', {
            total: response.total,
            currentPage: response.currentPage,
            listed: response.records.length
        });
        return response.records;
    }
}).extend(LiferayVideoService);

var VideoView = Backbone.View.extend({
    render: function () {
        var mustacheAccumulator = {};
        _.extend(mustacheAccumulator, this.model.toJSON(), Valamis.language);

        this.videoCollection = new LiferayVideoGallery();
        this.videoCollection.on('reset', this.renderVideoGallery, this);

        this.videoCollection.on("videoCollection:updated", function (details) {
            that.updatePagination(details, that);
        }, this);

        var that = this;
        this.paginator = new ValamisPaginator({el: this.$('#videoPaginator'), language: Valamis.language});
        this.paginator.setItemsPerPage(5);
        this.paginator.on('pageChanged', function () {
            that.videoCollection.fetch({
                reset: true,
                currentPage: that.paginator.currentPage(),
                itemsOnPage: that.paginator.itemsOnPage()
            });
        });

        this.videoCollection.fetch({reset: true, currentPage: 1, itemsOnPage: that.paginator.itemsOnPage()});
        return this;
    },
    updatePagination: function (details) {
        this.paginator.updateItems(details.total);
    },
    renderVideoGallery: function () {
        this.$('#dlvideo').html('');
        if( this.videoCollection.length == 0 ){
            var message = Mustache.to_html(
                jQueryValamis('#galleryItemEmptyTemplate').html(),
                { title: Valamis.language['pleaseUploadVideoLabel'] }
            );
            this.$('#dlvideo').append( message );
        } else {
            this.videoCollection.each(this.addVideo, this);
        }
    },
    addVideo: function (item) {
        var view = new VideoElement({model: item});
        view.on('unselectAll', this.unselectAll, this);
        this.$('#dlvideo').append(view.render().$el);
    },
    unselectAll: function () {
        this.videoCollection.each(function (i) {
            i.set({selected: false});
        }, this);
    },
    submit: function () {
        var selectedVideo = this.videoCollection.find(function (item) {
            return item.get('selected');
        });
        this.model.set({
            title: this.$('.js-title-edit').val() || this.model.get('lessonId') ? 'New video' : (selectedVideo) ? selectedVideo.get('title') : 'New video',
            mimeType: (selectedVideo) ? selectedVideo.get('mimeType') : '',
            uuid: (selectedVideo) ? selectedVideo.get('uuid') : '',
            groupID: (selectedVideo) ? selectedVideo.get('groupID') : '',
            fromDocLibrary: 'DL'
        });
        this.trigger('video:added', this.model);
    }
});

var VideoModal = Backbone.Modal.extend({
    template: function () {
        return Mustache.to_html(jQueryValamis('#view-video-template').html(),
            _.extend({header: Valamis.language['selectVideoLabel']}, Valamis.language))
    },
    submitEl: '.bbm-button',
    cancelEl: '.modal-close',
    className: 'val-modal',
    onRender: function () {
        this.view = new VideoView({
            model: this.model,
            el: this.$('.js-modal-content')
        });
        this.view.render();
        var self = this;
        this.view.on('video:added', function(model) { self.trigger('video:added', model) });
    },
    submit: function () {
        if (this.view)
            this.view.submit(this.model);
    }
});

var VideoElement = Backbone.View.extend({
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