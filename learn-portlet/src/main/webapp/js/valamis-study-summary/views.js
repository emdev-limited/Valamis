valamisStudySummary.module('Views', function (Views, valamisStudySummary, Backbone, Marionette, $, _) {

    function getColor(index) {
        switch (index) {
            case 0:
                return '#112d3e';
            case 1:
                return '#1d5073';
            case 2:
                return '#2974a6';
            case 3:
                return '#4ba6d7';
            default:
                return '#bfbfbf';
        }
    }

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#valamisStudySummaryLayoutTemplate',
        templateHelpers: function () {
            var hideStatistic = this.options.hideStatistic;
            return {
                welcomeText: Valamis.language[(hideStatistic)
                    ? 'teacherWelcomeLabel' : 'studentWelcomeLabel'],
                welcomeDescriptionText: Valamis.language[(hideStatistic)
                    ? 'teacherWelcomeDescriptionLabel' : 'studentWelcomeDescriptionLabel'],
                userModel: this.options.userModel
            }
        },
        ui: {
            toggleButton: '.js-stat-toggle',
            tooltip: '.js-tooltip',
            statistic: '.js-statistic',
            lessonsCompleted: '.js-lesson-completed',
            pathsCompleted: '.js-certificates-achieved',
            goalsAchieved: '.js-learning-goals-achieved',
            pathsInProgress: '.js-certificates-in-progress',
            chartContainer: '.js-chart-container'
        },
        events: {
            'click @ui.toggleButton': 'statisticShowToggle'
        },
        initialize: function (options) {
            this.hideStatistic = options.hideStatistic;
        },
        onRender: function () {
            if (!this.hideStatistic) {
                this.getData();
                this.ui.tooltip.tooltip();
            }
        },
        statisticShowToggle: function (e) {
            var toggle = !(this.ui.statistic.is('.l-only'));
            this.ui.statistic.toggleClass('l-only', toggle);

            $(e.target).text(toggle ? $(e.target).data('text') : $(e.target).data('toggle-text'));
        },
        createSummaryPie: function (parent_selector, data) {

            var other_color = getColor(-1),
                isEmpty = data.length == 0,
                chartWidth = 440,
                chartHeight = 200,
                outerRadius = 100,
                innerRadius = 50;

            _.each(data, function (item, i) {
                item.index = i;
            });

            if (isEmpty) {
                data = [
                    {
                        label: Valamis.language['noDataLabel'],
                        value: 100,
                        color: other_color
                    }
                ];
            }

            //pie
            var arc = d3.svg.arc()
                .outerRadius(outerRadius)
                .innerRadius(innerRadius);

            var pie = d3.layout.pie()
                .sort(null)
                .value(function (d) {
                    return d.value;
                });

            var svg = d3.select(parent_selector)
                .append('svg')
                .attr('width', chartWidth)
                .attr('height', chartHeight)
                .append('g')
                .attr('transform', 'translate(' + outerRadius + ',' + outerRadius + ')');

            var path = svg.selectAll('.arc')
                .data(pie(data))
                .enter().append('g')
                .attr('class', 'arc');

            path.append('path')
                .attr('d', arc)
                .style('fill', function (d) {
                    return getColor((d.data.label == '') ? -1 : d.data.index);
                });

            //tooltip
            if (!isEmpty) {
                var tooltip = d3.select(parent_selector)
                    .append('div')
                    .attr('class', 'tooltip right');

                tooltip
                    .append('div')
                    .attr('class', 'tooltip-inner');

                path.on('mouseover', function (d) {
                    var total = d3.sum(data.map(function (d) {
                        return (d.enabled) ? d.value : 0;
                    }));
                    //var percent = Math.round(1000 * d.data.value / total) / 10;//example
                    var text_value = d.data.label + ' <b>' + d.data.value + '%</b>';
                    tooltip.select('.tooltip-inner').html(text_value);
                    tooltip.style('opacity', 1);
                });

                path.on('mouseout', function () {
                    tooltip.style('opacity', 0);
                });

                path.on('mousemove', function (d) {
                    var parent_offset = $(parent_selector).offset(),
                        posX = d3.event.clientX - parent_offset.left,
                        posY = d3.event.clientY - parent_offset.top;

                    posY += $(window).scrollTop();
                    tooltip
                        .style('top', (posY + 15) + 'px')
                        .style('left', (posX + 10) + 'px');
                });
            }

            //legend
            var wrap = function (d) {
                var self = d3.select(this),
                    textLength = self.node().getComputedTextLength(),
                    text = self.text();
                while (( textLength > self.attr('width')) && text.length > 0) {
                    text = text.slice(0, -1);
                    self.text(text + '...');
                    textLength = self.node().getComputedTextLength();
                }
            };

            var legendRectSize = 16,
                legendSpacing = 10,
                percentSpacing = 160;

            var legend = svg.selectAll('.legend')
                .data(data)
                .enter()
                .append('g')
                .attr('class', 'legend')
                .attr('transform', function (d, i) {
                    var height = legendRectSize + legendSpacing;
                    var horz = outerRadius + 45;
                    var offset = data.length * ( height / 2 - 1 );
                    var vert = i * height - offset;
                    return 'translate(' + horz + ',' + vert + ')';
                });

            legend.append('rect')
                .attr('width', legendRectSize)
                .attr('height', legendRectSize)
                .attr('rx', '4')
                .style('fill', function (d) {
                    return getColor((d.label == '') ? -1 : d.index);
                });

            legend.append('text')
                .attr('x', legendRectSize + legendSpacing)
                .attr('y', legendRectSize - legendSpacing + 5)
                .attr('dy', '.15em')
                .attr('font-size', '13px')
                .attr('font-weight', 'bold')
                .attr('fill', '#555555')
                .text(function (d) {
                    return d.label || Valamis.language['otherLabel'];
                })
                .attr('width', percentSpacing - legendRectSize - legendSpacing - 5)
                .each(wrap);

            if (!isEmpty) {
                legend.append('text')
                    .attr('x', percentSpacing)
                    .attr('y', legendRectSize - legendSpacing + 5)
                    .attr('dy', '.15em')
                    .attr('font-size', '13px')
                    .attr('font-weight', 'bold')
                    .attr('fill', '#2c2c2c')
                    .text(function (d) {
                        return d.value + '%';
                    });
            }
        },
        getData: function () {
            var that = this;

            var lessonSummary = new valamisStudySummary.Entities.LessonSummary();
            lessonSummary.getData().then(
                function(response) {
                    that.ui.lessonsCompleted.html(response.lessonsCompleted || 0);
                    that.createSummaryPie(that._uiBindings.chartContainer, response.piedata );
                }
            );

            var pathSummary = new valamisStudySummary.Entities.PathSummary();
            pathSummary.getPathsByStatus({}, {status: 'InProgress'}).then(
                function(response) { that.ui.pathsInProgress.html(response.total || 0) }
            );
            pathSummary.getPathsByStatus({}, {status: 'Success'}).then(
                function(response) { that.ui.pathsCompleted.html(response.total || 0) }
            );
            pathSummary.getGoalsCompleted().then(
                function(response) { that.ui.goalsAchieved.html(response.value || 0) }
            );
        }
    });

});