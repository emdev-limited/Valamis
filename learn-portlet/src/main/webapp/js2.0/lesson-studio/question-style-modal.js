/* TODO: Remove this later (not used) */
QuestionStyleModalView = Marionette.ItemView.extend({
    template: '#questionStyleModalTemplate',
    className: 'question-style-modal',
    events: {
        'change select': 'selectChanged',
        'click .js-save': 'saveChanges'
    },
    fonts: { //From CKEDITOR
        'Arial': "Arial, Helvetica, sans-serif",
        'Comic Sans MS': "Comic Sans MS, cursive",
        'Courier New': "Courier New, Courier, monospace",
        'Georgia': "Georgia, serif",
        'Lucida Sans Unicode': "Lucida Sans Unicode, Lucida Grande, sans-serif",
        'Tahoma': "Tahoma, Geneva, sans-serif",
        'Times New Roman': "Times New Roman, Times, serif",
        'Trebuchet MS': "Trebuchet MS, Helvetica, sans-serif",
        'Verdana': "Verdana, Geneva, sans-serif"
    },
    fontSizes: [8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72],
    questionFont: {},
    answerFont: {},
    initialize: function (options) {
        this.model = options.model || {};

        this.setDefaults(options);

        //We can load some additional fonts or sizes
        if (options.fonts)
            _.extend(this.fonts, options.fonts);

        if (options.fontSizes)
            this.fontSizes.concat(options.fontSizes);

        this.fontSizes = _.uniq(this.fontSizes);
        _.sortBy(this.fontSizes, function (a) { //By default there were strings
            return parseInt(a);
        });

        var mustacheAccumulator = {};
        _.extend(mustacheAccumulator, {
            fonts: _.map(this.fonts, function (value, key) {
                return {name: key, css: value};
            }),
            fontSizes: _.map(this.fontSizes, function (value) {
                return {name: value, css: value + 'px'}
            })
        });
        _.extend(mustacheAccumulator, Valamis.language);

        this.template = _.template(Mustache.to_html(jQueryValamis(this.template).html(), mustacheAccumulator));
        this.content = options.content.clone();
    },
    onRender: function () {
        var self = this;

        this.$('.js-color-picker').colpick({
            layout: 'hex',
            submit: 0,
            onChange: function (hsb, hex, rgb, el) {
                jQueryValamis(el).val(hex);
                jQueryValamis(el).css('background-color', '#' + hex);

                self.setColor(el, hex);
            },
            onBeforeShow: function (colorpicker) {
                jQueryValamis(colorpicker).css({
                    'z-index': 1101
                });
                jQueryValamis(this).colpickSetColor(self.getColor(this));
            }
        });

        this.buildPreview();
        this.updateControls();
    },
    buildPreview: function () {
        var preview = this.$('.question-preview .reveal');
        preview.empty();
        if (this.content && this.model)
            preview.append(this.content);
        this.content.css('background-color', slidesApp.activeSlideModel.get("bgColor"));
        this.updatePreview();
    },
    updatePreview: function () {
        var preview = this.$('.question-preview'),
            header = preview.find('h2 > p');


        header.css({
            'font-family': this.questionFont.family,
            'font-size': this.questionFont.size,
            'color': this.questionFont.color
        });

        var answers = getAnswerElement(preview, this.model.get("questionType"));
        answers.text.css({
            'font-family': this.answerFont.family,
            'font-size': this.answerFont.size,
            'color': this.answerFont.color
        });
       answers.background.css({
           'background': 'none',
           'border': 'none',
           'background-color': this.answerFont.background
       });
       answers.background.find(' > *').css('color', this.answerFont.color);
    },
    updateControls: function () {
        var self = this;
        this.$('.js-color-picker').each(function () {
            jQueryValamis(this).css('background-color', '#' + self.getColor(this))
        });
        this.selectValue(this.$('.js-question-font'), this.questionFont.family.split(',')[0].replace(/['"]+/g, ''));
        this.selectValue(this.$('.js-question-font-size'), this.questionFont.size.replace('px', ''));
        this.selectValue(this.$('.js-answer-font'), this.answerFont.family.split(',')[0].replace(/['"]+/g, ''));
        this.selectValue(this.$('.js-answer-font-size'), this.answerFont.size.replace('px', ''));
    },
    selectValue: function (select, value) {
        select.find('option').filter(function () {
            return jQueryValamis(this).text() === value
        }).prop('selected', true);
    },
    setDefaults: function (options) {
        if (options.content && this.model) {
            var header = options.content.find('h2 > p');
            this.questionFont = {
                family: header.css('font-family'),
                size: header.css('font-size'),
                color: header.css('color')
            };

            var answer = getAnswerElement(options.content, this.model.get('questionType'));
            this.answerFont = {
                family: answer.text.css('font-family'),
                size: answer.text.css('font-size'),
                color: answer.text.css('color'),
                background: answer.background.css('background-color')
            }
        }
        _.extend(this.fonts, this.getFonts());
        this.fontSizes.push(parseInt(this.questionFont.size));
        this.fontSizes.push(parseInt(this.answerFont.size));
    },
    getFonts: function () { //returns first question and answer fonts as object
        return _.object(
            _.map([this.answerFont.family, this.questionFont.family], function (el) {
                    var fontName = el.split(',')[0].replace(/["']+/g, '');
                    return [fontName, el];
                }
            )
        );
    },
    selectChanged: function (ev) {
        var prop = this.getProperty(ev.target);
        var text = jQueryValamis(ev.target).find(':selected').text();
        if (this.fonts[text])
            text = this.fonts[text]; //It is font
        else
            text += 'px'; //If it is not font, it is size
        prop.obj[prop.prop] = text;
        this.updatePreview();
    },
    getProperty: function (el) {
        var classList = jQueryValamis(el).attr('class').split(/\s+/);

        for (var i in classList) {
            switch (classList[i]) {
                case 'js-question-font':
                    return {obj: this.questionFont, prop: 'family'};
                case 'js-question-font-size':
                    return {obj: this.questionFont, prop: 'size'};
                case 'js-font-color':
                    return {obj: this.questionFont, prop: 'color'};
                case 'js-answer-font':
                    return {obj: this.answerFont, prop: 'family'};
                case 'js-answer-font-size':
                    return {obj: this.answerFont, prop: 'size'};
                case 'js-answer-font-color':
                    return {obj: this.answerFont, prop: 'color'};
                case 'js-answer-bg-color':
                    return {obj: this.answerFont, prop: 'background'};

            }
        }
    },
    setColor: function (el, color) {
        var prop = this.getProperty(el);
        prop.obj[prop.prop] = '#' + color;

        this.updatePreview();
    },
    getColor: function (el) {
        var prop = this.getProperty(el),
            res = prop.obj[prop.prop];

        if (res == 'transparent' || res == 'rgba(0, 0, 0, 0)') {
            res = this.backgroundColor;
            prop.obj[prop.prop] = res;
            this.updatePreview();
        }

        if (res.startsWith('rgb')) {
            res = res.match(/^rgb.*\((\d+),\s*(\d+),\s*(\d+).*\).*$/);
            if (res)
                return jQueryValamis.colpick.rgbToHex({r: parseInt(res[1]), g: parseInt(res[2]), b: parseInt(res[3])});
        }
        return res.replace('#', '');
    },
    saveChanges: function () {
        var appearance = {
            question: this.questionFont,
            answer: this.answerFont
        };
        this.trigger('saveChanges', appearance, this.model.get("questionType"));
    },
    onDestroy: function () {
        this.$('.js-color-picker').colpickDestroy();
    }
});