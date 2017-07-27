(function () {
    if(typeof window.CKEDITOR !== 'undefined'){
        if(typeof window.CKEDITOR._bundle === 'undefined' || window.CKEDITOR._bundle != 'valamis'){
            delete window.CKEDITOR;
            jQuery('script[src*="/vendor/ckeditor/"]').remove();
        }
    }
}());