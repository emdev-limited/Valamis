valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var GRAPH_MEASURE = {
        MINIMUM_HEIGHT: 105,
        MEDIUM_HEIGHT: 160,
        MAXIMUM_HEIGHT: 210
    };

    Views.topLessonsView = Marionette.ItemView.extend({
        template: '#valamisReportEmptyListTemplate',
        className: 'val-portlet val-reports top-lessons-report',
        initialize: function (options) {

            var boxWidth = Math.round(parseInt(d3.select('#valamisReportAppRegion').style('width')) * 0.9);
            var boxHeight = (function() {
                var height = GRAPH_MEASURE.MAXIMUM_HEIGHT;
                if (boxWidth > 900 ) height = GRAPH_MEASURE.MINIMUM_HEIGHT;
                    else if (boxWidth > 690 ) height = GRAPH_MEASURE.MEDIUM_HEIGHT;
                return height;
            })();

            this.width = boxWidth;
            this.height = boxHeight;

        },
        onRender: function () {

            var that = this,
                userIds = this.model.get('userIds');

            this.model.getData({}, {
                data: {
                    startDate: this.options.startDate,
                    endDate: this.options.endDate,
                    reportsScope: this.model.get('reportsScope'),
                    courseId: Utils.getCourseId(),
                    userIds: userIds
                },
                success: function (responseData) {
                    that.showReport(responseData);
                },
                error: function (e) {
                    toastr.error(Valamis.language['overlayFailedMessageLabel']);
                    console.log(e);
                }
            });
        },

        showReport: function(responseData) {
            var that = this;
            that.triggerMethod('loading:finished');

            var countAll = 0;

            if (responseData['data'].length) {

                var image_url_prefix = '/delegate/packages/',
                    image_url_postfix = '/logo?courseId=' + Utils.getCourseId();

                _.each(responseData['data'], function(item) {
                    countAll += item.countCompleted;
                });

                if (countAll > 0) {

                    responseData.percentCompleted = 100;
                    responseData.countCompleted = countAll;

                    _.each(responseData['data'], function(item) {
                        item.percentCompleted = Math.round(item.countCompleted/countAll * 100);
                    });

                    var color = d3.scale.category10();

                    that.wrapperDiv = d3.select(that.el)
                        .html('')
                        .append('div')
                        .attr('class', 'top-lessons-wrapper')
                        .style('width', that.width + 'px')
                        .style('height', that.height + 'px');

                    var treemap = d3.layout.treemap()
                        .size([that.width, that.height])
                        .children(function(d){ return d.data; })
                        .sort(function(a,b) {
                            return a.value - b.value;
                        })
                        .value(function(d){ return d.percentCompleted; })
                        .nodes(responseData);

                    var cell = that.wrapperDiv.selectAll('div')
                        .data(treemap)
                        .enter().append('div')
                        .filter(function(d) { return d.percentCompleted > 0 })
                        .attr('class', function(d) { return d.children ? 'box' : 'cell js-has-tooltip' })
                        .style('left', function(d) { return d.x + "px"; })
                        .style('top', function(d) { return d.y + "px"; })
                        .style('display', function(d) { return d.children ? 'none' : null })
                        .style('width', function(d) { return Math.max(1, d.dx - 2) + "px"; })
                        .style('height', function(d) { return Math.max(1, d.dy - 2) + "px"; })
                        .style('background-color', function(d,i) { return d.children ? null : color(i); })
                        .style('background-image', function(d,i) {
                            return (d.children || !d.logo) ? null : 'url("' + image_url_prefix + d.id + image_url_postfix +  '")'
                        });

                    cell.data(treemap)
                        .append('div')
                        .attr('class', 'cell-content')
                        .append('span')
                        .attr('class', 'cell-label ellipsis')
                        .text(function(d) { return d.children ? null : d.title });

                    cell.data(treemap)
                        .select('div.cell-content')
                        .append('span')
                        .attr('class', 'cell-label ellipsis')
                        .text(function(d) { return d.children ? null : d.value + '%'; });

                    cell.data(treemap)
                        .append('i')
                        .attr('class', 'cell-tooltip')
                        .attr('data-toggle', function(d) { return d.children ? null : 'tooltip'; })
                        .attr('data-html', function(d) { return d.children ? null : 'true'; })
                        .attr('data-placement', function(d) { return d.children ? null : 'top'; })
                        .attr('data-animation', function(d) { return d.children ? null : 'false'; })
                        .attr('data-trigger', function(d) { return d.children ? null : 'manual'; })
                        .attr('title', function(d) {
                            return d.children ? null : d.title + '<br/><strong>' + d.countCompleted + ' completed</strong>';
                        });

                    that.$('.js-has-tooltip').mousemove( function(e) {
                        cellTooltip = $('.cell-tooltip', this);

                        var offset = $(this).offset();

                        var iPositionX = e.pageX - offset.left - 2;
                        var iPositionY = e.pageY - offset.top - 2;

                        cellTooltip.css({ left: iPositionX, top: iPositionY });
                        cellTooltip.tooltip('show');
                    });

                    that.$('.js-has-tooltip').mouseout( function(e) {
                        $('[data-toggle ="tooltip"]', $(this)).tooltip('hide');
                    })

                } else {
                    that.$('.js-no-finished-label').removeClass('hidden');
                }
            } else {
                that.$('.js-no-items-label').removeClass('hidden');
            }

        }

    });

});