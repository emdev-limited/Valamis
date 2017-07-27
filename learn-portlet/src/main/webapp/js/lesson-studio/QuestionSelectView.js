var contentManagerModalView = Marionette.ItemView.extend({
    template: '#content-manager-modal-template',
    onShow: function () {

        var contentManagerOptions = {
            resourceName: 'questionManager',
            app: contentManager,
            appOptions: {
                showGlobalBase: lessonStudio.cmShowGlobalBase,
                selectMode: true
            }
        };

        valamisApp.execute('subapp:start', contentManagerOptions);

        //make small size for modal window
        var portlet_container = jQueryValamis('#valamisAppModalRegion').closest('.portlet'),
            current_min_width = portlet_container.attr('min-width');

        portlet_container
            .attr('min-width', '768px 480px')
            .data('old-min-width', current_min_width)
            .attr('data-elementquery-bypass', 1);
    }

});