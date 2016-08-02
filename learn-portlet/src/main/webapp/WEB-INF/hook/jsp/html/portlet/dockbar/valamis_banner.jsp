<%@ include file="/html/portlet/dockbar/init.jsp" %>

<c:if test="<%= themeDisplay.isShowSiteAdministrationIcon() %>">
    <style>
        .showBanner {
            display: block;
        }
        .hideBanner {
            display: none;
        }
        .adminBanner {
            background-color: #2974A6;
            position: absolute;
            top: 0;
            left: 0;
            opacity: 0.9;
            z-index: 1000;
            width: 100%;
            height: 50px;
            text-align: center;
            color: white;
            font-weight: bold;
            box-sizing: border-box;
            padding-top: 15px;
        }
        .adminBanner a {
            color: white;
            text-decoration: underline;
        }
        .closeButton {
            background: url(/learn-portlet/images/popup-close-white.png) left center;
            width: 50px;
            height: 50px;
            position: absolute;
            right: 0;
            top: 0;
            cursor: pointer;
        }
    </style>
    <div class="adminBanner hideBanner" id="<portlet:namespace />Banner">
        This is Valamis CE version - No support - No warranty - <a target="_blank" href="http://valamis.arcusys.com/-/valamis-ee-download">Update to EE version</a>
        <div class="closeButton" onclick="<portlet:namespace />DoClose()"></div>
    </div>
    <script>
        (function() {
            var breakBannerCookie = document.cookie.match('(^|;) ?' + 'breakBanner' + '=([^;]*)(;|$)');
            if (!breakBannerCookie) {
                var banner = document.getElementById('<portlet:namespace />Banner');
                banner.className = 'adminBanner showBanner';
            }
        })();
        var <portlet:namespace />DoClose = function() {
            var banner = document.getElementById('<portlet:namespace />Banner');
            banner.className = 'adminBanner hideBanner';
            var cookieDate = new Date();
            cookieDate.setMinutes(cookieDate.getMinutes() + 15);
            document.cookie = "breakBanner = true; expires=" + cookieDate.toGMTString();
        };
    </script>
</c:if>