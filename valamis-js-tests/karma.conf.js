// Karma configuration
// Start testing:
// node_modules/karma/bin/karma start
module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: [ 'jasmine-jquery', 'jasmine-ajax', 'jasmine' ],

    plugins: [
      'karma-jasmine',
      'karma-jasmine-ajax',
      'karma-jasmine-jquery',
      'karma-phantomjs-launcher'
    ],

    // list of files / patterns to load in the browser
    files: [
        'node_modules/karma-jquery/jquery/jquery-2.1.0.js',
        '../learn-portlet/src/main/webapp/js/vendor/lodash.min.js',
        '../learn-portlet/src/main/webapp/js/vendor/backbone.js',
        '../learn-portlet/src/main/webapp/js/vendor/backbone.marionette.min.2.4.1.js',
        '../learn-portlet/src/main/webapp/js/vendor/backbone.service.js',
        '../learn-portlet/src/main/webapp/js/vendor/backbone.modal-bundled.1.1.5.js',
        '../learn-portlet/src/main/webapp/js/vendor/tincan-min.js',
        '../learn-portlet/src/main/webapp/js/helpers/*',
        '../learn-portlet/src/main/webapp/js/Urls.js',
        '../learn-portlet/src/main/webapp/js/common/valamisApp.js',
        '../learn-portlet/src/main/webapp/js/common/valamisModalViews.js',
        '../learn-portlet/src/main/webapp/js/common/valamisModels.js',
        '../learn-portlet/src/main/webapp/js/common/valamisViews.js',
        '../learn-portlet/src/main/webapp/js/common/marionette-utils.js',

        '../learn-portlet/src/main/webapp/js/lesson-studio/app.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/views.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/model-entities.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/entities.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/main.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/generic-item-module.js',
        '../learn-portlet/src/main/webapp/js/lesson-studio/*',
        'unit/*/*.js'
    ],

    // list of files to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: [ 'PhantomJS' ],//['Firefox'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity
  })
}
