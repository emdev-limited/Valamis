certificateExpirationTracker.module('Views', function (Views, certificateExpirationTracker, Backbone, Marionette, $, _) {
    var dateFormat = 'YYYY-MM-DD',
        userLanguage = Utils.getLanguage();

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#certificateExpirationTrackerTemplate',
        className: 'valamis-certificate-expiration-tracker-container',
        regions: {
            'toolbarRegion': '#certificateExpirationTrackerToolbarRegion',
            'resultsRegion': '#certificateExpirationTrackerResultsRegion',
            'contentRegion': '#certificateExpirationTrackerContentRegion'
        },
        childEvents: {
            'settings:changed': 'render'
        },
        initialize: function(options) {
            this.toolbarModel = options.toolbarModel;
        },
        onRender: function() {
            this.showToolbar(this.toolbarModel);

            this.certificatesModel = new certificateExpirationTracker.Entities.CertificatesModel({});
            var that = this;
            this.certificatesModel.fetch({
                startDate : this.toolbarModel.get('startDate'),
                endDate : this.toolbarModel.get('endDate'),
                courseId: Utils.getCourseId
            }).then(function() {
                that.showResults();
                that.showItems();
            });
        },
        showToolbar: function(model) {
            var toolbarView = new Views.ToolbarView({ model: model});
            this.toolbarRegion.show(toolbarView);
        },
        showResults: function() {
            var that = this;
            var resultsView = new Views.ResultsView({
                model: this.certificatesModel,
                templateHelpers: function() {
                    return {
                        allAmount: that.certificatesModel.get('totalExpired') + that.certificatesModel.get('totalExpires')
                    }
                }
            });
            this.resultsRegion.show(resultsView);
        },
        showItems: function() {
            var oneColumnItemsCount = 5;
            var expirationItemsCollection = new certificateExpirationTracker.Entities.ExpirationItemsCollection(
                this.certificatesModel.get('certificatesInfo')
            );
            var fewItems = (this.certificatesModel.get('totalExpired') + this.certificatesModel.get('totalExpires')) <= oneColumnItemsCount;

            var expirationItemsContainerView = new Views.ExpirationItemsContainerView({
                collection: expirationItemsCollection,
                fewItems: fewItems
            });
            this.contentRegion.show(expirationItemsContainerView);
        }
    });
    
    Views.ToolbarView = Marionette.LayoutView.extend({
        template: '#expirationToolbarTemplate',
        events: {
            'click .js-send-notifications': 'sendNotifications',
            'click .js-current-selection': 'dropdownToggle',
            'click .js-dropdown-options li': 'changePeriod'
        },
        initialize: function() {
            var startDate = this.model.get('startDate') || moment().format(dateFormat);
            var endDate = this.model.get('endDate') || moment().add(7, 'days').format(dateFormat);

            this.model.set('startDate', startDate);
            this.model.set('endDate', endDate);
        },
        templateHelpers: function() {
            var startDateValue = moment(this.model.get('startDate'));
            var endDateValue = moment(this.model.get('endDate'));

            return {
                startDateValue: moment(startDateValue).format('ll'),
                endDateValue: moment(endDateValue).format('ll')
            }
        },
        onShow: function() {
            var that = this;
            this.dateSelectors = {
                startSelector: this.$('#period-selector-start'),
                endSelector: this.$('#period-selector-end')
            };

            this.$('button.js-apply').on('click',function (e) {
                e.preventDefault();

                var startDate = that.dateSelectors['startSelector'].data('DateTimePicker')
                        .viewDate().format(dateFormat),
                    endDate = that.dateSelectors['endSelector'].data('DateTimePicker')
                        .viewDate().format(dateFormat);

                if (startDate && endDate) {
                    that.setSelectorDates(startDate, endDate);
                    that.setModelDates(startDate, endDate);
                }
            });
        },
        activatePeriod: function(timePeriod){
            var activeOption = _.findWhere(this.model.get('options'), {id : timePeriod});

            if(activeOption && activeOption.active) return activeOption;

            _.each(this.model.get('options'), function(option) {
                option.active=false;
            });

            activeOption = _.findWhere(this.model.get('options'), {id : timePeriod});

            activeOption.active = true;
            return activeOption;
        },
        changePeriod: function(e) {
            var timePeriod = $(e.target).parent().attr('data-value');
            var that = this;
            var startDate='', endDate='';
            e.preventDefault();

            var activeOption = this.activatePeriod(timePeriod);

            if(!!activeOption) {
                if (activeOption.id == 'periodCustom') {
                    that.dateSelectors['startSelector'].focus();
                    that.setCustomActive();
                }
                else {
                    startDate = moment().format(dateFormat);
                    endDate = moment(startDate).add(activeOption.days, 'days').format(dateFormat);

                    this.setSelectorDates(startDate, endDate);
                    this.setModelDates(startDate, endDate);
                }
            }
        },
        setCustomActive: function() {
            this.$('.js-dropdown-options li').removeClass('active');
            this.$('.js-dropdown-options li:last-child').addClass('active');
            this.$('button.js-apply').removeAttr('disabled');
        },
        setSelectorDates: function(startDate, endDate) {
            var startDateForUser = moment(startDate).locale(userLanguage).format('L');
            var endDateForUser = moment(endDate).locale(userLanguage).format('L');

            this.dateSelectors['startSelector'].val(startDateForUser);
            this.dateSelectors['endSelector'].val(endDateForUser);
        },
        setModelDates: function(startDate, endDate) {
            this.model.set({
                'startDate': startDate,
                'endDate': endDate,
                'startDateValue': moment(startDate).format('ll'),
                'endDateValue': moment(endDate).format('ll')
            });

            this.renderReport();
        },
        initDatePicker: function () {
            var that = this;

            var datePickerSettings = {
                format: 'L',
                useCurrent: false,
                locale: userLanguage,
                inline: true,
                widgetParent: that.$('#selector-datepicker-wrapper'),
                focusOnShow: false,
                debug: true
            };

            this.setSelectorDates(
                moment(this.model.get('startDate')),
                moment(this.model.get('endDate'))
            );

            _.each(this.dateSelectors, function(selector, key) {
                if (selector.data && selector.data('DateTimePicker')){
                    selector.datetimepicker('destroy');
                }

                selector.datetimepicker(datePickerSettings);
                selector.data('DateTimePicker').hide();

                selector.on('click', function(e) {
                    that.activatePeriod('periodCustom');
                    that.setCustomActive();

                    that.dateSelectors[(key == 'startSelector') ? 'endSelector' : 'startSelector'].data('DateTimePicker').hide();
                });

                selector.on('dp.change', function (e) {
                    if (key == 'startSelector') {
                        that.dateSelectors['endSelector'].data('DateTimePicker').minDate(e.date);
                    }
                    else {
                        that.dateSelectors['startSelector'].data('DateTimePicker').maxDate(e.date);
                    }
                });
            });

        },
        sendNotifications: function() {
            this.model.sendNotifications();
        },
        dropdownToggle: function() {
            var dropdownWrapper = this.$('.dropdown-wrapper');
            var isDropdownHidden = dropdownWrapper.hasClass('hidden');
            dropdownWrapper.toggleClass('hidden', !isDropdownHidden);

            var activeOption = _.findWhere(this.model.get('options'), {active : true});

            if(activeOption && activeOption.id == 'periodCustom') {
                this.setCustomActive();
            }

            if (isDropdownHidden) {
                this.initDatePicker();
            }
        },
        renderReport: function () {
            this.triggerMethod('settings:changed');
        }
    });

    Views.ResultsView = Marionette.LayoutView.extend({
        template: '#expirationResultsTemplate',
        className: 'results-container',
        onShow: function() {
            this.createPie(
                this.model.get('totalExpired'),
                this.model.get('totalExpires')
            );
        },
        createPie: function(rateExpired, rateExpires) {
            var height = 62,
                width = height,
                margin = 2,
                lineWidth = 5;

            var options = {
                pieColors: 'pieColors',
                pieContainer : '.js-expiration-pie-graphic'
            };

            options.rateExpires = rateExpires;
            options.rateExpired = rateExpired;

            var data=[
                { rate: options.rateExpires},
                { rate: options.rateExpired}
            ];

            var radius = Math.min(width - 2 * margin, height - 2 * margin) / 2;

            var arc = d3.svg.arc()
                .outerRadius(function (d) {
                    if(d.data.radius) return radius*d.data.radius;
                    return radius;})
                .innerRadius(Math.min(width - 2 * margin, height - 2 * margin) / 2 - lineWidth);

            var pie = d3.layout.pie()
                .sort(null)
                .value(function(d) { return d.rate; });

            var svgPie = $(options.pieContainer + ' svg');
            svgPie.remove();

            var svg = d3.select(options.pieContainer)
                .append("svg")
                .attr("class", "axis pie-circle")
                .attr("width", width)
                .attr("height", height)
                .append("g")
                .attr('class', options.pieColors)
                .attr("transform", "translate(" +(width / 2) + "," + (height / 2 ) + ")");

            svg.append("circle")
                .attr("cx", 0)
                .attr("cy", 0)
                .attr("r", width / 2);

            var g = svg.selectAll(".js-expiration-pie-graphic .arc")
                .data(pie(data))
                .enter().append("g")
                .attr("class", "arc");

            g.append("path")
                .attr("d", arc);
        }
    });

    Views.ExpirationItemView = Marionette.LayoutView.extend({
        template: '#expirationItemTemplate',
        className: 'expiration-item',
        events: {
            'click .js-send-notification': 'sendNotification'
        },
        templateHelpers: function() {
            var expirationLabel =
                this.model.get('certificateExpiredInFuture') ? 'expiresOnLabel' : 'expiredLabel';
            var expiredDate = this.model.get('expiredDate');
            expiredDate = moment(expiredDate).locale(userLanguage).format('L');

            return {
                expiration: Valamis.language[expirationLabel],
                expiredDate: expiredDate
            }
        },
        sendNotification: function() {
            this.model.sendNotification({ courseId: Utils.getCourseId});
        }
    });

    Views.ExpirationItemsContainerView = Marionette.CollectionView.extend({
        className: function () {
            var classes = 'items-container clearfix ';

            if (this.options.fewItems) {
                classes = classes + 'one-column';
            }

            return classes;
        },
        childView: Views.ExpirationItemView
    });
});