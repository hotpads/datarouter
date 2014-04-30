<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@page import="com.hotpads.setting.cluster.ClusterSettingScope"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hotpads : Cluster settings</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<script type="text/javascript">
 		require(["bootstrap/bootstrap"], function($) {});
</script>
<script type="text/javascript">
function stayFocus(object){
	setTimeout(function(){object.form.instance.focus();},10);
}
function showCreateForm(link) {
	var hash = link.id.substring(5);
	$(link).hide();
	$('#type_' + hash).show();
	$('#inst_' + hash).show();
	$('#appl_' + hash).show();
	$('#valu_' + hash).show();
	$('#acti_' + hash).show();
	document.getElementById('inst_' + hash).parentNode.parentNode.removeAttribute('class');
}
</script>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<a href="?" class="btn btn-primary">&rarr; All settings</a><br/>
		<br>
		<h2 class="page-header">Cluster Settings Browser</h2>
		<ul class="breadcrumb">
			<c:forEach items="${ancestors}" var="ancestor">	
				<c:choose>
					<c:when test="${node.getName().equals(ancestor.getName())}">
						<li class="active">${ancestor.getShortName()} <span class="divider">.</span></li>
					</c:when>
					<c:otherwise>
						<li>
							<a href="?submitAction=browseSettings&name=${ancestor.getName()}">${ancestor.getShortName()}</a>
							<span class="divider">.</span>
						</li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</ul>
		<c:if test="${!children.isEmpty()}">
			<div class="btn-toolbar">
				<div class="btn-group">
					<c:forEach items="${children}" var="child">		
						<a class="btn" href="?submitAction=browseSettings&name=${child.getName()}">${child.getShortName()}</a>
					</c:forEach>
				</div>	
			</div>
		</c:if>
		<c:if test="${!listSettings.isEmpty()}">
			<table class="table table-condensed center-header">
				<tr>
					<th rowspan="2">Name</th>
					<th colspan="6">Custom values</th>
				</tr>
				<tr>
					<th>Scope</th>
					<th>ServerType</th>
					<th>Instance</th>
					<th>Application</th>
					<th>Value</th>
					<th>Action</th>
				</tr>
				<c:forEach items="${listSettings}" var="setting">
					<tr
						<c:choose>
							<c:when test="${setting.getHasRedundantCustomValue()}">style="background: #F5FAFA;"</c:when>
							<c:when test="${setting.getHasCustomValue()}">style="background: #ACD1E9;"</c:when>
						</c:choose>
						>
						<c:set var="customSettings" value="${mapListsCustomSettings.get(setting.getName())}"></c:set>
						<td rowspan="${customSettings.size() + 2}" id="${fn:replace(setting.getName(), '.', '_')}">
							${setting.getName()}
						</td>
						<td colspan="5">
							<span style="margin: 0 20px">
								current : <strong>${setting.getValue()}</strong>
							</span>
							<wbr>
							<span style="margin: 0 20px">
								default : <strong>${setting.getDefaultValue()}</strong>
							</span>
						</td>
						<td class="center">
							<a id="link_${setting.hashCode()}" onclick="showCreateForm(this)">add</a>
						</td>
					</tr>
					<c:forEach var="customSetting" items="${customSettings}">
						<tr>
							<form method="post" action="?">
								<input type="hidden" name="nodeName" value="${nodeName}">
								<input type="hidden" name="name" value="${customSetting.name}">
								<input type="hidden" name="scope" value="${customSetting.scope}">
								<input type="hidden" name="serverType" value="${customSetting.serverType}">
								<input type="hidden" name="instance" value="${customSetting.instance}">
								<input type="hidden" name="application" value="${customSetting.application}">
							<td>${customSetting.scope}</td>
								<td>${customSetting.serverType}</td>
								<td>${customSetting.instance}</td>
								<td>${customSetting.application}</td>
								<td>
									<input value="${customSetting.value}" class="input-mini"  placeholder="Value" name="value" id="valu_${customSetting.hashCode()}">
								</td>
								<td class="center">
									<input type="submit" name="submitAction" value="update" class="btn btn-mini btn-warning">
									<a
										class="btn btn-mini btn-danger"
										href="?submitAction=delete
											&nodeName=${nodeName}
											&name=${customSetting.name}
											&scope=${customSetting.scope}
											&serverType=${customSetting.serverType}
											&instance=${customSetting.instance}
											&application=${customSetting.application}"
										onclick="return confirm('Are you sure?');">
										delete
									</a>
								</td>
							</form>
						</tr>
					</c:forEach>
					<tr class="add_form">
						<form method="post" action="?">
							<input type="hidden" name="nodeName" value="${nodeName}">
							<input type="hidden" name="name" value="${setting.getName()}">
							<td/>
							<td>
								<select	name="serverType" id="type_${setting.hashCode()}" style="display: none;" class="setting-type input-small" required>
									<option disabled selected>Type</option>
									<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
										<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
									</c:forEach>
								</select>
							</td>
							<td>
									<input id="inst_${setting.hashCode()}" style="display: none;" class="input-small" name="instance" placeholder="Instance">
							</td>
							<td>
									<input id="appl_${setting.hashCode()}" style="display: none;" class="input-small" name="application" placeholder="Application">
							</td>
							<td>
									<input id="valu_${setting.hashCode()}" style="display: none;" class="input-mini" name="value" placeholder="Value" required>
							</td>
							<td class="center">
									<input type="submit" id="acti_${setting.hashCode()}" class="btn btn-mini btn-warning" style="display: none;" name="submitAction" value="create">
							</td>
						</form>
					</tr>
				</c:forEach>
			</table>
		</c:if>
	</div>
</body>
</html>