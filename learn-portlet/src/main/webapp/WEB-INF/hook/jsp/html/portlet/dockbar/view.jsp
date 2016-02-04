<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<liferay-util:buffer var="view">
    <liferay-util:include page="/html/portlet/dockbar/view.portal.jsp"/>
</liferay-util:buffer>

<liferay-util:buffer var="banner">
    <liferay-util:include page="/html/portlet/dockbar/valamis_banner.jsp"/>
</liferay-util:buffer>

<%= view %>
<%= banner %>