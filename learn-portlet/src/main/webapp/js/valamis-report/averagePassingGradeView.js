valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var GRAPH_MEASURE = {
        MINIMUM_HEIGHT: 105,
        MEDIUM_HEIGHT: 160,
        MAXIMUM_HEIGHT: 210
    };

    Views.averagePassingGradeView = Marionette.LayoutView.extend({
        template: '#valamisAverageReportListTemplate',
        regions: {
            'averageGrades': '#averageGradesRegion',
            'coursesListRegion': '#valamisReportCoursesList'
        },
        initialize: function() {
            var that = this;
            var courses = new valamisReport.Entities.CoursesCollection();
            courses.fetch().then( function() {
                var coursesListView = new valamisReport.Views.CoursesListView({
                    courses: courses.toJSON()
                });
                that.coursesListRegion.show(coursesListView);
            });
        },
        childEvents: {
            'valamisReport:course:changed': function(childView, filterByCourseId) {
                valamisReport.filterByCourseId = filterByCourseId;
                this.showAveragePassingGrade();
            },
            'averageGrade:loading:finished': function (childView, hasResponseData) {
                this.triggerMethod('loading:finished');
                this.$('#emptyListRegion').toggleClass('hidden', hasResponseData);
            }
        },
        onRender: function() {
            this.showAveragePassingGrade();
        },
        showAveragePassingGrade: function() {
            var reportView = new Views.averagePassingGradeReportView({ model: this.model });
            this.averageGrades.show(reportView);
        }
    });

    Views.averagePassingGradeReportView = Marionette.ItemView.extend({
        template: false,
        className: 'val-portlet val-reports average-passing-grade-report',
        initialize: function () {

            var boxWidthMaximum = 900;
            var boxWidthMedium = 690;

            var boxWidth = Math.round(parseInt(d3.select('#valamisReportAppRegion').style('width')) * 0.9);
            var boxHeight = (function () {
                var height = GRAPH_MEASURE.MAXIMUM_HEIGHT;
                if (boxWidth > boxWidthMaximum) height = GRAPH_MEASURE.MINIMUM_HEIGHT;
                else if (boxWidth > boxWidthMedium) height = GRAPH_MEASURE.MEDIUM_HEIGHT;
                return height;
            })();

            this.width = boxWidth;
            this.height = boxHeight;

            if(valamisReport.filterByCourseId == undefined)
                valamisReport.filterByCourseId = Utils.getCourseId();
        },
        onRender: function () {

            var that = this;

            var result = {
                courseId: Utils.getCourseId(),
                filterByCourseId: valamisReport.filterByCourseId
            };

            this.model.getData({}, {
                data: result,
                success: function (responseData) {
                    that.showReport(responseData);
                },
                error: function (e) {
                    toastr.error(Valamis.language['overlayFailedMessageLabel']);
                    console.log(e);
                }
            });
        },

        showReport: function (responseData) {
            var that = this;
            that.triggerMethod('averageGrade:loading:finished', !!responseData);
            if (!responseData || !responseData.length) {
                return;
            }

            if (responseData.length) {
                var zippedData = [];
                var labels = [];
                responseData.forEach(function (item) {
                    labels.push(item.lessonTitle);
                    zippedData.push(Math.round(item.grade * 100));
                });

                var chartWidth = 475,
                    barHeight = 14,
                    gapBetweenGroups = 8,
                    spaceForLabels = chartWidth;

                var portletWIdth = this.$el.closest('.portlet').width();
                if (portletWIdth < 438) {
                    chartWidth = 150;
                    spaceForLabels = 250;
                }
                else if (portletWIdth < 1440) {
                    chartWidth = 290;
                    spaceForLabels = chartWidth;
                }


                var chartHeight = barHeight * zippedData.length + gapBetweenGroups * labels.length;

                var x = d3.scale.linear()
                    .domain([0, 100])
                    .range([0, chartWidth]);

                var y = d3.scale.linear()
                    .range([chartHeight + gapBetweenGroups, 0]);

                var yAxis = d3.svg.axis()
                    .scale(y)
                    .tickFormat('')
                    .tickSize(0)
                    .orient("left");

                // Specify the chart area and dimensions
                that.wrapperDiv = d3.select(that.el)
                    .html('')
                    .append('svg')
                    .attr('class', 'average-passing-grade')
                    .attr("width", spaceForLabels + chartWidth)
                    .attr("height", chartHeight);

                // Create bars
                var bar = that.wrapperDiv.selectAll("div")
                    .data(zippedData)
                    .enter().append("g")
                    .attr("transform", function (d, i) {
                        return "translate(" + spaceForLabels + "," + (i * barHeight + gapBetweenGroups * i) + ")";
                    });


                // Create rectangles of the correct width
                var zeroResultsColor = '#AAAAAA';
                var lowResultsColor = '#D25749';
                var middleResultsColor = '#F7AC1D';
                var highResultsColor = '#6A8E17';

                bar.append("path")
                    .attr("fill", function (d) {
                        if (d == 0) return zeroResultsColor;
                        else if (d <= 25) return lowResultsColor;
                        else if (d <= 75) return middleResultsColor;
                        else return highResultsColor;
                    })

                    .attr("class", function (d) {
                        if (d == 0) return "bar-zero";
                        else return "bar";
                    })
                    .attr("width", x)
                    .attr("height", barHeight)
                    .attr("d", function (d) {
                        var zeroResultsWidth = 14;
                        var width = (x(d) == 0) ? zeroResultsWidth : x(d);
                        return rightRoundedRect(0, 0, width, barHeight, 2);
                    });

                function rightRoundedRect(x, y, width, height, radius) {
                    return "M" + x + "," + y
                        + "h" + (width - radius)
                        + "a" + radius + "," + radius + " 0 0 1 " + radius + "," + radius
                        + "v" + (height - 2 * radius)
                        + "a" + radius + "," + radius + " 0 0 1 " + -radius + "," + radius
                        + "h" + (radius - width)
                        + "z";
                }

                // Add text label in bar
                bar.append("text")
                    .attr("class", "percent")
                    .attr("x", function (d) {
                        if (d == 0)
                            return x(d) + 8;
                        else
                            return x(d) - 6;
                    })
                    .attr("y", barHeight / 2)
                    .attr("dx", 2)
                    .attr("dy", ".35em")
                    .text(function (d) {
                        return d;
                    });

                // Draw labels
                var spaceAfterLabel = 16;
                var labelLength = spaceForLabels - spaceAfterLabel;

                bar.append("foreignObject")
                    .attr("class", "lesson-label")
                    .attr("x", - labelLength - spaceAfterLabel)
                    .attr("y", 0)
                    .attr("dy", ".35em")
                    .attr("width", labelLength)
                    .attr("height", '1.1em')

                    .html(function (d, i) {
                        var styles = 'width:'+ labelLength +'px;height:1.1em;';

                        return '<div style="'+ styles +'" class="ellipsis">' +
                            '<span class="js-lesson-label">'+ labels[i] + '</span></div>';

                    });

                var lessonLabels = that.$('.js-lesson-label');

                for (var item=0; item < lessonLabels.length; item++) {
                    if (lessonLabels[item].offsetWidth < spaceForLabels) {
                        lessonLabels[item].setAttribute('class', 'shift-right');
                    }
                }
            }
        }

    });

    Views.CoursesListView = Marionette.ItemView.extend({
        template: '#reportCoursesListTemplate',
        events: {
            'click li': 'selectCourse'
        },
        templateHelpers: function() {
            return { courses: this.options.courses }
        },
        behaviors: {
            ValamisUIControls: {}
        },
        onValamisControlsInit: function() {
            this.$('.js-courses-list').valamisDropDown('select', parseInt(Utils.getCourseId()));
        },
        selectCourse: function(e) {
            this.triggerMethod('valamisReport:course:changed', $(e.target).data('value'));
        }
    });
});