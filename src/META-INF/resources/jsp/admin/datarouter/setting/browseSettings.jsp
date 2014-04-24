<%@page import="com.hotpads.setting.HotPadsServerType"%>
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
               "bootstrap/bootstrap"
         ], function($) {});
</script>

<style type="text/css">
#magicRow:hover td #hidden
{display:block;}
<%--#magicRow td #hidden
{display:none;}
#magicRow:hover td #hide
{display:none;}
#magicRow td #hide
{display:block;} --%>
</style>
<script type="text/javascript">
function stayFocus(object){
	setTimeout(function(){object.form.instance.focus();},10);
}
</script>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container">
		<h2 class="page-header">Cluster Settings Browser</h2>
		<ul class="breadcrumb">
			<c:forEach items="${ancestors}" var="ancestor">	
				<c:choose>
					<c:when test="${node.getName().equals(ancestor.getName())}">
						<li class="active">${ancestor.getShortName()} <span class="divider">.</span></li>
					</c:when>
					<c:otherwise><li><a href="?submitAction=browseSettings&name=${ancestor.getName()}">
						${ancestor.getShortName()}</a> <span class="divider">.</span></li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</ul>
		<c:if test="${!children.isEmpty()}">
			<div class="btn-toolbar">
				<div class="btn-group">
					<c:forEach items="${children}" var="child">		
						<a class="btn"
							href="?submitAction=browseSettings&name=${child.getName()}">${child.getShortName()}</a>
					</c:forEach>
				</div>	
			</div>
		</c:if>
		<c:if test="${!listSettings.isEmpty()}">
			<table class="table table-condensed">
				<tr>
					<th class="span4">Setting name</th>
					<th class="span8">Current Value</th>
				</tr>
				<c:forEach items="${listSettings}" var="setting">
					<tr id="magicRow"
						<c:choose><c:when test="${setting.getHasRedundantCustomValue()}">style="background: #F5FAFA;"</c:when>
						<c:when test="${setting.getHasCustomValue()}">style="background: #ACD1E9;"</c:when></c:choose>
						>
						<td class="span4">
							<c:choose>
								<c:when test="${setting.getHasCustomValue()}">
									<a href=?submitAction=detailSetting&name=${setting.getName()}><strong>
										${setting.getName()}</strong></a>
								</c:when>
							<c:otherwise>${setting.getName()}</c:otherwise></c:choose>
						</td>
						<td class="span8">
						<c:choose>
						<c:when test="${setting.getHasCustomValue()}">
								<div id="hide"><strong>Current value : </strong>${setting.getValue()}</div>
								<div id="hidden">
									<p>
										<strong>Default value :</strong>
										${setting.getDefaultValue()}
									</p>
									<form class="well form-inline" method="post" action="?">
										<label for="serverType"> Server Type : <select
											name="serverType" class="input-small" id="serverType" onchange="stayFocus(this)">
												<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
													<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
												</c:forEach>
										</select></label>
										<input type="hidden" name="nodeName" value="${nodeName}"></input>
										<input type="hidden" name="name" value="${setting.getName()}"></input>
										<input type="text" name="instance" class="input-medium" placeholder="Instance" />
										<input type="text" name="value" class="input-medium" placeholder="Value" />
										<input class="btn btn-mini btn-warning" type="submit" name="submitAction" value="modify" />
									</form>
									<%--<p>
										<strong>Actual value on this server :</strong>
										${setting.get()}
									</p> --%>
									<h3>Custom settings in database</h3>
									<table
										class="table table-bordered table-condensed table-striped">
										<tr>
											<th>Scope</th>
											<th>ServerType</th>
											<th>Instance</th>
											<th>Application</th>
											<th>Value</th>
											<th>Delete</th>
										</tr>
										<c:forEach
											items="${mapListsCustomSettings.get(setting.getName())}"
											var="customSetting">
											<form method="post" action="?">
											<tr>
												<td>${customSetting.scope}</td>
												<td>${customSetting.serverType}</td>
												<td>${customSetting.instance}</td>
												<td>${customSetting.application}</td>
												<td>${customSetting.value}</td>
												<td><a class="btn btn-mini btn-danger"
													href="?submitAction=delete
													&nodeName=${nodeName}
													&name=${customSetting.name}
													&scope=${customSetting.scope}
													&serverType=${customSetting.serverType}
													&instance=${customSetting.instance}
													&application=${customSetting.application}"
													onclick="return window.confirm('Are you sure?');">delete</a>
												</td>
											</tr>
											</form>
										</c:forEach>
									</table>
								</div>
							</c:when>
							<c:otherwise>${setting.getDefaultValue()}</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</c:forEach>
			</table>
		</c:if>
	</div>
</body>
</html>