valamisReportSettings.module('Views', function (Views, valamisReportSettings, Backbone, Marionette, $, _) {

  var localeDateFormat = 'DD/MM/YYYY',
    fetchDateFormat = 'YYYY/MM/DD';

  var PERIOD_DAYS = {
    periodLastWeek: 7,
    periodLastMonth: 30,
    periodLastYear: 365,
    periodCustom: 1
  };

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#ValamisReportLayoutTemplate',
    className: 'valamis-report-container',
    regions:{
      'reportSettingsRegion' : '#valamisReportSettings'
    },
    onRender: function() {
      this.reportSettingsCollection = new valamisReportSettings.Entities.ReportSettingsModel();
      this.reportSettingsCollection.on('sync', this.renderSettings, this);
      this.reportSettingsCollection.fetch();
    },
    renderSettings: function(data) {
      var fetchedOptions = this.options,
        settings = this.reportSettingsCollection;

      this.reportSettingsView = new valamisReportSettings.Views.ReportSettingsView({
        collection: this.reportSettingsCollection,
        userIds: fetchedOptions.userIds
      });

      if (data.length > 0) {

        if (fetchedOptions.reportType != '')
          settings.get('reportType').set('currentOptionId', fetchedOptions.reportType);

        if (fetchedOptions.reportPeriodType != '')
          settings.get('reportPeriod').set('currentOptionId', fetchedOptions.reportPeriodType);

        if (fetchedOptions.reportsScope != '')
          settings.get('reportsScope').set('currentOptionId', fetchedOptions.reportsScope);

        if (settings.get('reportPeriod').get('currentOptionId') == 'periodCustom') {
          if (fetchedOptions.reportPeriodStart != '')
            settings.get('reportPeriod').set('startDate', this.options.reportPeriodStart);

          if (fetchedOptions.reportPeriodEnd != '')
            settings.get('reportPeriod').set('endDate', this.options.reportPeriodEnd);
        } else {
          var days = PERIOD_DAYS[settings.get('reportPeriod').get('currentOptionId')];
          settings.get('reportPeriod').set('startDate', moment().subtract(days, 'days').format(fetchDateFormat));
          settings.get('reportPeriod').set('endDate', moment().format(fetchDateFormat))
        }

        if (fetchedOptions.userIds.length > 0) {
          _.each(fetchedOptions.userIds, function(id) {
            settings.get('reportUsers').get('users').push({ id: id });
          });
        }

        this.reportSettingsView.updateSettingsVisibility();
        this.reportSettingsRegion.show(this.reportSettingsView);
      }
    }
  });

  Views.SelectorView = Marionette.ItemView.extend({
    tagName: 'li',
    className: function() {
      if (this.model.get('groupFirstItem')) return 'group-first-item';
    },
    template: '#reportDropDownSelectorOption',
    templateHelpers: function() {
      return {
        optionTitle: Valamis.language[this.model.get('id')+'SettingsLabel']
      }
    },
    events: {
      'click span': 'updateMenu'
    },
    updateMenu: function(){
      this.triggerMethod('item:set_active', this.model);
    },
    onRender: function () {
      this.triggerMethod('item:check_active', this.model);
    }
  });


  Views.ReportSettingFieldsetView = Marionette.CompositeView.extend({
    defaults: {
      type: 'dropdown'
    },
    template: '#reportSettingsFieldset',
    className: 'fieldset',
    childView: Views.SelectorView,
    childViewContainer: 'ul',
    childEvents: {
      'item:check_active': function (childView, childModel) {
        childModel.set('isActive', childModel.get('id') == this.model.get('currentOptionId'));
        childView.$el.toggleClass('active', childModel.get('isActive'));
      },
      'item:set_active': function (childView, childModel) {
        var childSelectorId = childModel.get('id');

        if (this.model.get('data') == 'datePeriod') {

          childView.$el.siblings().removeClass('active');
          childView.$el.addClass('active');

          if (childModel.get('id') == 'periodCustom') {
            this.dateSelectors['startSelector'].val();
            this.dateSelectors['startSelector'].focus();
            this.$('button.js-apply').removeAttr('disabled');

          } else {

            if (this.model.get('currentOptionId') != childSelectorId) {
              this.model.set('currentOptionId', childSelectorId);
            }

            var currentSelectionLabel = Valamis.language[childSelectorId + 'SettingsLabel'],
              startDateValue = moment().subtract(childModel.get('days'), 'days'),
              endDateValue = moment();

            if (this.model.get('hotSelector')) {
              currentSelectionLabel = startDateValue.format('ll') + ' &ndash; ' + endDateValue.format('ll');
            }

            this.$('.current-selection .js-label').html(currentSelectionLabel);

            this.$('.js-dropdown-selector').removeClass('show-dropdown-menu');
            this.$('.date-inputs input').each(function() {
              $(this).data('DateTimePicker').hide();
            });

            this.setSelectorDates(startDateValue,endDateValue,true);
            this.$('button.js-apply').attr('disabled','disabled');

            this.setModelDates(startDateValue, endDateValue);
          }

        } else {

          if (this.model.get('currentOptionId') != childSelectorId) {
            this.model.set('currentOptionId', childSelectorId);
          }

          if (!(this.model.get('primary'))) { // primary field change renders the whole settings view
            this.render();
          }
        }
      }
    },
    initialize: function(){
      this.$el.prop('id', this.model.get('id')+'Fieldset');
      if (this.model.get('type') == 'dropdown') {
        var options = this.model.get('options');
        this.collection = new valamisReportSettings.Entities.ReportSelectorModel(options);
      }
    },
    templateHelpers: function() {
      return {
        title: Valamis.language[this.model.get('id')+'SettingsLabel'],
        isDropdown: (this.model.get('type') == 'dropdown'),
        isModal: (this.model.get('type') == 'modal'),
        isDateSelector: (this.model.get('data') == 'datePeriod'),
        currentOptionTitle: Valamis.language[this.model.get('currentOptionId')+'SettingsLabel'],
        buttonLabel: Valamis.language[this.model.get('data')+'ButtonLabel']
      }
    },
    events: {
      'click .current-selection': 'toggleDropdown',
      'click .js-select-users': 'showModal'
    },
    toggleDropdown: function() {
      this.triggerMethod('dropdown:toggle', this.model);
    },
    showModal: function() {
      this.triggerMethod('users:select', this.model);
    },
    onShow: function() {
      this.dateSelectors = {
        startSelector: this.$('#period-selector-start'),
        endSelector: this.$('#period-selector-end')
      };

      if (this.model.get('data') == 'datePeriod') {
        if (this.model.get('currentOptionId') == 'periodCustom' || this.model.get('hotSelector')) {
          this.setSelectorDates(
            moment(this.model.get('startDate'), fetchDateFormat),
            moment(this.model.get('endDate'), fetchDateFormat),false
          );
        }
        this.initDatePicker();
      }
    },
    modelEvents: {
      'change:currentOptionId': 'changeCurrent'
    },
    changeCurrent: function() {
      if (this.model.get('data') != 'datePeriod') {
        this.triggerMethod('settings:changed', this.model);
      }
    },
    setSelectorDates: function(startDateValue, endDateValue, predefinedValues) {
      var startDateShort = moment(startDateValue).format(localeDateFormat),
        endDateShort = moment(endDateValue).format(localeDateFormat);

      this.dateSelectors['startSelector'].val(startDateShort);
      this.dateSelectors['endSelector'].val(endDateShort);

      if (!predefinedValues) {
        this.$('.current-selection .js-label').html(
          moment(startDateValue).format('ll') + ' &ndash; ' + moment(endDateValue).format('ll')
        );
        this.$('.js-dropdown-selector').removeClass('show-dropdown-menu');
      }
    },
    setModelDates: function(startDate, endDate) {
      this.model.set('startDate', startDate.format(fetchDateFormat));
      this.model.set('endDate', endDate.format(fetchDateFormat));

      this.triggerMethod('settings:changed', this.model);
    },
    initDatePicker: function () {
      var that = this;

      var datePickerSettings = {
        useCurrent: false,
        maxDate: $.now(),
        locale: Utils.getLanguage(),
        format: localeDateFormat,
        inline: true,
        widgetParent: that.$('#selector-datepicker-wrapper'),
        focusOnShow: false,
        debug: true
      };

      $.each(this.dateSelectors, function(key, selector) {
        selector.val(moment(that.model.get(key == 'startSelector' ? 'startDate' : 'endDate'), fetchDateFormat).format(localeDateFormat));
        selector.datetimepicker(datePickerSettings);
        selector.data('DateTimePicker').hide();

        selector.on('click', function() {
          that.$('button.js-apply').removeAttr('disabled');

          if (that.model.get('currentOptionId') != 'periodCustom') {
            _.each(that.collection.models, function(item){
              item.set('isActive', item.get('id') == 'periodCustom');
            });

            that.$('.dropdown-options li').removeClass('active');
            that.$('.dropdown-options li:last-child').addClass('active');
          }

          that.dateSelectors[(key == 'startSelector') ? 'endSelector' : 'startSelector'].data('DateTimePicker').hide();
        });

        selector.on('dp.change', function (e) {
          if (key == 'startSelector')
            that.dateSelectors['endSelector'].data('DateTimePicker').minDate(e.date);
          else
            that.dateSelectors['startSelector'].data('DateTimePicker').maxDate(e.date);
        });
      });

      this.$('button.js-apply').on('click',function (e) {
        e.preventDefault();

        var startDateValue = that.dateSelectors['startSelector'].val(),
          endDateValue = that.dateSelectors['endSelector'].val(),
          startDate,
          endDate;

        if (startDateValue && endDateValue) {

          startDate = moment(startDateValue, localeDateFormat);
          endDate = moment(endDateValue, localeDateFormat);

          that.model.set('currentOptionId', 'periodCustom');
          that.setSelectorDates(startDate, endDate, false);
          that.setModelDates(startDate, endDate);
        }
      });
    }

  });

  Views.ReportSettingsView = Marionette.CollectionView.extend({
    className: 'report-settings-wrapper',
    childView: Views.ReportSettingFieldsetView,
    filter: function (model, index, collection) {
      return model.get('primary') || model.get('visibility');
    },
    updateSettingsVisibility: function () {
      var reportSettings = this.collection,
        reportType = reportSettings.get('reportType').get('currentOptionId'),
        reportTypeOption = _.find(reportSettings.get('reportType').get('options'), {'id': reportType}),
        reportDependencies = reportTypeOption.dependencies;

      reportSettings.each(function (model, index) {
        model.set('visibility', _(reportDependencies).contains(model.get('id')));
      });
    },
    saveSettings: function(model) {
      var options = [],
        settingsCollection = model.collection;

      options.currentTypeId = settingsCollection.get('reportType').get('currentOptionId');
      options.reportsScope = settingsCollection.get('reportsScope').get('currentOptionId');
      options.periodType = settingsCollection.get('reportPeriod').get('currentOptionId');
      options.periodStartDate = settingsCollection.get('reportPeriod').get('startDate');
      options.periodEndDate = settingsCollection.get('reportPeriod').get('endDate');
      options.userIds = _.pluck(settingsCollection.get('reportUsers').get('users'), 'id');

      window.LearnAjax.post(valamisReportSettings.actionURL, {
        action: 'SaveAll',
        reportType: options.currentTypeId,
        reportsScope: options.reportsScope,
        userIds: options.userIds.join(','),
        periodType: options.periodType,
        startDate: options.periodStartDate,
        endDate: options.periodEndDate
      }).done(function () {
        valamisApp.execute('notify', 'success', Valamis.language['settingsSavedMessageLabel']);
      }).error(function () {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    saveUsers: function (model, view, userIds) {
      var currentUserIds = _.pluck(model.get('users'), 'id');
      var newUserIds = _.union(currentUserIds, userIds);
      model.set({
        users: _.map(newUserIds, function(id) { return { id: id }; })
      });
      window.LearnAjax.post(valamisReportSettings.actionURL, {
        action: 'SaveUsers',
        userIds: newUserIds.join(',')
      }).done(function () {
        valamisApp.execute('notify', 'success', Valamis.language['settingsSavedMessageLabel']);
        view.options.reportModel.set('users', _.map(newUserIds, function (id) { return { id: id }; }));
      }).error(function () {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      }).always(function () {
        if(view.collection instanceof Backbone.Collection) view.fetchCollection(true, { userIds: newUserIds });
      });
    },
    childEvents: {
      'dropdown:toggle': function (childView, model) {
        var activeStateClass = 'show-dropdown-menu';
        var dropdownElClicked = $('.js-dropdown-selector',childView.$el);
        var dropdownElClickedNewState = !(dropdownElClicked.hasClass(activeStateClass));

        this.$('.js-dropdown-selector').removeClass(activeStateClass);
        dropdownElClicked.toggleClass(activeStateClass,dropdownElClickedNewState);

        if (dropdownElClickedNewState && model.get('data') == 'datePeriod') {

          $.each(childView.dateSelectors, function(key, selector) {
            selector.data('DateTimePicker').hide();
          });

        }
      },
      'users:select':  function (childView, model) {
        var that = this;
        var viewTemplate = '#reportSelectUsersModalTemplate';
        var selectUsersView = new valamisReportSettings.Views.SelectUsers.UsersView({
          reportModel: model,
          showAvailable: false
        });

        selectUsersView.on('users:selected', function(view, userIds) {
          that.saveUsers(model, view, userIds);
        });

        var modalView = new valamisApp.Views.ModalView({
          template: viewTemplate,
          header: Valamis.language[model.get('data') + 'ModalHeaderLabel'],
          contentView: selectUsersView
        });

        valamisApp.execute('modal:show', modalView);
      },
      'settings:changed': function (childView, model) {
        this.saveSettings(model);

        if (model.get('primary')) {
          this.updateSettingsVisibility();
          this.render();
        }
      }
    }
  });

});