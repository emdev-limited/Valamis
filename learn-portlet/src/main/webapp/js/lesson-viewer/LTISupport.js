ltiSupport = null;

ltiCollection = new lessonViewer.Entities.LTICollection;
ltiCollection.fetch();

ltiSupport = {
    handle: function($slide){
        var $elements = $slide.find('.rj-element');
        var that = this;
        $elements.each(function(){
            var $elem = jQueryValamis(this);
            var $iframe = $elem.find('iframe');

            var iframeName = $iframe.attr('name');
            var ltiContent = $iframe.attr('lti-src');
            if (ltiContent && !$elem.hasClass('loaded')) {
                var provider = ltiCollection.find(function (elem) {
                    return elem.get('url') == ltiContent;
                });
                if (provider) {
                    that.loadIFrame(provider, iframeName, $elem);
                    $elem.addClass('loaded');
                } else {
                    $elem.prepend(Mustache.to_html(jQuery('#warningNoLtiTemplate').html(),
                        {errorNoLTILabel: Valamis.language['errorNoLTILabel']}));
                }
            }
        });
    },
    createForm : function(id, target, actionUrl, parameters, $appendElement) {
        jQuery(".tempForm").remove();
        var form = jQuery(
            '<form class="tempForm" style="display:none;"' +
            'id="' + id + '"' +
            'target="' + target + '"' +
            'action="' + actionUrl + '"' +
            'method="POST"' +
            '></form>');
        for(var key in parameters) {
            form.append('<input type="text" name="' + key + '" value="'+parameters[key]+'">');
        }
        form.append('<input type="submit">');
        $appendElement.append(form);
        return form;
    },

    loadIFrame : function(model, iframeName, $appendElem) {

        var targetUrl = model.get('url');
        var customerKey = model.get('customerKey');
        var customerSecret = model.get('customerSecret');

        var nonce = (new Date()).getTime();

        var parameters = {
            oauth_consumer_key : customerKey,
            oauth_signature_method : lessonViewer.ltiOauthSignatureMethod,
            oauth_version :lessonViewer.ltiOauthVersion,
            lti_message_type : lessonViewer.ltiMessageType,
            lti_version : lessonViewer.ltiVersion,
            resource_link_id : "valamis-resource-link",
            launch_presentation_return_url : lessonViewer.ltiLaunchPresentationReturnUrl,
            selection_directive :'yes please',
            oauth_nonce: nonce,
            oauth_timestamp: Math.round(nonce / 1000.0),
            user_id: Utils.getUserId(),
            lis_person_name_given: lessonViewer.ltiFirstName,
            lis_person_name_family: lessonViewer.ltiLastName,
            lis_person_name_full: lessonViewer.ltiFirstName + ' ' + lessonViewer.ltiLastName,
            lis_person_contact_email_primary: lessonViewer.ltiEmail,
            roles: "Learner",
            oauth_callback: "about:blank"
        };

        parameters['oauth_signature'] = decodeURIComponent(oauthSignature.generate('POST', targetUrl, parameters,  customerSecret));

        //We launch a POST request to the iframe by creating a hidden form.
        this.createForm("valamis-resource-link", iframeName, targetUrl, parameters, $appendElem).submit();
    },
};