slidesApp.module('TinCanPackageGenericItem', {
    Model: baseLessonStudioModels.LessonPageElementModel,
    define: function(TinCanPackageGenericItem, slidesApp, Backbone, Marionette, $, _){
        // todo duplicate from Utils.js, not included in published lesson
        var mimeToExt = {
            'video': {
                'video/mp4': 'mp4',
                  'video/mpeg': 'mpeg',
                  'video/x-flv': 'flv',
                  'video/3gpp': '3gp',
                  'video/quicktime': 'mov',
                  'video/x-msvideo': 'avi',
                  'video/ogg': 'ogv',
                  'video/webm': 'webm'
            }
        };

        TinCanPackageGenericItem.startWithParent = false;

        TinCanPackageGenericItem.GenericItemView = Marionette.ItemView.extend({
            className: function(){
                var className = 'rj-element';
                switch(this.model.get('slideEntityType')) {
                    case 'text':
                        // todo fix it: do not apply text element (rj-text) styles to lesson summary
                        if (!_.contains(this.model.get('content'), '<div id="lesson-summary">'))
                            className += ' rj-text';
                        break;
                    case 'question':
                        className += ' question-element';
                        break;
                    case 'plaintext':
                        className += ' question-element';
                        break;
                }
                return className;
            },
            template: '#genericElementTemplate',
            templateHelpers: function() {
                // Default is an icon (if content is empty)
                var contentWrapper = '<div class="content-icon-' + this.model.get('slideEntityType') + '">' +
                    '<span class="val-icon-' + this.model.get('slideEntityType') + '"></span></div>';
                if(this.model.get('content')) {
                    switch (this.model.get('slideEntityType')) {
                        case 'text':
                            contentWrapper = this.model.get('content');
                            break;
                        case 'image':
                            contentWrapper = '<iframe width="100%" height="100%" style="display: none"></iframe>';
                            break;
                        case 'iframe':
                        case 'pdf':
                            contentWrapper = '<iframe class="slideset-iframe"></iframe>';
                            break;
                        case 'video':
                            contentWrapper = '<iframe width="100%" height="100%" style="display: none"></iframe>' +
                                '<video class="video-js vjs-default-skin" width="' + this.model.get('width') +
                                '" height="' + this.model.get('height') + '"><source src="" /></video>';
                            break;
                        case 'webgl':
                            contentWrapper = '<canvas class="glcanvas" width="' + this.model.get('width') +
                                '" height="' + this.model.get('height') + '" style="display: none; background-color:black;"></canvas>';
                            break;
                        case 'math':
                            contentWrapper = '<div class="math-content"></div>';
                            break;
                        case 'audio':
                            contentWrapper = '<audio src="" controls preload="none" style="width: 100%;"></audio>';
                            break;
                    }
                }
                return {
                    contentWrapper: contentWrapper
                }
            },
            initialize: function(options) {
                var view = this;
                this.constructor.__super__.initialize.apply(this, arguments);
                this.canvas
                    = this.threeJsRenderer
                    = this.scene
                    = this.camera
                    = this.trackballControls
                    = this.threeJsModel = null;

                this.model.on('change', function(){
                    view.updateEl();
                    if(this.get('slideEntityType') == 'webgl'){
                        view.trigger('resize:stop');
                    }
                });
                this.on('resize:stop',function(){
                    if(this.model.get('slideEntityType') == 'webgl' && this.canvas && this.threeJsRenderer && this.camera) {
                        this.canvas.width = this.model.get('width');
                        this.canvas.height = this.model.get('height');
                        this.threeJsRenderer.setSize(this.model.get('width'), this.model.get('height'));
                        this.camera.aspect = this.model.get('width') / this.model.get('height');
                        this.camera.updateProjectionMatrix();
                    }
                });
            },
            onRender: function() {
                this.content = this.$('.item-content');
                this.content.html(this.templateHelpers().contentWrapper);

                this.prepareEl();
                this.updateEl();
            },
            prepareEl: function(){
                if(this.model.get('content') === '' && this.model.get('slideEntityType') !== 'text')
                    this.content.css({'background': '#1C1C1C'});
                else {
                    switch (this.model.get('slideEntityType')) {
                        case 'text':
                            this.content.html(this.model.get('content'));
                            break;
                        case 'image':
                            var url = this.model.get('content');
                            if (url.indexOf('docs.google.com/file/d/') != -1) {
                                this.$('iframe').attr('src', url);
                                this.$('iframe').show();
                            }
                            else {
                                url = 'url("' + slidesApp.RevealModule.getFileURL(url, 'slide_item_' + this.model.get('id')) + '") no-repeat';
                                this.$('iframe').attr('src', '');
                                this.$('iframe').hide();
                                this.content.css('background-image', url);
                                this.content.css('background-color', 'transparent');
                                this.content.css({
                                    'background': url,
                                    'background-size': '100% 100%'
                                });
                                this.$('.content-icon-image').first().hide();
                            }
                            break;
                        case 'video':
                            var url = this.model.get('content');
                            var that = this;
                            if (url.indexOf('docs.google.com/file/d/') != -1) {
                                this.$('iframe').attr('src', url);
                                this.$('iframe').show();
                                this.$('.video-js').hide();
                            }
                            else if (url.indexOf('youtube.com/') != -1) {
                                this.videoId = /https?:\/\/(www\.)?youtube\.com\/embed\/([^&]*)/g.exec(url)[2];
                                this.$('iframe').attr('src', 'https://www.youtube.com/embed/' + this.videoId + '?enablejsapi=1');
                                this.playerInitAttemptCount = 0;
                                this.initPlayer(that);

                                this.$('iframe').show();
                            }
                            else {
                                this.$('iframe').hide();
                                this.$('.video-js').show();
                                this.$('video').attr('src', url);
                                this.$('video > source').attr('src', url);
                                this.$('video > source').attr('type', (_.invert(mimeToExt.video))[/&ext=([^&]*)/g.exec(url)[1]]);
                                this.$('video').load();
                                if (navigator.sayswho[0].toLowerCase() === 'firefox') {
                                    this.$('video').attr('controls', 'controls');
                                }
                                else
                                    this.$('video').on('loadeddata', function () {
                                        slidesApp.execute('item:focus', that);
                                        that.player = videojs(that.$('video')[0], {
                                            "controls": true,
                                            "autoplay": false,
                                            "preload": "auto"
                                        }, function () {
                                            // Player (this) is initialized and ready.
                                        });
                                        that.player.on('loadeddata', function () {
                                            that.player.play();
                                            that.player.pause();
                                        });
                                    });
                            }
                            this.content.css('background-color', 'transparent');
                            this.$('.content-icon-video').first().hide();

                            break;
                        case 'pdf':
                            var content = (this.model.get('content').indexOf('http') == 0)
                                ? this.model.get('content')
                                : slidesApp.RevealModule.getFileURL(
                                    this.model.get('content'),
                                    'slideData' + this.model.get('id'),
                                    this.model
                                );

                            this.content.find('iframe').attr({
                                'src': content
                            });
                            break;
                        case 'iframe':
                            var content = this.model.get('content');
                            if(content.indexOf('LTI:') == 0) {
                                this.content.find('iframe').attr('lti-src', content.replace('LTI:',''));
                                this.content.find('iframe').attr('name', 'iframe-'+this.model.id);
                            } else {
                                this.content.find('iframe').attr('src', content);
                            }
                            break;
                        case 'plaintext':
                        //break is not needed here as for now actions for question and plaintext are the same
                        case 'question':
                        case 'randomquestion':
                            var questionId;
                            var questionModel;
                            var template;
                            var isQuestion = false;
                            if (this.model.get('slideEntityType') === 'randomquestion') {
                                var id = this.getUniqueQuestion();
                                questionId = id.substring(2);// id format is q_*(questions) or t_*(plainText)
                                if (id.indexOf("q_") == -1)
                                    questionModel = slidesApp.randomPlainTextCollection.get(questionId);
                                else {
                                    isQuestion = true;
                                    questionModel = slidesApp.randomQuestionCollection.get(questionId);
                                }
                                slidesApp.questionsArray.push(id);
                                template = 'TemplateRandom';
                            }
                            else {
                                questionId = parseInt(this.model.get('content'));
                                template = 'Template';
                                if (this.model.get('slideEntityType') === 'plaintext') {
                                    questionModel = slidesApp.plaintextsCollection.get(questionId);
                                } else {
                                    isQuestion = true;
                                    questionModel = slidesApp.questionCollection.get(questionId);
                                }
                            }
                            this.$el.addClass('question-element');
                            var questionNumber = parseInt(this.model.get('id')),
                                questionType = questionModel.get('questionType'),
                                questionTypeString = (_.invert(QuestionType))[questionType];

                            // TODO: replace 'replace'
                            var questionTemplate = jQuery('#' + questionTypeString + template + questionId + '_' + questionNumber)
                                .html()
                                .replace(/placeholder(=")?/g, 'placeholder="' + translations.typeYourAnswerLabel + '"');
                            this.content.html(window.unescape(questionTemplate));
                            var data_state_prefix = questionType != 8 //plaintext element
                                ? questionTypeString.slice(0, questionTypeString.indexOf('Question')).toLowerCase()
                                : questionTypeString.toLowerCase();
                            var prefix = (data_state_prefix === 'shortanswer'
                                ? 'short'
                                : data_state_prefix);
                            var data_state = isQuestion
                                ? prefix + '_' + questionId + '_' + questionNumber
                                : prefix + '_' + questionId;

                            this.dataState = data_state;

                            if (isQuestion)
                                TinCanCourseQuestions[data_state] = TinCanCourseQuestionsAll[data_state];
                            if(this.model.get('slideEntityType') === 'plaintext'){
                                this.$('.js-valamis-question').addClass('plaintext-element');
                            }

                            if (this.model.get('fontSize')) {
                                // set font sizes for labels, input and textarea in question element
                                // otherwise these elements will have styles according aui and reveal styles
                                this.$('.answer-container > label, .answer-textarea, .question-short-answers')
                                    .css('font-size', this.model.get('fontSize'));
                            }
                            break;
                        case 'math':
                            katex.render(this.model.get('content'), this.content.find('.math-content')[0], {displayMode: true});
                            break;
                        case 'webgl':
                            var src = slidesApp.RevealModule.getFileURL(this.model.get('content'), 'slide_item_' + this.model.get('id'));
                            this.initRenderer();
                            this.installModel(src);
                            this.content.css('background-color', 'transparent');
                            this.$('.content-icon-arrange').hide();
                            break;
                        case 'audio':
                            var url = this.model.get('content');
                            if (url.indexOf('docs.google.com') != -1 || url.indexOf('/documents') != -1) {
                                this.$('audio').attr('src', url);
                            }
                            else {
                                url = slidesApp.RevealModule.getFileURL(url, 'slide_item_' + this.model.get('id'));
                                this.$('audio').attr('src', url);
                            }
                            this.content.css('background-color', 'transparent');
                            break;
                    }
                }
                this.content.find('.content-icon-' + this.model.get('slideEntityType')).first()
                    .css('font-size', Math.min(this.model.get('width') / 2, this.model.get('height') / 2) + 'px');
            },
            updateEl: function() {

                var elCss = {
                    'top': parseInt(this.model.get('top')),
                    'left': parseInt(this.model.get('left'))
                };

                elCss['width'] = parseInt(this.model.get('width'));
                elCss['height'] = parseInt(this.model.get('height'));

                this.$el.css(elCss);

                var itemContentCss = {
                    fontSize: this.model.get('fontSize') || ''
                };

                if( !!this.model.get('zIndex') ){
                    itemContentCss['zIndex'] = this.model.get('zIndex');
                }

                this.$('.item-content').css(itemContentCss);

                this.$el.toggleClass('hidden', !!this.model.get('classHidden'));
            },
            goToSlideActionInit: function() {
                var that = this;
                if( _.indexOf(['text','image'], this.model.get('slideEntityType')) > -1 && this.model.get('correctLinkedSlideId') ) {
                    this.$el.bind('click', {slideId: this.model.get('correctLinkedSlideId')}, that.goToSlideAction);
                }
            },
            goToSlideAction: function(e){
                if(e.data && e.data.slideId ) {
                    if( slidesApp.addedSlideIndices[e.data.slideId] ){
                        var slideIndices = slidesApp.addedSlideIndices[e.data.slideId];
                        Reveal.slide(slideIndices.h, slideIndices.v);
                    }
                }
            },
            // YouTube player methods
            initPlayer: function(that) {
                if (that.playerInitAttemptCount > 10)
                    console.warn('Failed to load YouTube Iframe API in 10 attempts. YouTube videos will not be available this time.');
                else {
                    if (window.youtubeIframeApiReady) {
                        that.YTPlayer = new YT.Player(that.$('iframe')[0], {
                            events: {
                                'onReady': function (evt) {
                                    that.onPlayerReady(evt);
                                },
                                'onStateChange': function (evt) {
                                    that.onPlayerStateChange(evt);
                                }
                            }
                        });
                        clearTimeout(that.initTimeout);
                    }
                    else {
                        that.playerInitAttemptCount++;
                        that.initTimeout = setTimeout(function() {
                            that.initPlayer(that)
                        }, 500);
                    }
                }
            },
            onPlayerReady: function(event) {
                var that = this;
                that.lastCheckedPlayerTime = 0;
                that.videoTitle = that.YTPlayer.getVideoData().title;
                that.videoDuration = that.YTPlayer.getVideoData().duration;
                // Check if the user has skipped part of a video
                that.playerCheckInterval = setInterval(function () {
                    if (Math.abs(that.lastCheckedPlayerTime - that.YTPlayer.getCurrentTime()) > 1)
                        that.onVideoSkipped(that, that.lastCheckedPlayerTime, that.YTPlayer.getCurrentTime());
                    that.lastCheckedPlayerTime = that.YTPlayer.getCurrentTime();
                }, 500);
                slidesApp.playerCheckIntervals.push(that.playerCheckInterval);
            },

            onPlayerStateChange: function(newState) {
                var that = this;
                switch (newState.data) {
                    case (YT.PlayerState.PLAYING):
                        that.onVideoPlayed(that, that.YTPlayer.getCurrentTime());
                        break;
                    case (YT.PlayerState.PAUSED):
                        if (that.lastPlayerState == YT.PlayerState.PLAYING) {
                            that.onVideoWatched(that, that.lastPlayerTime, that.YTPlayer.getCurrentTime())
                        }
                        that.onVideoPaused(that);
                        break;
                    case (YT.PlayerState.ENDED):
                        that.onVideoEnded(that);
                        break;
                    case (YT.PlayerState.UNSTARTED):
                        break;
                }
                that.lastPlayerTime = that.YTPlayer.getCurrentTime();
                that.lastPlayerState = newState.data;
            },
            onVideoPlayed: function(that, start) {
                console.log('Played ' + that.videoTitle);
                tincan.sendStatement(GetVideoStatement('played', that.videoId, that.videoTitle, that.videoDuration, toTimeString(start)));
            },
            onVideoPaused:function(that) {
                console.log('Paused ' + that.videoTitle);
                tincan.sendStatement(GetVideoStatement('paused', that.videoId, that.videoTitle, that.videoDuration));
            },
            onVideoWatched: function(that, start, finish) {
                console.log('Watched ' + that.videoTitle + ' from ' + start + ' to ' + finish);
                tincan.sendStatement(GetVideoStatement('watched', that.videoId, that.videoTitle, that.videoDuration, toTimeString(start), toTimeString(finish)));
            },
            onVideoSkipped: function(that, start, finish) {
                console.log('Skipped ' + that.videoTitle + ' from ' + start + ' to ' + finish);
                tincan.sendStatement(GetVideoStatement('skipped', that.videoId, that.videoTitle, that.videoDuration, toTimeString(start), toTimeString(finish)));
            },
            onVideoEnded: function(that) {
                console.log('Ended ' + videoTitle);
                tincan.sendStatement(GetVideoStatement('completed', that.videoId, that.videoTitle, that.videoDuration));
            },
            // Methods for WebGL
            bindTrackballControls: function () {
                var that = this;
                this.$el.addClass('inactive');
                this.undelegateEvents();
                this.goToSlideActionInit();
                if(this.model.get('slideEntityType') === 'webgl') {
                    this.$el.unbind('mouseover').bind('mouseover', function() {
                        that.trackballControls.enabled = true;
                    });
                    this.$el.unbind('mouseout').bind('mouseout', function() {
                        that.trackballControls.enabled = false;
                    });
                }
            },
            initRenderer: function() {
                try {
                    this.canvas = this.$(".glcanvas")[0];
                    this.threeJsRenderer = new THREE.WebGLRenderer({
                        canvas: this.canvas,
                        antialias: true
                    });
                }
                catch (e) {
                    console.error("<p><b>Sorry, an error occurred:<br>" +
                        e + "</b></p>");
                    return;
                }
                this.createWorld();
                this.installTrackballControls();
                this.animateScene(this);
                this.renderScene(this);  // Just gives a black background
            },
            createWorld: function() {
                this.scene = new THREE.Scene();
                this.camera = new THREE.PerspectiveCamera(50, parseInt(this.model.get('width')) / parseInt(this.model.get('height')), 0.1, 100);
                this.camera.position.z = 30;
                var light;  // A light shining from the direction of the camera; moves with the camera.
                light = new THREE.DirectionalLight();
                light.position.set(0, 0, 1);
                this.camera.add(light);
                this.scene.add(this.camera);
            },
            installTrackballControls: function() {
                this.trackballControls = new THREE.TrackballControls(this.camera);
                this.trackballControls.rotateSpeed = 3.0;
                this.trackballControls.zoomSpeed = 1.5;
                this.trackballControls.panSpeed = 0.4;
                this.trackballControls.noZoom = false;
                this.trackballControls.noPan = false;
                this.trackballControls.staticMoving = true;
                this.trackballControls.dynamicDampingFactor = 0.6;
                this.trackballControls.minDistance = 1;
                this.trackballControls.maxDistance = 100;
                this.trackballControls.keys = [82, 90, 80]; // [r:rotate, z:zoom, p:pan]
                this.trackballControls.enabled = false;
            },
            renderScene: function(context) {
                context.threeJsRenderer.render(context.scene, context.camera);
            },
            animateScene: function(context) {
                requestAnimationFrame(function() { context.animateScene(context); });
                if(this.trackballControls.enabled)
                    this.trackballControls.update();
                this.renderScene(context);
            },
            installModel: function(src) {
                var that = this;
                function callback(geometry, materials) {  // callback function to be executed when loading finishes.
                    that.onModelLoaded(geometry, materials);
                }

                if (this.threeJsModel) {
                    this.scene.remove(this.threeJsModel);
                }
                this.trackballControls.reset();  // return camera to original position.
                this.renderScene(that);  // draw without model while loading
                var loader = new THREE.JSONLoader();
                loader.load(src, callback);
            },
            onModelLoaded: function(geometry, materials) {
                var material, mesh;
                if ( materials !== undefined ) {
                    if ( materials.length > 1 ) {
                        material = new THREE.MeshFaceMaterial( materials );
                    } else {
                        material = materials[ 0 ];
                    }
                } else {
                    material = new THREE.MeshPhongMaterial();
                }
                if ( geometry.animation && geometry.animation.hierarchy ) {
                    mesh = new THREE.SkinnedMesh( geometry, material );
                } else {
                    mesh = new THREE.Mesh( geometry, material );
                }

                /* Determine the ranges of x, y, and z in the vertices of the geometry. */

                var xmin = Infinity;
                var xmax = -Infinity;
                var ymin = Infinity;
                var ymax = -Infinity;
                var zmin = Infinity;
                var zmax = -Infinity;
                for (var i = 0; i < geometry.vertices.length; i++) {
                    var v = geometry.vertices[i];
                    if (v.x < xmin)
                        xmin = v.x;
                    else if (v.x > xmax)
                        xmax = v.x;
                    if (v.y < ymin)
                        ymin = v.y;
                    else if (v.y > ymax)
                        ymax = v.y;
                    if (v.z < zmin)
                        zmin = v.z;
                    else if (v.z > zmax)
                        zmax = v.z;
                }

                /* translate the center of the object to the origin */
                var centerX = (xmin + xmax) / 2;
                var centerY = (ymin + ymax) / 2;
                var centerZ = (zmin + zmax) / 2;
                var max = Math.max(centerX - xmin, xmax - centerX);
                max = Math.max(max, Math.max(centerY - ymin, ymax - centerY));
                max = Math.max(max, Math.max(centerZ - zmin, zmax - centerZ));
                var scale = 10 / max;
                mesh.position.set(-centerX, -centerY, -centerZ);
                if (window.console) {
                    console.log("Loading finished, scaling object by " + scale);
                    console.log("Center at ( " + centerX + ", " + centerY + ", " + centerZ + " )");
                }

                /* Create the wrapper, model, to scale and rotate the object. */

                this.threeJsModel = new THREE.Object3D();
                this.threeJsModel.add(mesh);
                this.threeJsModel.scale.set(scale, scale, scale);
                this.scene.add(this.threeJsModel);
                this.renderScene(this);

                //Make model smooth
                this.threeJsModel.children[0].geometry.computeFaceNormals();
                this.threeJsModel.children[0].geometry.computeVertexNormals();
                this.threeJsModel.children[0].geometry.normalsNeedUpdate = true;

                this.$('.glcanvas').show();
            },
            makeSmooth: function() {
                if (this.threeJsModel) {
                    this.threeJsModel.children[0].geometry.computeFaceNormals();
                    this.threeJsModel.children[0].geometry.computeVertexNormals();
                    this.threeJsModel.children[0].geometry.normalsNeedUpdate = true;
                    this.renderScene(this);
                }
            },
            getUniqueQuestion: function(){
                var questionIds = this.model.get('content').split(",");
                var isAvailable = false;
                _.each(questionIds, function(id){
                    if (slidesApp.questionsArray.indexOf(id) == -1) isAvailable = true;
                });
                var questionId = questionIds[Math.floor(Math.random() * questionIds.length)];
                if (isAvailable) return this.checkQuestion(questionId, questionIds);
                else return questionId;
            },
            checkQuestion: function(id, ids){
                if (slidesApp.questionsArray.indexOf(id) >= 0){
                    var newId = ids[Math.floor(Math.random() * ids.length)];
                    return this.checkQuestion(newId, ids);
                }
                else return id;
            }
        });
    }
});
