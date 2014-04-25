<%-- <%@page import="com.hotpads.config.server.enums.HotPadsServerType"%> --%>
<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@page import="com.hotpads.setting.cluster.ClusterSettingScope"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hotpads : Job Project - Cluster settings</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/job.css"/>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require([
               "bootstrap/bootstrap", "plugin/sorttable"
         ], function($) {});
</script>
</head>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<div class="page-header">
			<h2>${setting.getName()}</h2>
		</div>
		<p>Default value : ${setting.getDefaultValue()}</p>
		<p>Actual value on this server : ${setting.get()}</p>
		<h3>Custom settings in database</h3>
		<table class="table table-bordered table-condensed table-striped">
			<tr>
				<th>Scope</th>
				<th>ServerType</th>
				<th>Instance</th>
				<th>Application</th>
				<th>Value</th>
			</tr>
			<c:forEach items="${settings}" var="setting">
				<tr>
					<td>${setting.scope}</td>
					<td>${setting.serverType}</td>
					<td>${setting.instance}</td>
					<td>${setting.application}</td>
					<td>${setting.value}</td>
				</tr>
			</c:forEach>
		</table>

	</div>
</body>
</html>