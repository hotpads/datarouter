<%@page import="com.hotpads.setting.HotPadsServerType"%>
<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@page import="com.hotpads.setting.cluster.ClusterSettingScope"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hotpads : Job Project - Cluster settings</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/job.css"/>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require([
               "bootstrap/bootstrap"
         ], function($) {});
</script>

</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container">
		<a href="?submitAction=browseSettings" class="btn btn-primary">&rarr; Browse settings</a><br/>
		<br/>
		<h2 class="page-header">Create Cluster Settings</h2>
		<form class="well form-inline" method="post" action="?">
			<input type="text" name="name" class="input-medium" placeholder="Name" />
			<label for="serverType"> Server Type : <select
				name="serverType" class="input-small" id="serverType">
					<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
						<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
					</c:forEach>
			</select></label>
			<input type="text" name="instance" class="input-medium" placeholder="Instance" />
			<input type="text" name="application" class="input-medium" placeholder="Application" />
			<input type="text" name="value" class="input-medium" placeholder="Value" />
			<input class="btn btn-mini btn-success" type="submit" name="submitAction" value="create" />
		</form>
		<h2 class="page-header">Search Cluster Settings</h2>
		<form class="well form-search" method="get" action="?">
			<input type="text" class="input-medium search-query" name="prefix"
				placeholder="Name Prefix" autofocus="autofocus">
			<button type="submit" class="btn btn-mini btn-primary">Search</button>
		</form>
		<table class="table table-bordered table-condensed">
			<tr style="background: black; color: white;">
				<th>Name</th>
				<th>Scope</th>
				<th>ServerType</th>
				<th>Instance</th>
				<th>Application</th>
				<th>Value</th>
				<th>Delete</th>
				<th>Update</th>
			</tr>
			<c:forEach items="${settings}" var="setting">
				<form method="post" action="?">
					<tr>
						<td><input type="hidden" name="name" value="${setting.name}">${setting.name}</input></td>
						<td><input type="hidden" name="scope"
							value="${setting.scope}">${setting.scope}</input></td>
						<td><input type="hidden" name="serverType"
							value="${setting.serverType}">${setting.serverType}</input></td>
						<td><input type="hidden" name="instance"
							value="${setting.instance}">${setting.instance}</input></td>
						<td><input type="hidden" name="application"
							value="${setting.application}">${setting.application}</input></td>
						<td><input type="text" name="value" class="input-medium" value="${setting.value}"
							class="span2" /></td>
						<td><a class="btn btn-mini btn-danger"
							href="?submitAction=delete
							&name=${setting.name}
							&scope=${setting.scope}
							&serverType=${setting.serverType}
							&instance=${setting.instance}
							&application=${setting.application}"
							onclick="return window.confirm('Are you sure?');">delete</a></td>
						<td><input class="btn btn-mini btn-warning" type="submit"
							name="submitAction" value="update" /></td>
					</tr>
				</form>
			</c:forEach>
		</table>
	</div>

</body>
</html>