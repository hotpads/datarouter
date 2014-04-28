<%-- <%@page import="com.hotpads.config.server.enums.HotPadsServerType"%> --%>
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
}
</script>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
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
			<table class="table table-condensed center-header">
				<tr>
					<th rowspan="2">Default and current values</th>
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
					<c:set var="customSettings" value="${mapListsCustomSettings.get(setting.getName())}"></c:set>
					<c:forEach var="i" begin="0" end="${customSettings.size()}">
						<tr
							<c:choose>
								<c:when test="${setting.getHasRedundantCustomValue()}">style="background: #F5FAFA;"</c:when>
								<c:when test="${setting.getHasCustomValue()}">style="background: #ACD1E9;"</c:when>
							</c:choose>
							>
							<c:choose>
								<c:when test="${i == 0}">
									<td rowspan="${customSettings.size() + 1}">
										<p>
											<c:choose>
												<c:when test="${setting.getHasCustomValue()}">
													<a href=?submitAction=detailSetting&name=${setting.getName()}>
														<strong>${setting.getName()}</strong>
													</a>
												</c:when>
												<c:otherwise>${setting.getName()}</c:otherwise>
											</c:choose>
											<br>
											<strong>Current value : </strong>${setting.getValue()}
											<br>
											<strong>Default value : </strong>${setting.getDefaultValue()}
										</p>
									</td>
								</c:when>
							</c:choose>
							<c:choose>
								<c:when test="${i == customSettings.size()}">
									<form method="post" action="?">
										<input type="hidden" name="nodeName" value="${nodeName}">
										<input type="hidden" name="name" value="${setting.getName()}">
										<td>
											<a id="link_${setting.hashCode()}" onclick="showCreateForm(this)">Add a custom setting</a>
										</td>
										
										<td>
											<select	name="serverType" class="input-small" id="type_${setting.hashCode()}" style="display: none;" >
												<option disabled selected>Type</option>
												<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
													<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
												</c:forEach>
											</select>
										</td>
										<td>
												<input type="text" id="inst_${setting.hashCode()}" class="input-small" style="display: none;" name="instance" placeholder="Instance">
										</td>
										<td>
												<input type="text" id="appl_${setting.hashCode()}" class="input-small" style="display: none;" name="application" placeholder="Application">
										</td>
										<td>
												<input type="text" id="valu_${setting.hashCode()}" class="input-small" style="display: none;" name="value" placeholder="Value">
										</td>
										<td class="center">
												<input type="submit" id="acti_${setting.hashCode()}" class="btn btn-mini btn-warning" style="display: none;" name="submitAction" value="create">
										</td>
									</form>
								</c:when>
								<c:otherwise>
									<c:set var="customSetting" value="${customSettings[i]}"></c:set>
									<td>${customSetting.scope}</td>
									<td>${customSetting.serverType}</td>
									<td>${customSetting.instance}</td>
									<td>${customSetting.application}</td>
									<td>${customSetting.value}</td>
									<td class="center">
										<a class="btn btn-mini btn-danger"
										href="?submitAction=delete
										&nodeName=${nodeName}
										&name=${customSetting.name}
										&scope=${customSetting.scope}
										&serverType=${customSetting.serverType}
										&instance=${customSetting.instance}
										&application=${customSetting.application}"
										onclick="return confirm('Are you sure?');">delete</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>						
				</c:forEach>
			</table>
		</c:if>
	</div>
</body>
</html>