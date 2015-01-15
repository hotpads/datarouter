<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@page import="com.hotpads.setting.cluster.ClusterSettingScope"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Edit Cluster Settings | Datarouter</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require(["bootstrap/bootstrap"], function($) {});
</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<a href="?submitAction=browseSettings" class="btn btn-primary">&rarr; Browse settings</a>
		<br>
		<h2 class="page-header">Create Cluster Settings</h2>
		<form class="well form-inline" method="post" action="?">
			<input type="text" name="name" class="input-medium" placeholder="Name" required>
			<select	name="serverType" class="input-small" required>
				<option value="">Type</option>
				<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
					<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
				</c:forEach>
			</select>
			<input type="text" name="instance" class="input-medium" placeholder="Instance">
			<input type="text" name="application" class="input-medium" placeholder="Application">
			<input type="text" name="value" class="input-medium" placeholder="Value" required>
			<input class="btn btn-mini btn-success" type="submit" name="submitAction" value="create">
		</form>
		<h2 class="page-header">Search Cluster Settings</h2>
		<form class="well form-search" method="get" action="?">
			<input type="text" class="input-medium search-query" name="prefix" placeholder="Name Prefix" autofocus>
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
					<input type="hidden" name="name" value="${setting.name}">
					<input type="hidden" name="scope" value="${setting.scope}">
					<input type="hidden" name="serverType" value="${setting.serverType}">
					<input type="hidden" name="instance" value="${setting.instance}">
					<input type="hidden" name="application" value="${setting.application}">
					<tr>
						<td>${setting.name}</td>
						<td>${setting.scope}</td>
						<td>${setting.serverType}</td>
						<td>${setting.instance}</td>
						<td>${setting.application}</td>
						<td>
							<input name="value" class="input-medium" value="${setting.value}">
						</td>
						<td class="center" >
							<a class="btn btn-mini btn-danger"
							href="?submitAction=delete
							&name=${setting.name}
							&scope=${setting.scope}
							&serverType=${setting.serverType}
							&instance=${setting.instance}
							&application=${setting.application}"
							onclick="return window.confirm('Are you sure?');">delete</a>
						</td>
						<td class="center">
							<input class="btn btn-mini btn-warning" type="submit" name="submitAction" value="update">
						</td>
					</tr>
				</form>
			</c:forEach>
		</table>
	</div>
</body>
</html>