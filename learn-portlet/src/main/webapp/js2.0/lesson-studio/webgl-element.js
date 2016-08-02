var WebglElementModule = slidesApp.module('WebglElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(WebglElementModule, slidesApp, Backbone, Marionette, $, _){

        WebglElementModule.View = this.BaseView.extend({
            template: '#webglElementTemplate',
            className: 'rj-element rj-webgl no-select',
            initialize: function() {
                this.constructor.__super__.initialize.apply(this, arguments);
                this.canvas
                    = this.threeJsRenderer
                    = this.scene
                    = this.camera
                    = this.trackballControls
                    = this.threeJsModel = null;
                this.on('resize:stop',function(){
                    if(this.canvas && this.threeJsRenderer && this.camera) {
                        this.canvas.width = this.model.get('width');
                        this.canvas.height = this.model.get('height');
                        this.threeJsRenderer.setSize(this.model.get('width'), this.model.get('height'));
                        this.camera.aspect = this.model.get('width') / this.model.get('height');
                        this.camera.updateProjectionMatrix();
                    }
                });
            },
            updateUrl: function(url, oldUrl) {
                if(url) {
                    var src = (url.indexOf('/') == -1)
                        ? slidesApp.getFileUrl(this.model, url)
                        : url;
                    this.initRenderer();
                    this.installModel(src);
                    this.content.css('background-color', 'transparent');
                    this.$('.content-icon-arrange').hide();
                    this.trigger('resize:stop');
                    slidesApp.viewId = this.cid;
                    slidesApp.actionType = 'itemContentChanged';
                    slidesApp.oldValue = {contentType: 'url', content: oldUrl};
                    slidesApp.newValue = {contentType: 'url', content: this.model.get('content')};
                    slidesApp.execute('action:push');
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
                this.trackballControls = new THREE.TrackballControls(this.camera, this.canvas);
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

                //Make model smooth
                this.threeJsModel.children[0].geometry.computeFaceNormals();
                this.threeJsModel.children[0].geometry.computeVertexNormals();
                this.threeJsModel.children[0].geometry.normalsNeedUpdate = true;

                this.renderScene(this);

                this.$('.glcanvas').show();
            },
            makeSmooth: function() {
                if (this.threeJsModel) {
                    this.threeJsModel.children[0].geometry.computeFaceNormals();
                    this.threeJsModel.children[0].geometry.computeVertexNormals();
                    this.threeJsModel.children[0].geometry.normalsNeedUpdate = true;
                    this.renderScene(this);
                }
            }
        });

        WebglElementModule.CreateModel = function() {
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
                layoutWidth = deviceLayoutCurrent.get('minWidth'),
                elementWidth = Math.min(layoutWidth, 500);
            var model = new WebglElementModule.Model( {
                'content': '',
                'slideEntityType': 'webgl',
                'width': elementWidth,
                'height': Math.round(elementWidth / (4/3))
            });
            return model;
        }
    }
});

WebglElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'WebGL', label: Valamis.language['3dmodelLabel'], slideEntityType: 'webgl'});
});