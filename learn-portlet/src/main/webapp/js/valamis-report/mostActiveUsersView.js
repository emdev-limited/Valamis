valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var GRAPH_MEASURE = {
        MINIMUM_SIZE: 200,
        MEDIUM_SIZE: 300,
        MAXIMUM_SIZE: 400
    };

    Views.mostActiveUsersView = Marionette.ItemView.extend({
        template: false,
        className: 'val-portlet val-reports most-active-users-report',
        initialize: function (options) {

            var boxWidth = Math.round(parseInt(d3.select('#valamisReportAppRegion').style('width')) * 0.9);
            var boxSize = (function() {
                var size = GRAPH_MEASURE.MINIMUM_SIZE;
                if (boxWidth > 300 ) size = GRAPH_MEASURE.MEDIUM_SIZE;
                    else if (boxWidth > 400 ) size = GRAPH_MEASURE.MAXIMUM_SIZE;
                return size;
            })();

            this.width = boxSize;
            this.height = boxSize;

        },
        onRender: function () {

            var that = this;

            this.model.getData({}, {
                data: {
                    startDate: this.options.startDate,
                    endDate: this.options.endDate,
                    courseId: Utils.getCourseId()
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
                function tooptipContent(d) {
                    if (d.children) return null;

                    var html = '',
                        userInfoAssets = ['Certificates','Lessons'];

                    html += '<div class="tooltip-header">';
                    html += '<div class="image" style="background-image: url(' + d.picture + ')"></div>';
                    html += '<strong>' + d.name + '</strong>';
                    html += '</div>';
                    html += '<ul>';
                    _.each(userInfoAssets, function(asset) {
                        if (d['count'+asset] > 0) {
                            html += '<li>';
                            html += Valamis.language['userInfo'+ asset + 'Label'];
                            html += '<strong>' + d['count'+asset] + '</strong>';
                            html += '</li>';
                        }
                    });
                    html += '</ul>';

                    return html;
                }

                _.each(responseData['data'], function(item) {
                    countAll += item.countCompleted;
                });

                responseData.activityValue = countAll;

                var color = d3.scale.category10();

                that.wrapperDiv = d3.select(that.el)
                    .append('div')
                    .attr('class', 'top-lessons-wrapper')
                    .style('width', that.width + 'px')
                    .style('height', that.height + 'px');

                var pack = d3.layout.pack()
                    .size([that.width, that.height])
                    .children(function(d){ return d.data; })
                    .value(function(d){ return d.activityValue; })
                    .padding(1)
                    .nodes(responseData);

                var cell = that.wrapperDiv.selectAll('div')
                    .data(pack)
                    .enter().append('div')
                    .attr('class', function(d) { return d.children ? 'box' : 'cell js-has-tooltip' })
                    .style('left', function(d) { return (d.x - d.r) + "px"; })
                    .style('top', function(d) { return (d.y - d.r) + "px"; })
                    .style('display', function(d) { return d.children ? 'none' : 'block' })
                    .style('width', function(d) { return Math.max(1, d.r * 2) + "px"; })
                    .style('height', function(d) { return Math.max(1, d.r * 2) + "px"; })
                    .style('background-color', function(d,i) { return d.children ? null : color(i); })
                    .style('background-image', function(d,i) {
                        return d.children ? null : 'url("' + d.picture + '")'
                    });

                cell.data(pack)
                    .append('i')
                    .attr('class', 'cell-tooltip')
                    .attr('data-toggle', function(d) { return d.children ? null : 'tooltip'; })
                    .attr('data-html', function(d) { return d.children ? null : 'true'; })
                    .attr('data-placement', function(d) { return d.children ? null : 'top'; })
                    .attr('data-animation', function(d) { return d.children ? null : 'false'; })
                    .attr('data-trigger', function(d) { return d.children ? null : 'manual'; })
                    .attr('title', function(d) { return tooptipContent(d)});


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

                var emptyListHtml = '<div class="no-users-msg"><h5 style="margin:0 5px">' + Valamis.language['noItemsLabel'] + '.</h5></div>';

                $(that.el).html(emptyListHtml);

            }
        }
    });

});