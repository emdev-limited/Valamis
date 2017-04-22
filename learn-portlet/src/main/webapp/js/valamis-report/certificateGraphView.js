valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var GRAPH_MEASURE = {
        BASE_CIRCLE_RADIUS: 3,
        LEGEND_SQUARE_SIZE: 16,
        TOOLTIP_HEIGHT: 29
    };

    Views.CertificateGraphView = Marionette.ItemView.extend({
        template: false,
        className: 'val-portlet val-reports',
        initialize: function (options) {

            this.parseDate = function(isoString) { return new Date(isoString) };

            this.bisectDate = d3.bisector(function (d) {
                return d.date;
            }).left;

            var canvasWidth = parseInt(d3.select('#valamisReportAppRegion').style('width')),
                canvasHeight = parseInt(canvasWidth * 0.5);

            this.margin = {top: 30, right: 60, bottom: 100, left: 80};
            this.width = canvasWidth - this.margin.left - this.margin.right;
            this.height = canvasHeight - this.margin.top - this.margin.bottom;

            this.titleColorHash = [
                {key: 'countInProgress', title: 'legendJoinedLabel', color: '#F7AC1D'},
                {key: 'countAchieved', title: 'legendAchievedLabel', color: '#417504'}
            ];

            this.legendItemWidth = 90;
            this.legendPositionX = (this.width - (this.titleColorHash.length * (this.legendItemWidth + 30))) / 2;
            this.legendPositionY = this.height + 70;

            this.xScale = d3.time.scale().range([0, this.width]);
            this.yScale = d3.scale.linear().range([this.height, 0]);

        },

        onRender: function () {
            var that = this;

            this.model.getData({}, {
                data: {
                    startDate: this.options.startDate,
                    endDate: this.options.endDate,
                    reportScope: this.model.get('reportsScope'),
                    courseId: Utils.getCourseId(),
                    userIds: this.model.get('userIds')
                },
                success: function(responseData) {
                    that.showReport(responseData);
                },
                error: function(e) {
                    toastr.error(Valamis.language['overlayFailedMessageLabel']);
                    console.log(e);
                }
            });

        },

        showReport: function(responseData) {
            var that = this;
            that.triggerMethod('loading:finished');

            that.data = responseData.data;

            that.data.forEach(function (d) {
                d.date = that.parseDate(d.date);
            });

            that.countMax = that.data.map(function (d) {
                return Math.max(d.countAchieved,d.countInProgress,1);
            });

            that.xScale.domain(d3.extent(that.data, function (d) {
                return d.date;
            }));
            that.yScale.domain([0, d3.max(that.countMax)]).nice();

            that.ticksNum = {x: d3.min([7, that.data.length]), y: 5};

            that.xAxis = d3.svg.axis().scale(that.xScale)
                .orient('bottom')
                .ticks(that.ticksNum.x)
                .tickFormat(d3.time.format('%b %e'));

            that.yAxis = d3.svg.axis().scale(that.yScale)
                .orient('left')
                .ticks(that.ticksNum.y)
                .tickFormat(d3.format("d"));

            that.hBars = d3.svg.axis().scale(that.yScale)
                .orient('left')
                .ticks(that.ticksNum.y)
                .tickSize(-that.width, 0, 0)
                .tickFormat('');

            var lineGraphType = 'monotone';

            that.lineHash = {
                countInProgress: d3.svg.line()
                    .interpolate(lineGraphType)
                    .x(function (d) {
                        return that.xScale(d.date);
                    })
                    .y(function (d) {
                        return that.yScale(d.countInProgress);
                    }),
                countAchieved: d3.svg.line()
                    .interpolate(lineGraphType)
                    .x(function (d) {
                        return that.xScale(d.date);
                    })
                    .y(function (d) {
                        return that.yScale(d.countAchieved);
                    })
            };

            that.svg = d3.select(that.el)
                .append('svg')
                .attr('width', that.width + that.margin.left + that.margin.right)
                .attr('height', that.height + that.margin.top + that.margin.bottom)
                .append('g')
                .attr('transform', 'translate(' + that.margin.left + ',' + that.margin.top + ')');

            that.drawAxes();

            that.titleColorHash.forEach(function (hash) {
                that.drawLine(hash.key);
            });

            that.drawLegend();

            that.addMouseMove();
        },

        drawLine: function(dataType) {

            var chart = this.svg.append('g');
            var lineFunc = this.lineHash[dataType];
            var color = _.find(this.titleColorHash, function (hash) {
                return hash.key === dataType;
            }).color;

            chart.append('path')
                .style({stroke: color})
                .attr('d', lineFunc(this.data))
                .attr('class', 'chart-line report-value-' + dataType);

        },

        drawAxes: function() {

            this.svg.append('g')
                .attr('class', 'axis axis-x axis-time-period')
                .attr('transform', 'translate(0,' + this.height + ')')
                .call(this.xAxis)
                .selectAll('text')
                .attr('text-anchor', 'middle');

            this.svg.append('g')
                .attr('class', 'axis axis-y')
                .attr('transform', 'translate(0,0)')
                .call(this.yAxis);

            this.svg.append('g')
                .attr('class', 'bars-horizontal')
                .attr('transform', 'translate(0,0)')
                .call(this.hBars);

            this.svg.select('.axis-y .tick text').attr('opacity', 0);
        },

        drawLegend: function() {
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
                .attr('pointer-events', 'all')
                .attr('transform', 'translate(0, 0)')
                .attr('width', this.legendItemWidth)
                .attr('height', GRAPH_MEASURE.LEGEND_SQUARE_SIZE)
                .on('mouseover', function (d) {
                    that.svg.classed('emphasize-mode', true);
                    that.svg.selectAll('path.report-value-' + d.key).classed('emphasize', true);
                    that.svg.selectAll('.point-value.value-' + d.key).classed('emphasize', true);
                    that.svg.selectAll('.point-value.value-' + d.key+' circle').attr('r', GRAPH_MEASURE.BASE_CIRCLE_RADIUS + 1);
                    that.svg.selectAll('.legend-item.' + d.key).classed('emphasize', true);
                })
                .on('mouseout', function (d) {
                    that.svg.classed('emphasize-mode', false);
                    that.svg.selectAll('path.report-value-' + d.key).classed('emphasize', false);
                    that.svg.selectAll('.point-value.value-' + d.key).classed('emphasize', false);
                    that.svg.selectAll('.point-value.value-' + d.key+' circle').attr('r', GRAPH_MEASURE.BASE_CIRCLE_RADIUS);
                    that.svg.selectAll('.legend-item.' + d.key).classed('emphasize', false);
                });
        },

        addMouseMove: function() {
            var that = this;

            var interactionArea = this.svg.append('g')
                .attr('class', 'interaction-area')
                .style('display', 'none');

            // adding vertical bar
            interactionArea.append('line')
                .attr('class', 'interactive-vertical-bar')
                .attr({
                    'x1': 0,
                    'y1': 0,
                    'x2': 0,
                    'y2': this.height
                });

            // adding a group with a circle and tootip for each curve
            var interGroups = interactionArea.selectAll('g.point-value')
                .data(this.titleColorHash)
                .enter()
                .append('g')
                .attr('class', function (d) {
                    return 'point-value value-' + d.key;
                });

            interGroups.append('circle')
                .style('stroke', function (d) {
                    return d.color;
                })
                .attr('r', GRAPH_MEASURE.BASE_CIRCLE_RADIUS);

            interGroups.append('rect')
                .attr('rx', 3).attr('ry', 3)
                .classed('tooltip-bg', true);

            interGroups.append('rect')
                .attr('rx', 2).attr('ry', 2)
                .attr('fill', function (d) {
                    return d.color;
                })
                .classed('tooltip-icon', true);

            interGroups.append('text');

            this.currentVertBarPosition = -1;

            // adding the rectangle to capture mouse
            that.svg.append('rect')
                .attr('width', this.width)
                .attr('height', this.height)
                .style('fill', 'none')
                .style('pointer-events', 'all')
                .on('mouseover', function () {
                    interactionArea.style('display', null);
                })
                .on('mouseout', function () {
//                        interactionArea.style('display', 'none');
                })
                .on('mousemove', mousemove);

            function mousemove() {
                var x0 = that.xScale.invert(d3.mouse(this)[0]),
                    i = that.bisectDate(that.data, x0, 1),
                    d0 = that.data[i - 1],
                    d1 = that.data[i];
                var d = x0 - d0.date > d1.date - x0 ? d1 : d0;
                var x = that.xScale(d.date);

                if (x != that.currentVertBarPosition) {

                    that.currentVertBarPosition = x;

                    var sortedHash = _.sortBy(_.each(_.extend({}, that.titleColorHash), function (hash, index) {
                        _.extend(hash, {value: that.yScale(d[hash.key])});
                    }), function (hash) {
                        return hash.value;
                    });

                    var previousTooltipPosition = -(GRAPH_MEASURE.TOOLTIP_HEIGHT + 3);

                    sortedHash.forEach(function (hash) {

                        if (hash.value - previousTooltipPosition < (GRAPH_MEASURE.TOOLTIP_HEIGHT + 2)) {
                            tooltipShift = GRAPH_MEASURE.TOOLTIP_HEIGHT + 2 - (hash.value - previousTooltipPosition);
                        }
                        else {
                            tooltipShift = 0;
                        }

                        var key = hash.key;
                        var toolTipGroup = interactionArea.select('.point-value.value-' + key)
                            .attr('transform', 'translate(' + x + ',' + hash.value + ')');

                        var toolTipText = toolTipGroup.select('text')
                            .text(d[key])
                            .attr('transform', 'translate(34,' + (tooltipShift + 5) + ')');

                        var bbox = toolTipText.node().getBBox();

                        toolTipGroup.select('rect.tooltip-bg')
                            .attr('width', bbox.width + 36)
                            .attr('height', GRAPH_MEASURE.TOOLTIP_HEIGHT)
                            .attr('transform', 'translate(8,' + (tooltipShift - 14) + ')');

                        toolTipGroup.select('rect.tooltip-icon')
                            .attr('width', 12)
                            .attr('height', 12)
                            .attr('transform', 'translate(16,' + (tooltipShift - 5 ) + ')');

                        previousTooltipPosition = hash.value + tooltipShift;
                    });

                    interactionArea.select('.interactive-vertical-bar').attr('transform', 'translate(' + x + ',0)');
                }
            }
        }
    });

});