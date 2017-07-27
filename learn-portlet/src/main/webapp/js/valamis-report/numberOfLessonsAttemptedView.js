valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var GRAPH_MEASURE = {
        LEGEND_SQUARE_SIZE: 16
    };

    Views.numberOfLessonsAttemptedView = Marionette.LayoutView.extend({
        template: '#valamisAttemptedReportListTemplate',
        regions: {
            'reportRegion': '#reportRegion',
            'sortRegion': '#sortRegion',
            'coursesListRegion': '#reportCoursesList'
        },
        ui: {
            showMore: '.js-show-more'
        },
        events: {
            'click @ui.showMore': 'fetchMore'
        },
        initialize: function() {
            var that = this;
            valamisReport.takesUsers = 20;
            valamisReport.take = valamisReport.takesUsers;
            valamisReport.skip = 0;
            var courses = new valamisReport.Entities.CoursesCollection();
            courses.fetch().then( function() {
                var coursesListView = new valamisReport.Views.CoursesListView({
                    courses: courses.toJSON()
                });
                that.coursesListRegion.show(coursesListView);

                var sortView = new valamisReport.Views.SortListView();
                that.sortRegion.show(sortView);
            });

        },
        childEvents: {
            'valamisReport:course:changed': function(childView, filterByCourseId) {
                valamisReport.filterByCourseId = filterByCourseId;
                valamisReport.take = valamisReport.takesUsers;
                valamisReport.skip = 0;
                this.showReport();
            },
            'valamisReport:sort:changed': function(childView, sortBy) {
                if (valamisReport.sortById != sortBy) {
                    valamisReport.sortById = sortBy;
                    valamisReport.sortBy = valamisReport.sortById == 0 ? 'attempted' : 'completed';
                    if (valamisReport.skip > 0){
                        valamisReport.take += valamisReport.skip;
                        valamisReport.skip = 0;
                    }
                    this.showReport();
                }
            },
            'report:loading:finished': function (childView, hasResponseData) {
                this.triggerMethod('loading:finished');
                this.$('#emptyListRegion').toggleClass('hidden', hasResponseData);
            },
            'valamisReport:showMore:update': function (childView, state) {
                this.toggleShowMore(state);
            }
        },
        fetchMore: function() {
            var that = this;
            that.ui.showMore.addClass('loading');
            valamisReport.skip += valamisReport.take;
            valamisReport.take = valamisReport.takesUsers;
            this.model.getData({}, {
                data: {
                    courseId: Utils.getCourseId(),
                    filterByCourseId: valamisReport.filterByCourseId,
                    take: valamisReport.take,
                    skip: valamisReport.skip,
                    sortBy: valamisReport.sortBy
                },
                success: function(responseData) {
                    that.toggleShowMore(responseData.length < valamisReport.takesUsers);
                    that.reportView.updateReport(responseData);
                },
                error: function(e) {
                    toastr.error(Valamis.language['overlayFailedMessageLabel']);
                    console.log(e);
                }
            });
        },
        toggleShowMore: function(state) {
            this.ui.showMore.toggleClass('hidden', state);
            this.ui.showMore.removeClass('loading');
        },
        onRender: function() {
            this.showReport();
        },
        showReport: function() {
            this.reportView = new Views.numberOfLessonsAttemptedReportView({ model: this.model });
            this.reportRegion.show(this.reportView);
        }
    });

    Views.numberOfLessonsAttemptedReportView = Marionette.ItemView.extend({
        template: false,
        className: 'val-portlet val-reports',
        initialize: function (options) {

            this.parseDate = function(isoString) { return new Date(isoString) };

            this.bisectDate = d3.bisector(function (d) {
                return d.date;
            }).left;

            var canvasWidth = parseInt(d3.select('#valamisReportAppRegion').style('width')),
                canvasHeight = parseInt(canvasWidth * 0.5);

            this.margin = {top: 30, right: 60, bottom: 100, left: 170};
            this.width = canvasWidth - this.margin.left - this.margin.right;
            this.height = canvasHeight - this.margin.top - this.margin.bottom - 240;

            this.titleColorHash = [
                {key: 'countAttempted', title: 'legendAttemptedLabel', color: '#CCC'},
                {key: 'countCompleted', title: 'legendCompletedLabel', color: '#6A8E17'}
            ];

            if(valamisReport.filterByCourseId == undefined)
                valamisReport.filterByCourseId = Utils.getCourseId();
        },

        onRender: function () {
            var that = this;
            this.model.getData({}, {
                data: {
                    courseId: Utils.getCourseId(),
                    filterByCourseId: valamisReport.filterByCourseId,
                    take: valamisReport.take,
                    skip: valamisReport.skip,
                    sortBy: valamisReport.sortBy
                },
                success: function(responseData) {
                    that.triggerMethod('valamisReport:showMore:update', (responseData.length < valamisReport.takesUsers))
                    that.showReport(responseData);
                },
                error: function(e) {
                    toastr.error(Valamis.language['overlayFailedMessageLabel']);
                    console.log(e);
                }
            });

        },

        showReport: function (responseData) {
            var that = this;
            that.triggerMethod('report:loading:finished', !!responseData.length);
            if (!responseData.length){
                return;
            }

            that.data = responseData.sort(function(a, b) {
                var index = !!valamisReport.sortById ? valamisReport.sortById : 0,
                    val1 = !!a.categories[index] ? a.categories[index].value : 0,
                    val2 = !!b.categories[index] ? b.categories[index].value : 0;
                return val2 - val1;
            });

            that.height = Math.max(that.height, 40*that.data.length);
            that.xScale = d3.scale.linear().range([0, this.width]);
            that.yScale = d3.scale.ordinal().rangeRoundBands([0, this.height], .2);


            var y1 = d3.scale.ordinal();
            var color = d3.scale.ordinal().range(that.titleColorHash.map(function(val) {
                return val.color;
            }));

            that.yAxis = d3.svg.axis()
                .scale(that.yScale)
                .orient('left')
                .tickSize(0,0)
                .tickPadding(15);

            that.xAxis = d3.svg.axis()
                .scale(that.xScale)
                .orient('bottom')
                .tickSize(that.height)
                .tickFormat(d3.format('.f'))
                .outerTickSize(0);

            that.svg = d3.select(that.el)
                .append('svg')
                .attr('width', that.width + that.margin.left + that.margin.right)
                .attr('height', that.height + that.margin.top + that.margin.bottom);

            that.g = that.svg.append('g')
                .attr('transform', 'translate(' + that.margin.left + ',' + that.margin.top + ')');

            var categories = ['attempted','completed'];

            that.yScale.domain(that.data.map(function(d){ return d.name; }));
            y1.domain(categories).rangeRoundBands([0, that.yScale.rangeBand()], 0.2);
            that.xScale.domain([0, d3.max(that.data, function(user) {
                return d3.max(user.categories, function(d) { return d.value; })+10;
            })]);

            that.hLines = d3.svg.axis()
                .scale(that.yScale)
                .orient('left')
                .ticks(that.data.length)
                .tickSize(-that.width, 0)
                .tickFormat('');

            that.drawAxes();
            that.drawLegend();

            var users = that.g.selectAll('.user')
                .data(that.data, function(d){ return d.name; })
                .enter().append('g')
                .attr('class', 'user')
                .attr('transform', function(d) { return 'translate(0,' + that.yScale(d.name) + ')'; });

            var bars = users.selectAll('.bar')
                .data(function(user) { return user.categories; }, function(d){ return d.name; })
                .enter().append('rect')
                .attr('class', 'bar')
                .attr('height', y1.rangeBand())
                .attr('rx', 2)
                .attr('x', 0)
                .attr('y', function(d) { return y1(d.name); })
                .attr('width', function(d) { return that.xScale(d.value)})
                .attr("fill", function(d) { return color(d.name); });

            var bars_val =  users.selectAll('text')
                .data(function(user) { return user.categories; }, function(d){ return d.name; })
                .enter().append('text')
                .attr('x', function(d) { return that.xScale(d.value) +7;  })
                .attr('y', function(d) { return y1(d.name) +(y1.rangeBand()/2); })
                .attr('dy', '.35em')
                .text(function(d) { return d.value; });

            that.g.append('g')
                .attr('class', 'axis axis-y')
                .attr('transform', 'translate(0,0)')
                .call(that.yAxis)
                .selectAll('.tick text')
                .call(that.wrap, that.yScale.rangeBand());
        },

        wrap: function (text) {
            text.each(function () {
                var text = d3.select(this),
                    words = text.text().split(/\s+/).reverse(),
                    word,
                    line = [],
                    lineNumber = 0,
                    lineHeight = 1.1,
                    y = text.attr("y"),
                    dy = parseFloat(text.attr("dy")),
                    tspan = text.text(null).append("tspan").attr("x", -16).attr("y", y).attr("dy", dy + "em");
                while (word = words.pop()) {
                    line.push(word);
                    tspan.text(line.join(" "));
                    if (tspan.node().getComputedTextLength() > 145) {
                        line.pop();
                        tspan.text(line.join(" "));
                        line = [word];
                        tspan = text.append("tspan").attr("x", -16).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").text(word);
                    }
                }
            });
        },

        drawAxes: function() {
            this.g.append('g')
                .attr('class', 'bars-horizontal axis-x')
                .attr('transform', 'translate(0,0)')
                .call(this.xAxis);

            var step = (this.height/this.data.length/2)-1;
            this.g.append('g')
                .attr('class', 'bars-horizontal')
                .attr('transform', 'translate(1,' + step + ')')
                .call(this.hLines);
        },

        drawLegend: function() {
            this.legendItemWidth = 90;
            this.legendPositionX = (this.width - (this.titleColorHash.length * (this.legendItemWidth-90))) / 2;
            this.legendPositionY = this.height + 100;
            var that = this;

            var legend = this.svg.append('g')
                .attr('class', 'legend')
                .attr('transform', 'translate(' + this.legendPositionX + ',' + this.legendPositionY + ')');

            var legendItem = legend.selectAll('.legend')
                .data(this.titleColorHash)
                .enter().append('g')
                .attr('class', function (d) {
                    return 'legend-item ' + d.key;
                })
                .attr('transform', function (d, i) {
                    return 'translate(' + i * (that.legendItemWidth + 30) + ',0)';
                });

            legendItem.append('rect')
                .attr('width', GRAPH_MEASURE.LEGEND_SQUARE_SIZE)
                .attr('height', GRAPH_MEASURE.LEGEND_SQUARE_SIZE)
                .attr('rx', 2).attr('ry', 2)
                .style('fill', function (d) {
                    return d.color;
                });

            legendItem.append('text')
                .attr('x', GRAPH_MEASURE.LEGEND_SQUARE_SIZE + 7)
                .attr('y', GRAPH_MEASURE.LEGEND_SQUARE_SIZE)
                .attr('class', function (d) {
                    return d.key;
                })
                .attr('transform', 'translate(0,-4)')
                .text(function (d) {
                    return Valamis.language[d.title];
                });

            // Add rectangles to hover over for line highlighting
            legendItem.append('rect')
                .style('fill', 'none')
                .attr('class', 'legend-item-hover-area')
                .attr('transform', 'translate(0, 0)')
                .attr('width', this.legendItemWidth)
                .attr('height', GRAPH_MEASURE.LEGEND_SQUARE_SIZE);
        },

        updateReport: function(data) {
            $.merge(this.data, data);
            d3.select("svg").remove();
            this.showReport(this.data);
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

    Views.SortListView = Marionette.ItemView.extend({
        template: '#reportSortTemplate',
        events: {
            'click li': 'selectCategory'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        selectCategory: function(e) {
            this.triggerMethod('valamisReport:sort:changed', $(e.target).data('value'));
        },
    });
});