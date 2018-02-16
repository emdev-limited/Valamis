package com.arcusys.valamis.slide.service.export

object PublisherFileLists {
  val VendorJSFolder = "js/vendor/"
  val VendorJSFileNames =
      "jquery.min.js" ::
      "classList.min.js" ::
      "reveal.min.js" ::
      "jquery-ui-1.11.4.custom.min.js" ::
      "jquery.ui.widget.js" ::
      "jquery.ui.touch-punch.min.js" ::
      "lodash.min.js" ::
      "backbone-min.js" ::
      "backbone.marionette.min.2.4.1.js" ::
      "backbone.service.js" ::
      "mustache.min.js" ::
      "tincan-min.js" ::
      "head.min.js" ::
      "toastr.min.js" ::
      "d3.min.js" ::
      Nil

  val VideoVendorJSFileNames = "video.js" :: Nil
  val MathVendorJSFileNames = "katex.min.js" :: Nil
  val WebglVendorJSFileNames = "three.min.js" :: "TrackballControls.js" :: Nil

  val SlideSetJSFolder ="js/"
  val SlideSetJSFileNames =
    "lesson-studio/helper.js" ::
      "lesson-studio/model-base-entities.js" ::
      "question-manager/models/QuestionType.js" ::
      Nil

  val CommonJSFolder = "common/"
  val CommonJSFileNames =
    "base.js" ::
      "translations.js" ::
      "TinCanPackageRenderer.js" ::
      "TinCanPackageGenericItem.js" ::
      Nil

  val SlideSetCSSFolder ="css/"
  val SlideSetCSSFileNames =
    "reveal.min.css" ::
      "video-js.min.css" ::
      "katex.min.css" ::
      "valamis.css" ::
      "valamis_slides.css" ::
      "toastr.css" ::
      "valamis_slides_theme.css" ::
      Nil

  val FontsFolder ="fonts/"
  val FontsFileNames =
    "device-icons26.eot" ::
    "device-icons26.svg" ::
    "device-icons26.ttf" ::
    "device-icons26.woff" ::
    "glyphicons-halflings-regular.eot" ::
    "glyphicons-halflings-regular.svg" ::
    "glyphicons-halflings-regular.ttf" ::
    "glyphicons-halflings-regular.woff" ::
    "HelveticaNeue-Light.ttf" ::
    "KaTeX_AMS-Regular.eot" ::
    "KaTeX_AMS-Regular.ttf" ::
    "KaTeX_AMS-Regular.woff" ::
    "KaTeX_AMS-Regular.woff2" ::
    "KaTeX_Main-Bold.eot" ::
    "KaTeX_Main-Bold.ttf" ::
    "KaTeX_Main-Bold.woff" ::
    "KaTeX_Main-Bold.woff2" ::
    "KaTeX_Main-Italic.eot" ::
    "KaTeX_Main-Italic.ttf" ::
    "KaTeX_Main-Italic.woff" ::
    "KaTeX_Main-Italic.woff2" ::
    "KaTeX_Main-Regular.eot" ::
    "KaTeX_Main-Regular.ttf" ::
    "KaTeX_Main-Regular.woff" ::
    "KaTeX_Main-Regular.woff2" ::
    "KaTeX_Math-BoldItalic.eot" ::
    "KaTeX_Math-BoldItalic.ttf" ::
    "KaTeX_Math-BoldItalic.woff" ::
    "KaTeX_Math-BoldItalic.woff2" ::
    "KaTeX_Math-Italic.eot" ::
    "KaTeX_Math-Italic.ttf" ::
    "KaTeX_Math-Italic.woff" ::
    "KaTeX_Math-Italic.woff2" ::
    "KaTeX_Math-Regular.eot" ::
    "KaTeX_Math-Regular.ttf" ::
    "KaTeX_Math-Regular.woff" ::
    "KaTeX_Math-Regular.woff2" ::
    "KaTeX_Size1-Regular.eot" ::
    "KaTeX_Size1-Regular.ttf" ::
    "KaTeX_Size1-Regular.woff" ::
    "KaTeX_Size1-Regular.woff2" ::
    "KaTeX_Size2-Regular.eot" ::
    "KaTeX_Size2-Regular.ttf" ::
    "KaTeX_Size2-Regular.woff" ::
    "KaTeX_Size2-Regular.woff2" ::
    "KaTeX_Size3-Regular.eot" ::
    "KaTeX_Size3-Regular.ttf" ::
    "KaTeX_Size3-Regular.woff" ::
    "KaTeX_Size3-Regular.woff2" ::
    "KaTeX_Size4-Regular.eot" ::
    "KaTeX_Size4-Regular.ttf" ::
    "KaTeX_Size4-Regular.woff" ::
    "KaTeX_Size4-Regular.woff2" ::
    "valamis-icons32.eot" ::
    "valamis-icons32.svg" ::
    "valamis-icons32.ttf" ::
    "valamis-icons32.woff" ::
    "vjs.eot" ::
    "vjs.svg" ::
    "vjs.ttf" ::
    "vjs.woff" ::
    Nil

  val PreviewResourceFolder = "preview-resources/pdf/"
  val PreviewResourceFiles =
    "build/pdf.js" ::
    "build/pdf.worker.js" ::
    "components/compatibility.js" ::
    "components/pdf_viewer.css" ::
    "components/pdf_viewer.js" ::
    "web/compatibility.js" ::
    "web/debugger.js" ::
    "web/l10n.js" ::
    "web/pageviewer.html" ::
    "web/pageviewer.js" ::
    "web/viewer.css" ::
    "web/viewer.html" ::
    "web/viewer.js" ::
    "web/cmaps/78-EUC-H.bcmap" ::
    "web/cmaps/78-EUC-V.bcmap" ::
    "web/cmaps/78-H.bcmap" ::
    "web/cmaps/78ms-RKSJ-H.bcmap" ::
    "web/cmaps/78ms-RKSJ-V.bcmap" ::
    "web/cmaps/78-RKSJ-H.bcmap" ::
    "web/cmaps/78-RKSJ-V.bcmap" ::
    "web/cmaps/78-V.bcmap" ::
    "web/cmaps/83pv-RKSJ-H.bcmap" ::
    "web/cmaps/90msp-RKSJ-H.bcmap" ::
    "web/cmaps/90msp-RKSJ-V.bcmap" ::
    "web/cmaps/90ms-RKSJ-H.bcmap" ::
    "web/cmaps/90ms-RKSJ-V.bcmap" ::
    "web/cmaps/90pv-RKSJ-H.bcmap" ::
    "web/cmaps/90pv-RKSJ-V.bcmap" ::
    "web/cmaps/Add-H.bcmap" ::
    "web/cmaps/Add-RKSJ-H.bcmap" ::
    "web/cmaps/Add-RKSJ-V.bcmap" ::
    "web/cmaps/Add-V.bcmap" ::
    "web/cmaps/Adobe-CNS1-0.bcmap" ::
    "web/cmaps/Adobe-CNS1-1.bcmap" ::
    "web/cmaps/Adobe-CNS1-2.bcmap" ::
    "web/cmaps/Adobe-CNS1-3.bcmap" ::
    "web/cmaps/Adobe-CNS1-4.bcmap" ::
    "web/cmaps/Adobe-CNS1-5.bcmap" ::
    "web/cmaps/Adobe-CNS1-6.bcmap" ::
    "web/cmaps/Adobe-CNS1-UCS2.bcmap" ::
    "web/cmaps/Adobe-GB1-0.bcmap" ::
    "web/cmaps/Adobe-GB1-1.bcmap" ::
    "web/cmaps/Adobe-GB1-2.bcmap" ::
    "web/cmaps/Adobe-GB1-3.bcmap" ::
    "web/cmaps/Adobe-GB1-4.bcmap" ::
    "web/cmaps/Adobe-GB1-5.bcmap" ::
    "web/cmaps/Adobe-GB1-UCS2.bcmap" ::
    "web/cmaps/Adobe-Japan1-0.bcmap" ::
    "web/cmaps/Adobe-Japan1-1.bcmap" ::
    "web/cmaps/Adobe-Japan1-2.bcmap" ::
    "web/cmaps/Adobe-Japan1-3.bcmap" ::
    "web/cmaps/Adobe-Japan1-4.bcmap" ::
    "web/cmaps/Adobe-Japan1-5.bcmap" ::
    "web/cmaps/Adobe-Japan1-6.bcmap" ::
    "web/cmaps/Adobe-Japan1-UCS2.bcmap" ::
    "web/cmaps/Adobe-Korea1-0.bcmap" ::
    "web/cmaps/Adobe-Korea1-1.bcmap" ::
    "web/cmaps/Adobe-Korea1-2.bcmap" ::
    "web/cmaps/Adobe-Korea1-UCS2.bcmap" ::
    "web/cmaps/B5-H.bcmap" ::
    "web/cmaps/B5pc-H.bcmap" ::
    "web/cmaps/B5pc-V.bcmap" ::
    "web/cmaps/B5-V.bcmap" ::
    "web/cmaps/CNS1-H.bcmap" ::
    "web/cmaps/CNS1-V.bcmap" ::
    "web/cmaps/CNS2-H.bcmap" ::
    "web/cmaps/CNS2-V.bcmap" ::
    "web/cmaps/CNS-EUC-H.bcmap" ::
    "web/cmaps/CNS-EUC-V.bcmap" ::
    "web/cmaps/ETen-B5-H.bcmap" ::
    "web/cmaps/ETen-B5-V.bcmap" ::
    "web/cmaps/ETenms-B5-H.bcmap" ::
    "web/cmaps/ETenms-B5-V.bcmap" ::
    "web/cmaps/ETHK-B5-H.bcmap" ::
    "web/cmaps/ETHK-B5-V.bcmap" ::
    "web/cmaps/EUC-H.bcmap" ::
    "web/cmaps/EUC-V.bcmap" ::
    "web/cmaps/Ext-H.bcmap" ::
    "web/cmaps/Ext-RKSJ-H.bcmap" ::
    "web/cmaps/Ext-RKSJ-V.bcmap" ::
    "web/cmaps/Ext-V.bcmap" ::
    "web/cmaps/GB-EUC-H.bcmap" ::
    "web/cmaps/GB-EUC-V.bcmap" ::
    "web/cmaps/GB-H.bcmap" ::
    "web/cmaps/GBK2K-H.bcmap" ::
    "web/cmaps/GBK2K-V.bcmap" ::
    "web/cmaps/GBK-EUC-H.bcmap" ::
    "web/cmaps/GBK-EUC-V.bcmap" ::
    "web/cmaps/GBKp-EUC-H.bcmap" ::
    "web/cmaps/GBKp-EUC-V.bcmap" ::
    "web/cmaps/GBpc-EUC-H.bcmap" ::
    "web/cmaps/GBpc-EUC-V.bcmap" ::
    "web/cmaps/GBT-EUC-H.bcmap" ::
    "web/cmaps/GBT-EUC-V.bcmap" ::
    "web/cmaps/GBT-H.bcmap" ::
    "web/cmaps/GBTpc-EUC-H.bcmap" ::
    "web/cmaps/GBTpc-EUC-V.bcmap" ::
    "web/cmaps/GBT-V.bcmap" ::
    "web/cmaps/GB-V.bcmap" ::
    "web/cmaps/Hankaku.bcmap" ::
    "web/cmaps/H.bcmap" ::
    "web/cmaps/Hiragana.bcmap" ::
    "web/cmaps/HKdla-B5-H.bcmap" ::
    "web/cmaps/HKdla-B5-V.bcmap" ::
    "web/cmaps/HKdlb-B5-H.bcmap" ::
    "web/cmaps/HKdlb-B5-V.bcmap" ::
    "web/cmaps/HKgccs-B5-H.bcmap" ::
    "web/cmaps/HKgccs-B5-V.bcmap" ::
    "web/cmaps/HKm314-B5-H.bcmap" ::
    "web/cmaps/HKm314-B5-V.bcmap" ::
    "web/cmaps/HKm471-B5-H.bcmap" ::
    "web/cmaps/HKm471-B5-V.bcmap" ::
    "web/cmaps/HKscs-B5-H.bcmap" ::
    "web/cmaps/HKscs-B5-V.bcmap" ::
    "web/cmaps/Katakana.bcmap" ::
    "web/cmaps/KSC-EUC-H.bcmap" ::
    "web/cmaps/KSC-EUC-V.bcmap" ::
    "web/cmaps/KSC-H.bcmap" ::
    "web/cmaps/KSC-Johab-H.bcmap" ::
    "web/cmaps/KSC-Johab-V.bcmap" ::
    "web/cmaps/KSCms-UHC-H.bcmap" ::
    "web/cmaps/KSCms-UHC-HW-H.bcmap" ::
    "web/cmaps/KSCms-UHC-HW-V.bcmap" ::
    "web/cmaps/KSCms-UHC-V.bcmap" ::
    "web/cmaps/KSCpc-EUC-H.bcmap" ::
    "web/cmaps/KSCpc-EUC-V.bcmap" ::
    "web/cmaps/KSC-V.bcmap" ::
    "web/cmaps/LICENSE" ::
    "web/cmaps/NWP-H.bcmap" ::
    "web/cmaps/NWP-V.bcmap" ::
    "web/cmaps/RKSJ-H.bcmap" ::
    "web/cmaps/RKSJ-V.bcmap" ::
    "web/cmaps/Roman.bcmap" ::
    "web/cmaps/UniCNS-UCS2-H.bcmap" ::
    "web/cmaps/UniCNS-UCS2-V.bcmap" ::
    "web/cmaps/UniCNS-UTF16-H.bcmap" ::
    "web/cmaps/UniCNS-UTF16-V.bcmap" ::
    "web/cmaps/UniCNS-UTF32-H.bcmap" ::
    "web/cmaps/UniCNS-UTF32-V.bcmap" ::
    "web/cmaps/UniCNS-UTF8-H.bcmap" ::
    "web/cmaps/UniCNS-UTF8-V.bcmap" ::
    "web/cmaps/UniGB-UCS2-H.bcmap" ::
    "web/cmaps/UniGB-UCS2-V.bcmap" ::
    "web/cmaps/UniGB-UTF16-H.bcmap" ::
    "web/cmaps/UniGB-UTF16-V.bcmap" ::
    "web/cmaps/UniGB-UTF32-H.bcmap" ::
    "web/cmaps/UniGB-UTF32-V.bcmap" ::
    "web/cmaps/UniGB-UTF8-H.bcmap" ::
    "web/cmaps/UniGB-UTF8-V.bcmap" ::
    "web/cmaps/UniJIS2004-UTF16-H.bcmap" ::
    "web/cmaps/UniJIS2004-UTF16-V.bcmap" ::
    "web/cmaps/UniJIS2004-UTF32-H.bcmap" ::
    "web/cmaps/UniJIS2004-UTF32-V.bcmap" ::
    "web/cmaps/UniJIS2004-UTF8-H.bcmap" ::
    "web/cmaps/UniJIS2004-UTF8-V.bcmap" ::
    "web/cmaps/UniJISPro-UCS2-HW-V.bcmap" ::
    "web/cmaps/UniJISPro-UCS2-V.bcmap" ::
    "web/cmaps/UniJISPro-UTF8-V.bcmap" ::
    "web/cmaps/UniJIS-UCS2-H.bcmap" ::
    "web/cmaps/UniJIS-UCS2-HW-H.bcmap" ::
    "web/cmaps/UniJIS-UCS2-HW-V.bcmap" ::
    "web/cmaps/UniJIS-UCS2-V.bcmap" ::
    "web/cmaps/UniJIS-UTF16-H.bcmap" ::
    "web/cmaps/UniJIS-UTF16-V.bcmap" ::
    "web/cmaps/UniJIS-UTF32-H.bcmap" ::
    "web/cmaps/UniJIS-UTF32-V.bcmap" ::
    "web/cmaps/UniJIS-UTF8-H.bcmap" ::
    "web/cmaps/UniJIS-UTF8-V.bcmap" ::
    "web/cmaps/UniJISX02132004-UTF32-H.bcmap" ::
    "web/cmaps/UniJISX02132004-UTF32-V.bcmap" ::
    "web/cmaps/UniJISX0213-UTF32-H.bcmap" ::
    "web/cmaps/UniJISX0213-UTF32-V.bcmap" ::
    "web/cmaps/UniKS-UCS2-H.bcmap" ::
    "web/cmaps/UniKS-UCS2-V.bcmap" ::
    "web/cmaps/UniKS-UTF16-H.bcmap" ::
    "web/cmaps/UniKS-UTF16-V.bcmap" ::
    "web/cmaps/UniKS-UTF32-H.bcmap" ::
    "web/cmaps/UniKS-UTF32-V.bcmap" ::
    "web/cmaps/UniKS-UTF8-H.bcmap" ::
    "web/cmaps/UniKS-UTF8-V.bcmap" ::
    "web/cmaps/V.bcmap" ::
    "web/cmaps/WP-Symbol.bcmap" ::
    "web/images/annotation-check.svg" ::
    "web/images/annotation-comment.svg" ::
    "web/images/annotation-help.svg" ::
    "web/images/annotation-insert.svg" ::
    "web/images/annotation-key.svg" ::
    "web/images/annotation-newparagraph.svg" ::
    "web/images/annotation-noicon.svg" ::
    "web/images/annotation-note.svg" ::
    "web/images/annotation-paragraph.svg" ::
    "web/images/findbarButton-next@2x.png" ::
    "web/images/findbarButton-next.png" ::
    "web/images/findbarButton-next-rtl@2x.png" ::
    "web/images/findbarButton-next-rtl.png" ::
    "web/images/findbarButton-previous@2x.png" ::
    "web/images/findbarButton-previous.png" ::
    "web/images/findbarButton-previous-rtl@2x.png" ::
    "web/images/findbarButton-previous-rtl.png" ::
    "web/images/grabbing.cur" ::
    "web/images/grab.cur" ::
    "web/images/loading-icon.gif" ::
    "web/images/loading-small.png" ::
    "web/images/secondaryToolbarButton-documentProperties@2x.png" ::
    "web/images/secondaryToolbarButton-documentProperties.png" ::
    "web/images/secondaryToolbarButton-firstPage@2x.png" ::
    "web/images/secondaryToolbarButton-firstPage.png" ::
    "web/images/secondaryToolbarButton-handTool@2x.png" ::
    "web/images/secondaryToolbarButton-handTool.png" ::
    "web/images/secondaryToolbarButton-lastPage@2x.png" ::
    "web/images/secondaryToolbarButton-lastPage.png" ::
    "web/images/secondaryToolbarButton-rotateCcw@2x.png" ::
    "web/images/secondaryToolbarButton-rotateCcw.png" ::
    "web/images/secondaryToolbarButton-rotateCw@2x.png" ::
    "web/images/secondaryToolbarButton-rotateCw.png" ::
    "web/images/shadow.png" ::
    "web/images/texture.png" ::
    "web/images/toolbarButton-bookmark@2x.png" ::
    "web/images/toolbarButton-bookmark.png" ::
    "web/images/toolbarButton-download@2x.png" ::
    "web/images/toolbarButton-download.png" ::
    "web/images/toolbarButton-menuArrows@2x.png" ::
    "web/images/toolbarButton-menuArrows.png" ::
    "web/images/toolbarButton-openFile@2x.png" ::
    "web/images/toolbarButton-openFile.png" ::
    "web/images/toolbarButton-pageDown@2x.png" ::
    "web/images/toolbarButton-pageDown.png" ::
    "web/images/toolbarButton-pageDown-rtl@2x.png" ::
    "web/images/toolbarButton-pageDown-rtl.png" ::
    "web/images/toolbarButton-pageUp@2x.png" ::
    "web/images/toolbarButton-pageUp.png" ::
    "web/images/toolbarButton-pageUp-rtl@2x.png" ::
    "web/images/toolbarButton-pageUp-rtl.png" ::
    "web/images/toolbarButton-presentationMode@2x.png" ::
    "web/images/toolbarButton-presentationMode.png" ::
    "web/images/toolbarButton-print@2x.png" ::
    "web/images/toolbarButton-print.png" ::
    "web/images/toolbarButton-search@2x.png" ::
    "web/images/toolbarButton-search.png" ::
    "web/images/toolbarButton-secondaryToolbarToggle@2x.png" ::
    "web/images/toolbarButton-secondaryToolbarToggle.png" ::
    "web/images/toolbarButton-secondaryToolbarToggle-rtl@2x.png" ::
    "web/images/toolbarButton-secondaryToolbarToggle-rtl.png" ::
    "web/images/toolbarButton-sidebarToggle@2x.png" ::
    "web/images/toolbarButton-sidebarToggle.png" ::
    "web/images/toolbarButton-sidebarToggle-rtl@2x.png" ::
    "web/images/toolbarButton-sidebarToggle-rtl.png" ::
    "web/images/toolbarButton-viewAttachments@2x.png" ::
    "web/images/toolbarButton-viewAttachments.png" ::
    "web/images/toolbarButton-viewOutline@2x.png" ::
    "web/images/toolbarButton-viewOutline.png" ::
    "web/images/toolbarButton-viewOutline-rtl@2x.png" ::
    "web/images/toolbarButton-viewOutline-rtl.png" ::
    "web/images/toolbarButton-viewThumbnail@2x.png" ::
    "web/images/toolbarButton-viewThumbnail.png" ::
    "web/images/toolbarButton-zoomIn@2x.png" ::
    "web/images/toolbarButton-zoomIn.png" ::
    "web/images/toolbarButton-zoomOut@2x.png" ::
    "web/images/toolbarButton-zoomOut.png" ::
    "web/locale/locale.properties" ::
    "web/locale/ach/viewer.properties" ::
    "web/locale/af/viewer.properties" ::
    "web/locale/ak/viewer.properties" ::
    "web/locale/an/viewer.properties" ::
    "web/locale/ar/viewer.properties" ::
    "web/locale/as/viewer.properties" ::
    "web/locale/ast/viewer.properties" ::
    "web/locale/az/viewer.properties" ::
    "web/locale/be/viewer.properties" ::
    "web/locale/bg/viewer.properties" ::
    "web/locale/bn-BD/viewer.properties" ::
    "web/locale/bn-IN/viewer.properties" ::
    "web/locale/br/viewer.properties" ::
    "web/locale/bs/viewer.properties" ::
    "web/locale/ca/viewer.properties" ::
    "web/locale/cs/viewer.properties" ::
    "web/locale/csb/viewer.properties" ::
    "web/locale/cy/viewer.properties" ::
    "web/locale/da/viewer.properties" ::
    "web/locale/de/viewer.properties" ::
    "web/locale/el/viewer.properties" ::
    "web/locale/en-GB/viewer.properties" ::
    "web/locale/en-US/viewer.properties" ::
    "web/locale/en-ZA/viewer.properties" ::
    "web/locale/eo/viewer.properties" ::
    "web/locale/es-AR/viewer.properties" ::
    "web/locale/es-CL/viewer.properties" ::
    "web/locale/es-ES/viewer.properties" ::
    "web/locale/es-MX/viewer.properties" ::
    "web/locale/et/viewer.properties" ::
    "web/locale/eu/viewer.properties" ::
    "web/locale/fa/viewer.properties" ::
    "web/locale/ff/viewer.properties" ::
    "web/locale/fi/viewer.properties" ::
    "web/locale/fr/viewer.properties" ::
    "web/locale/fy-NL/viewer.properties" ::
    "web/locale/ga-IE/viewer.properties" ::
    "web/locale/gd/viewer.properties" ::
    "web/locale/gl/viewer.properties" ::
    "web/locale/gu-IN/viewer.properties" ::
    "web/locale/he/viewer.properties" ::
    "web/locale/hi-IN/viewer.properties" ::
    "web/locale/hr/viewer.properties" ::
    "web/locale/hu/viewer.properties" ::
    "web/locale/hy-AM/viewer.properties" ::
    "web/locale/id/viewer.properties" ::
    "web/locale/is/viewer.properties" ::
    "web/locale/it/viewer.properties" ::
    "web/locale/ja/viewer.properties" ::
    "web/locale/ka/viewer.properties" ::
    "web/locale/kk/viewer.properties" ::
    "web/locale/km/viewer.properties" ::
    "web/locale/kn/viewer.properties" ::
    "web/locale/ko/viewer.properties" ::
    "web/locale/ku/viewer.properties" ::
    "web/locale/lg/viewer.properties" ::
    "web/locale/lij/viewer.properties" ::
    "web/locale/lt/viewer.properties" ::
    "web/locale/lv/viewer.properties" ::
    "web/locale/mai/viewer.properties" ::
    "web/locale/mk/viewer.properties" ::
    "web/locale/ml/viewer.properties" ::
    "web/locale/mn/viewer.properties" ::
    "web/locale/mr/viewer.properties" ::
    "web/locale/ms/viewer.properties" ::
    "web/locale/my/viewer.properties" ::
    "web/locale/nb-NO/viewer.properties" ::
    "web/locale/nl/viewer.properties" ::
    "web/locale/nn-NO/viewer.properties" ::
    "web/locale/nso/viewer.properties" ::
    "web/locale/oc/viewer.properties" ::
    "web/locale/or/viewer.properties" ::
    "web/locale/pa-IN/viewer.properties" ::
    "web/locale/pl/viewer.properties" ::
    "web/locale/pt-BR/viewer.properties" ::
    "web/locale/pt-PT/viewer.properties" ::
    "web/locale/rm/viewer.properties" ::
    "web/locale/ro/viewer.properties" ::
    "web/locale/ru/viewer.properties" ::
    "web/locale/rw/viewer.properties" ::
    "web/locale/sah/viewer.properties" ::
    "web/locale/si/viewer.properties" ::
    "web/locale/sk/viewer.properties" ::
    "web/locale/sl/viewer.properties" ::
    "web/locale/son/viewer.properties" ::
    "web/locale/sq/viewer.properties" ::
    "web/locale/sr/viewer.properties" ::
    "web/locale/sv-SE/viewer.properties" ::
    "web/locale/sw/viewer.properties" ::
    "web/locale/ta/viewer.properties" ::
    "web/locale/ta-LK/viewer.properties" ::
    "web/locale/te/viewer.properties" ::
    "web/locale/th/viewer.properties" ::
    "web/locale/tl/viewer.properties" ::
    "web/locale/tn/viewer.properties" ::
    "web/locale/tr/viewer.properties" ::
    "web/locale/uk/viewer.properties" ::
    "web/locale/ur/viewer.properties" ::
    "web/locale/vi/viewer.properties" ::
    "web/locale/wo/viewer.properties" ::
    "web/locale/xh/viewer.properties" ::
    "web/locale/zh-CN/viewer.properties" ::
    "web/locale/zh-TW/viewer.properties" ::
    "web/locale/zu/viewer.properties" ::
    Nil
}