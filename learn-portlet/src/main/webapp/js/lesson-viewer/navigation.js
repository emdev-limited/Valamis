NavigationNode = Backbone.Model.extend({
    navigateToSlide: function() {
        this.trigger('internal-navigate', this);
    },
    toggleActive: function() {
        var isActive = this.get('active');
        this.set({active: !isActive, completed: true});
    },
    setComplete: function() {
        this.set('completed', true);
    }
});

/*  If this is directory: { elementType: 'directory', childElements: [$childElementsArray] } */
NavigationNodeCollection = Backbone.Collection.extend({
    model: NavigationNode,
    previouslyToggled: null,
    navigate: function(){
        throw new Error("Replace me");
    },
    afterInitialization: function(){
        var that = this;
        function afterInitializationHelper(elem){
            elem.each(function(model){
                if(model.get('elementType') == 'directory') {
                    model.internalCollection = new NavigationNodeCollection(model.get('childElements'));
                    afterInitializationHelper(model.internalCollection);
                } else {
                    that.listenTo(model,'internal-navigate',that.navigate);
                }
            })
        }
        afterInitializationHelper(this);
    },
    toggle: function(){
        throw new Error("Replace me");
    },
    toggleHelper: function(toggleModel){
        toggleModel.toggleActive();
        if(this.previouslyToggled != null) this.previouslyToggled.toggleActive();
        this.previouslyToggled = toggleModel;
    }
});
