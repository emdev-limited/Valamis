function findAPI(win) {
    var findAPITries = 0;
    while ((win.API_1484_11 == null) && (win.parent != null) && (win.parent != win)) {
        findAPITries++;
        if (findAPITries > 20) return null;
        win = win.parent;
    }
    return win.API_1484_11;
}

function getAPI() {
    var theAPI = findAPI(window);
    if ((theAPI == null)) {
        if ((window.opener != null) && (typeof(window.opener) != "undefined"))
            theAPI = findAPI(window.opener);
    }
    return theAPI;
}

(function($){

    $.fn.shuffle = function() {

        var allElems = this.get(),
            getRandom = function(max) {
                return Math.floor(Math.random() * max);
            },
            shuffled = $.map(allElems, function(){
                var random = getRandom(allElems.length),
                    randEl = $(allElems[random]).clone(true)[0];
                allElems.splice(random, 1);
                return randEl;
            });

        this.each(function(i){
            $(this).replaceWith($(shuffled[i]));
        });

        return $(shuffled);

    };

})(jQuery);

var escapeArray = {
    '<': "&lt;",
    '>': "&gt;",
    '&': "&amp;",
    '"': "&quot;",
    '\'': "&#39;",
    '\\': "&#92;",
    '\\\\\\\"': "\\\\\"", //Small fix if str has \"

    //Scandinavian letters
    'Ä': "&Auml;",
    'Ö': "&Ouml;",
    'Å': "&Aring;",
    'ä': "&auml;",
    'ö': "&ouml;",
    'å': "&aring;"
};

var unescapeElement = function(str) {
    _.each(escapeArray, function (value, key) {
        str = str.split(value).join(key)
    });
    return str;
};

var escapeElement = function(str) {
    _.each(escapeArray, function (value, key) {
        str = str.split(key).join(value)
    });
    return str;
};